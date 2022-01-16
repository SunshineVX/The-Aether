package com.gildedgames.aether.common.world.gen.chunk;

import com.gildedgames.aether.core.util.math.*;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public class CelledSpaceGenerator extends DelegatedChunkGenerator {
    private static final List<BlockState> ALL_BLOCKS = StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap((block) -> block.getStateDefinition().getPossibleStates().stream()).toList();

    public static final Codec<CelledSpaceGenerator> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ChunkGenerator.CODEC.fieldOf("delegate").forGetter(o -> o.delegate),
            Matrix3x3.CODEC.fieldOf("transformation").orElseGet(() -> Matrix3x3.identityScaled(256)).forGetter(o -> o.nodeMatrix),
            BlockPos.CODEC.fieldOf("unit_scale").forGetter(o -> o.unitScale),
            SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(o -> o.ruleSource),
            BlockState.CODEC.listOf().fieldOf("blockstates").forGetter(o -> o.blocks)
    ).apply(inst, CelledSpaceGenerator::new));

    // No big deal if this gets wiped as it's deterministic. Probably for the best because particular elements will not be called again when
    private final ConcurrentHashMap<BlockPos.MutableBlockPos, DelaunayVector> cells = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BlockPos.MutableBlockPos, VoronoiCell> voronoiCells = new ConcurrentHashMap<>();

    private final SurfaceRules.RuleSource ruleSource;
    private final List<BlockState> blocks;
    private final Matrix3x3 nodeMatrix;
    private final BlockPos unitScale;
    private final int scanRadius;
    private final float distributionRadius;
    private final SimplexNoise noise;

    @SuppressWarnings("FieldCanBeLocal")
    private final int SPAN = 1; // TODO Config

    //private final float halfUnitX;
    private final float halfUnitY;
    //private final float halfUnitZ;

    private static final BlockState EMPTY_DEFAULT = Blocks.AIR.defaultBlockState();

    public CelledSpaceGenerator(ChunkGenerator delegate, Matrix3x3 matrix3f, BlockPos unitScale, SurfaceRules.RuleSource ruleSource, List<BlockState> blocks) {
        super(delegate, delegate.getBiomeSource(), delegate.getSettings(), delegate.strongholdSeed);
        this.nodeMatrix = matrix3f;
        this.unitScale = unitScale;
        this.ruleSource = ruleSource;
        this.blocks = blocks;
        this.scanRadius = 1; // TODO Config?

        this.distributionRadius = 0.5f;

        //this.halfUnitX = this.unitScale.getX() / 2f;
        this.halfUnitY = this.unitScale.getY() / 2f;
        //this.halfUnitZ = this.unitScale.getZ() / 2f;

        this.noise = new SimplexNoise(new XoroshiroRandomSource(this.strongholdSeed));
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("aether_vector_fill", () -> this.doFill(blender, structureFeatureManager, chunkAccess)), executor);
    }

    @SuppressWarnings("DuplicatedCode")
    private ChunkAccess doFill(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
        Beardifier beardifier = new Beardifier(structureFeatureManager, chunk);

        /*if (this.delegate instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            int minimum = Math.max(0, chunk.getMinBuildHeight());
            int maximum = Math.min(128, chunk.getMaxBuildHeight());
            int k = Mth.intFloorDiv(minimum, QuartPos.toBlock(1));
            int l = Mth.intFloorDiv(maximum - minimum, QuartPos.toBlock(1));

            noiseBasedChunkGenerator.doFill(blender, structureFeatureManager, chunk, k, l);
        }*/

        ChunkPos chunkPos = chunk.getPos();

        int xPos = chunkPos.getMinBlockX();
        int zPos = chunkPos.getMinBlockZ();

        float nodeMinimumX = this.nodeMatrix.multiplyXRow(xPos, chunk.getMinBuildHeight(), zPos) / (float) this.unitScale.getX();
        float nodeMinimumY = this.nodeMatrix.multiplyYRow(xPos, chunk.getMinBuildHeight(), zPos) / (float) this.unitScale.getY();
        float nodeMinimumZ = this.nodeMatrix.multiplyZRow(xPos, chunk.getMinBuildHeight(), zPos) / (float) this.unitScale.getZ();

        BlockPos origin = new BlockPos(nodeMinimumX, nodeMinimumY, nodeMinimumZ);

        int nodeSpanX = Mth.ceil(this.nodeMatrix.multiplyXRow(xPos + 15, chunk.getMaxBuildHeight() - 1, zPos + 15) / (float) this.unitScale.getX()) - origin.getX();
        int nodeSpanY = Mth.ceil(this.nodeMatrix.multiplyYRow(xPos + 15, chunk.getMaxBuildHeight() - 1, zPos + 15) / (float) this.unitScale.getY()) - origin.getY();
        int nodeSpanZ = Mth.ceil(this.nodeMatrix.multiplyZRow(xPos + 15, chunk.getMaxBuildHeight() - 1, zPos + 15) / (float) this.unitScale.getZ()) - origin.getZ();

        //final Xoroshiro128PlusPlus random = new Xoroshiro128PlusPlus(((long) zPos << 32) | xPos, ((long) xPos << 32) | zPos);

        Sector<DelaunayVector> offsetSector = new Sector<>(nodeSpanX, nodeSpanY, nodeSpanZ, origin, this.scanRadius + 1, DelaunayVector[]::new, this::getOrCreateDelaunayVector);
        Sector<VoronoiCell> voronoiSector = new Sector<>(offsetSector, 1, VoronoiCell[]::new, this::getOrCreateVoronoiCell);

        Vector3f pooled0 = new Vector3f();
        Vector3f pooled1 = new Vector3f();
        Vector3f pooled2 = new Vector3f();
        Heightmap oceanHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    this.place(x, y, z, this.test(xPos + x, (int) (y - this.halfUnitY), zPos + z, (float) beardifier.calculateNoise(x, y, z), pooled0, pooled1, pooled2, offsetSector), oceanHeightmap, worldHeightmap, chunk);
                }
            }
        }

        //for (int z = 0; z < 16; z++) {
        //    for (int x = 0; x < 16; x++) {
        //        this.place(x, 0, z, this.getBlock(this.getNoiseBiome((xPos + x) >> 2, 64 >> 2, (zPos + z) >> 2).getRegistryName()), oceanHeightmap, worldHeightmap, chunk);
        //    }
        //}

        return chunk;
    }

    private DelaunayVector getOrCreateDelaunayVector(BlockPos.MutableBlockPos indexPosition) {
        return this.cells.computeIfAbsent(indexPosition, this::withRandomOffset);
    }

    private VoronoiCell getOrCreateVoronoiCell(Sector<DelaunayVector> sector, DelaunayVector center, BlockPos.MutableBlockPos indexPosition) {
        return this.voronoiCells.computeIfAbsent(indexPosition, elementPos -> this.buildVoronoiCell(sector, center, elementPos));
    }

    private DelaunayVector withRandomOffset(Vec3i unit) {
        // https://stackoverflow.com/a/5408843
        float phi = (float) this.noise.getValue(unit.getX(), unit.getY() + 1000, unit.getZ() - 1000);
        float cosTheta = (float) this.noise.getValue(unit.getX() - 2000, unit.getY(), unit.getZ() + 2000);

        float theta = (float) Math.acos(cosTheta);
        float r = (float) (this.distributionRadius * Math.cbrt(this.noise.getValue(unit.getX() + 3000, unit.getY() - 3000, unit.getZ())));

        float sinTheta = Mth.sin(theta);

        return new DelaunayVector(unit.getX() + r * sinTheta * Mth.cos(phi), unit.getY() + r * cosTheta, unit.getZ() + r * sinTheta * Mth.sin(phi));

        //return new Vector3f(unit.getX(), unit.getY(), unit.getZ());
    }

    public VoronoiCell buildVoronoiCell(Sector<DelaunayVector> parent, DelaunayVector center, BlockPos.MutableBlockPos indexPosition) {
        final int x = indexPosition.getX();
        final int y = indexPosition.getY();
        final int z = indexPosition.getZ();

        ArrayList<DelaunayVector> collector = new ArrayList<>();

        for (int deltaZ = 1; deltaZ <= this.scanRadius; deltaZ++) {
            for (int deltaX = 0; deltaX <= this.scanRadius; deltaX++) {
                this.tryAddVector(collector, parent.get(indexPosition.set(x + deltaX + 1, y + 1, z + deltaZ + 1)), center);
                this.tryAddVector(collector, parent.get(indexPosition.set(x - deltaX + 1, y + 1, z - deltaZ + 1)), center);
                this.tryAddVector(collector, parent.get(indexPosition.set(z - deltaZ + 1, y + 1, x + deltaX + 1)), center);
                this.tryAddVector(collector, parent.get(indexPosition.set(z + deltaZ + 1, y + 1, x - deltaX + 1)), center);

                for (int deltaY = 1; deltaY <= this.scanRadius; deltaY++) {
                    this.tryAddVector(collector, parent.get(indexPosition.set(x + deltaX + 1, y - deltaY + 1, z + deltaZ + 1)), center);
                    this.tryAddVector(collector, parent.get(indexPosition.set(x - deltaX + 1, y - deltaY + 1, z - deltaZ + 1)), center);
                    this.tryAddVector(collector, parent.get(indexPosition.set(z - deltaZ + 1, y - deltaY + 1, x + deltaX + 1)), center);
                    this.tryAddVector(collector, parent.get(indexPosition.set(z + deltaZ + 1, y - deltaY + 1, x - deltaX + 1)), center);

                    this.tryAddVector(collector, parent.get(indexPosition.set(x + deltaX + 1, y + deltaY + 1, z + deltaZ + 1)), center);
                    this.tryAddVector(collector, parent.get(indexPosition.set(x - deltaX + 1, y + deltaY + 1, z - deltaZ + 1)), center);
                    this.tryAddVector(collector, parent.get(indexPosition.set(z - deltaZ + 1, y + deltaY + 1, x + deltaX + 1)), center);
                    this.tryAddVector(collector, parent.get(indexPosition.set(z + deltaZ + 1, y + deltaY + 1, x - deltaX + 1)), center);
                }
            }
        }

        indexPosition.set(x, y, z);

        return new VoronoiCell(center.x(), center.y(), center.z(), collector.toArray(DelaunayVector[]::new));
    }

    private void tryAddVector(ArrayList<DelaunayVector> collector, DelaunayVector toAdd, DelaunayVector center) {
        this.tryAddVector(collector, toAdd.x() - center.x(), toAdd.y() - center.y(), toAdd.z() - center.z());
    }

    private void tryAddVector(ArrayList<DelaunayVector> collector, float dX, float dY, float dZ) {
        ArrayList<DelaunayVector> boundedOff = new ArrayList<>();

        // Here's an interactive demonstration on Desmos that explains how this algorithm works:
        //  https://www.desmos.com/calculator/udres0ksso
        float newLength = dX * dX + dY * dY + dZ * dZ;

        for (int i = collector.size() - 1; i >= 0; i--) {
            DelaunayVector vec = collector.get(i);
            float bound = vec.x() * dX + vec.y() * dY + vec.z() * dZ;

            if (bound > newLength)
                boundedOff.add(vec);
            else if (bound > vec.lengthSquared())
                return; // The vector we want to add is bounded off by a pre-existing vector
        }

        collector.removeAll(boundedOff);
        collector.add(new DelaunayVector(dX, dY, dZ));
    }

    protected BlockState test(int blockX, int blockY, int blockZ, float structureContribution, Vector3f pooled0, Vector3f pooled1, Vector3f pooled2, Sector<DelaunayVector> cellGenerators) {
        // Get transformed space coordinates. This is before the modulo-esque behavior happens.
        final float nodeSpaceX = this.nodeMatrix.multiplyXRow(blockX, blockY, blockZ) / (float) this.unitScale.getX();
        final float nodeSpaceY = this.nodeMatrix.multiplyYRow(blockX, blockY, blockZ) / (float) this.unitScale.getY();
        final float nodeSpaceZ = this.nodeMatrix.multiplyZRow(blockX, blockY, blockZ) / (float) this.unitScale.getZ();

        pooled0.set(nodeSpaceX, nodeSpaceY, nodeSpaceZ);

        // Find the nearest cell
        DelaunayVector cellGenerator = cellGenerators.getNearest(pooled0, DelaunayVector::distanceSquared);

        return this.getBlockI(cellGenerator.hashCode());

        /*final float relativeX = this.nodeMatrix.multiplyXRow(blockX, blockY, blockZ) / (float) this.unitScale.getX();
        final float relativeY = this.nodeMatrix.multiplyYRow(blockX, blockY, blockZ) / (float) this.unitScale.getY();
        final float relativeZ = this.nodeMatrix.multiplyZRow(blockX, blockY, blockZ) / (float) this.unitScale.getZ();

        if (!voronoiCell.reconstructNearestSpace(relativeX, relativeY, relativeZ, pooled0, pooled1, pooled2))
            // If that returns false, then it means it failed to build its inner space!
            return Blocks.AIR.defaultBlockState();

        // Subtract the cell's center from our spacial value to get spatial position relative to cell's center
        //float rX = Math.abs(nodeSpaceX - voronoiCell.xCenter());// * 2f;//Math.abs((nodeSpaceX - voronoiCell.xCenter() - 0.5f) * 2f);
        //float rY = Math.abs(nodeSpaceY - voronoiCell.yCenter());// * 2f;//Math.abs((nodeSpaceY - voronoiCell.yCenter() - 0.5f) * 2f);
        //float rZ = Math.abs(nodeSpaceZ - voronoiCell.zCenter());// * 2f;//Math.abs((nodeSpaceZ - voronoiCell.zCenter() - 0.5f) * 2f);

        float rX = MathUtil.multiplyXRow(relativeX, relativeY, relativeZ, pooled0, pooled1, pooled2);
        float rY = MathUtil.multiplyYRow(relativeX, relativeY, relativeZ, pooled0, pooled1, pooled2);
        float rZ = MathUtil.multiplyZRow(relativeX, relativeY, relativeZ, pooled0, pooled1, pooled2);

        //float fraction = rX * rY + rX * rZ + rY * rZ - structureContribution * 25;
        float fraction = rX * rX + rY * rY + rZ * rZ - structureContribution * 25;

        //return fraction <= 1f ? this.getBlockF(fraction) : EMPTY_DEFAULT;
        return this.getBlockF(fraction);*/
    }

    //@Deprecated // Debug
    //private BlockState getBlock(ResourceLocation biome) {
    //    if (AetherBiomeKeys.UNDERGROUND.location().equals(biome)) return Blocks.GRAY_STAINED_GLASS.defaultBlockState();
    //    if (AetherBiomeKeys.SKYWOOD_GROVE.location().equals(biome)) return Blocks.RED_STAINED_GLASS.defaultBlockState();
    //    if (AetherBiomeKeys.SKYWOOD_FOREST.location().equals(biome)) return Blocks.LIME_STAINED_GLASS.defaultBlockState();
    //    if (AetherBiomeKeys.SKYWOOD_THICKET.location().equals(biome)) return Blocks.BLUE_STAINED_GLASS.defaultBlockState();
    //    if (AetherBiomeKeys.GOLDEN_FOREST.location().equals(biome)) return Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
    //    return Blocks.RED_STAINED_GLASS.defaultBlockState();
    //}

    protected BlockState getBlockF(float fractional) {
        //if (fractional < 0 || this.blocks.isEmpty()) return EMPTY_DEFAULT;
        return this.getBlockI((int) (fractional * this.blocks.size()));
    }

    protected BlockState getBlockI(int index) {
        //return this.blocks.get(Mth.clamp(index, 0, this.blocks.size() - 1));
        return this.blocks.get(Mth.positiveModulo(index, this.blocks.size()));
    }

    protected void place(int blockX, int blockY, int blockZ, BlockState state, Heightmap oceanHeightmap, Heightmap worldHeightmap, ChunkAccess chunk) {
        chunk.setBlockState(new BlockPos(blockX, blockY, blockZ), state, false);
        oceanHeightmap.update(blockX, blockY, blockZ, state);
        worldHeightmap.update(blockX, blockY, blockZ, state);
    }



    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
