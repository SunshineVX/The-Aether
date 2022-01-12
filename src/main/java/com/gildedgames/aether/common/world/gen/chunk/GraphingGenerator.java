package com.gildedgames.aether.common.world.gen.chunk;

import com.gildedgames.aether.common.registry.AetherBiomeKeys;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class GraphingGenerator extends DelegatedChunkGenerator {
    public static final Codec<GraphingGenerator> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ChunkGenerator.CODEC.fieldOf("delegate").forGetter(o -> o.delegate),
            BlockState.CODEC.listOf().fieldOf("solid_blocks").forGetter(o -> o.blocks),
            BlockState.CODEC.listOf().fieldOf("glass_blocks").forGetter(o -> o.glassBlocks)
    ).apply(inst, GraphingGenerator::new));

    private final ConcurrentHashMap<BlockPos, BlockPos> nodes = new ConcurrentHashMap<>();

    private final List<BlockState> blocks;
    private final List<BlockState> glassBlocks;

    @SuppressWarnings("FieldCanBeLocal")
    private final int SPAN = 1; // TODO Config

    private static final BlockState EMPTY_DEFAULT = Blocks.AIR.defaultBlockState();

    public GraphingGenerator(ChunkGenerator delegate, List<BlockState> blocks, List<BlockState> glassBlocks) {
        super(delegate, delegate.getBiomeSource(), delegate.getSettings(), delegate.strongholdSeed);
        this.blocks = blocks;
        this.glassBlocks = glassBlocks;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("aether_debug_graph_fill", () -> this.doFill(blender, structureFeatureManager, chunkAccess)), executor);
    }

    private ChunkAccess doFill(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunk) {
        Beardifier beardifier = new Beardifier(structureFeatureManager, chunk);

        if (this.delegate instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            int minimum = Math.max(0, chunk.getMinBuildHeight());
            int maximum = Math.min(128, chunk.getMaxBuildHeight());
            int k = Mth.intFloorDiv(minimum, QuartPos.toBlock(1));
            int l = Mth.intFloorDiv(maximum - minimum, QuartPos.toBlock(1));

            noiseBasedChunkGenerator.doFill(blender, structureFeatureManager, chunk, k, l);
        }

        Heightmap oceanHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        ChunkPos chunkPos = chunk.getPos();
        int xPos = chunkPos.getMinBlockX();
        int zPos = chunkPos.getMinBlockZ();

        /*for (int y = chunk.getMaxBuildHeight() - 1; y >= chunk.getMinBuildHeight(); y--) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = this.getBlockI((int) this.climateSampler().sample(xPos + x, y, zPos + z).continentalness());

                    //this.place(x, y, z, this.test(xPos + x, y, zPos + z, beardifier), oceanHeightmap, worldHeightmap, chunk);
                    //BlockState state = this.getBlock(this.getNoiseBiome(xPos + x, y, zPos + z)); // this.getBlockI(chunk.getNoiseBiome(xPos + x, y, zPos + z).hashCode())

                    this.place(x, y, z, state, oceanHeightmap, worldHeightmap, chunk);
                }
            }
        }*/

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                //int y = oceanHeightmap.getHighestTaken(x, z);

                //this.place(x, y, z, Blocks.CLAY.defaultBlockState(), oceanHeightmap, worldHeightmap, chunk);
                //this.place(x, y - 1, z, Blocks.CLAY.defaultBlockState(), oceanHeightmap, worldHeightmap, chunk);
                //this.place(x, y - 2, z, Blocks.CLAY.defaultBlockState(), oceanHeightmap, worldHeightmap, chunk);
                //this.place(x, y - 3, z, Blocks.CLAY.defaultBlockState(), oceanHeightmap, worldHeightmap, chunk);

                var sample = this.climateSampler().sample((xPos + x) >> 2, 0, (zPos + z) >> 2);
                this.placeBiomeGraph(chunk, oceanHeightmap, worldHeightmap, x, z, sample.weirdness(), 0);
            }
        }

        return chunk;
    }

    @SuppressWarnings("SameParameterValue")
    private void placeBiomeGraph(ChunkAccess chunk, Heightmap oceanHeightmap, Heightmap worldHeightmap, int x, int z, long noise, int elevation) {
        float biomeF = (Climate.unquantizeCoord(noise) + 1f) * 0.5f;

        var graphState = this.getBlockF(biomeF);
        var glassState = this.glassBlocks.get((int) Mth.clamp(biomeF * this.blocks.size(), 0, this.blocks.size() - 1));

        int yModification = elevation + (int) (biomeF * 40f);

        this.place(x, elevation + 20, z, glassState, oceanHeightmap, worldHeightmap, chunk);
        this.place(x, elevation + yModification, z, (x == 0 || z == 0) ? (yModification < 20 ? Blocks.BLACK_STAINED_GLASS.defaultBlockState() : glassState) : graphState, oceanHeightmap, worldHeightmap, chunk);

        this.place(x, elevation + yModification - 1, z, graphState, oceanHeightmap, worldHeightmap, chunk);
        this.place(x, elevation + yModification - 2, z, graphState, oceanHeightmap, worldHeightmap, chunk);
        this.place(x, elevation + yModification - 3, z, graphState, oceanHeightmap, worldHeightmap, chunk);
        this.place(x, elevation + yModification - 4, z, graphState, oceanHeightmap, worldHeightmap, chunk);
    }
    
    @Deprecated // Debug
    private BlockState getBlock(Biome biome) {
        if (AetherBiomeKeys.UNDERGROUND.location().equals(biome.getRegistryName())) return Blocks.GRAY_STAINED_GLASS.defaultBlockState();
        if (AetherBiomeKeys.SKYWOOD_GROVE.location().equals(biome.getRegistryName())) return Blocks.LIME_STAINED_GLASS.defaultBlockState();
        if (AetherBiomeKeys.SKYWOOD_FOREST.location().equals(biome.getRegistryName())) return Blocks.GREEN_STAINED_GLASS.defaultBlockState();
        if (AetherBiomeKeys.SKYWOOD_THICKET.location().equals(biome.getRegistryName())) return Blocks.YELLOW_STAINED_GLASS.defaultBlockState();

        return Blocks.RED_STAINED_GLASS.defaultBlockState();
    }

    protected BlockState getBlockF(float fractional) {
        //if (fractional < 0 || this.blocks.isEmpty()) return EMPTY_DEFAULT;
        return this.getBlockI((int) (fractional * this.blocks.size()));
    }

    protected BlockState getBlockI(int index) {
        return this.blocks.get(Mth.clamp(index, 0, this.blocks.size() - 1));
    }

    protected void place(int blockX, int blockY, int blockZ, BlockState state, Heightmap oceanHeightmap, Heightmap worldHeightmap, ChunkAccess chunk) {
        chunk.setBlockState(new BlockPos(blockX, blockY, blockZ), state, false);
        oceanHeightmap.update(blockX, blockY, blockZ, state);
        worldHeightmap.update(blockX, blockY, blockZ, state);
    }

    // FIXME Very placeholder-y
    @SuppressWarnings({"ConstantConditions", "unused", "RedundantSuppression"})
    protected BlockPos getNearestNode(float vX, float vY, float vZ) {
        // TODO y-storage projection
        // FIXME Mutable BlockPos instead of new BlockPos constantly (not good)
        // TODO Vec3i (mutable) -> Node pooling system
        return this.nodes.computeIfAbsent(new BlockPos(vX, vY, vZ), vec3i -> vec3i);
    }

    // (Mth.positiveModulo(x, this.blocksToUnitScale) - this.blockHalfScale) / (float) this.blockHalfScale

    //protected int nearestXZ(int xzPos) {
    //    return xzPos / this.blocksToUnitScale;
    //}

    //protected int nearestY(int yPos) {
    //    return Mth.clamp(0, yPos / this.verticalScale, this.verticalUnitSpan - 1);
    //}

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
