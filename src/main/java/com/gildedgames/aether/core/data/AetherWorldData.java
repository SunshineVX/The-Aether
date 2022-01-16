package com.gildedgames.aether.core.data;

import com.gildedgames.aether.Aether;
import com.gildedgames.aether.common.block.state.properties.AetherBlockStateProperties;
import com.gildedgames.aether.common.registry.AetherBlocks;
import com.gildedgames.aether.common.world.gen.chunk.CelledSpaceGenerator;
import com.gildedgames.aether.core.data.provider.AetherWorldProvider;
import com.gildedgames.aether.core.util.math.Matrix3x3;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.Random;

public class AetherWorldData extends AetherWorldProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); // If desired, custom formatting rules can be set up here

    public AetherWorldData(DataGenerator generator) {
        super(generator, JsonOps.INSTANCE, GSON::toJson);
    }

    @Override
    public String getName() {
        return "Aether World Data";
    }

    @Override
    public void generate(RegistryAccess registryAccess) {
        // It if crashes on .get(), then you've got bigger problems than removing Optional here
        DimensionType dimensionType = registryAccess.registry(Registry.DIMENSION_TYPE_REGISTRY).map(reg -> Registry.register(reg, new ResourceLocation(Aether.MODID, "aether_type"), this.aetherDimensionType())).get();
        NoiseGeneratorSettings worldNoiseSettings = registryAccess.registry(BuiltinRegistries.NOISE_GENERATOR_SETTINGS.key()).map(reg -> Registry.register(reg, new ResourceLocation(Aether.MODID, "skyland_generation"), this.aetherNoiseSettings())).get();

        final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);

        // temperature
        // humidity
        // continentalness
        // erosion
        // depth
        // weirdness
        // offset
        BiomeSource biomes = new MultiNoiseBiomeSource(new Climate.ParameterList<>(List.of(
                //Pair.of(
                //        Climate.parameters(
                //                FULL_RANGE,
                //                FULL_RANGE,
                //                FULL_RANGE,
                //                FULL_RANGE,
                //                Climate.Parameter.span(-0.8f, 0.8f),
                //                FULL_RANGE,
                //                0f
                //        ), () -> AetherBiomeData.UNDERGROUND
                //),
                Pair.of(
                        new Climate.ParameterPoint(
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                Climate.Parameter.span(1f, 2f),
                                0
                        ), () -> AetherBiomeData.GOLDEN_FOREST
                ),
                Pair.of(
                        new Climate.ParameterPoint(
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                Climate.Parameter.span(0.5f, 1f),
                                0
                        ), () -> AetherBiomeData.SKYWOOD_FOREST
                ),
                Pair.of(
                        new Climate.ParameterPoint(
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                Climate.Parameter.span(-0.1f, 0.5f),
                                0
                        ), () -> AetherBiomeData.SKYWOOD_THICKET
                ),
                Pair.of(
                        new Climate.ParameterPoint(
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                Climate.Parameter.span(-0.7f, -0.1f),
                                0
                        ), () -> AetherBiomeData.SKYWOOD_FOREST
                ),
                Pair.of(
                        new Climate.ParameterPoint(
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                Climate.Parameter.span(-2f, -0.7f),
                                0
                        ), () -> AetherBiomeData.SKYWOOD_GROVE
                )
        )));

        // TODO Remove Seed from Datagen
        NoiseBasedChunkGenerator aetherChunkGen = new NoiseBasedChunkGenerator(RegistryAccess.builtin().registryOrThrow(Registry.NOISE_REGISTRY), biomes, new Random().nextLong(), () -> worldNoiseSettings);

        AetherBiomeData.ALL_AETHER_BIOMES.forEach(biome -> this.serialize(Registry.BIOME_REGISTRY, biome.getRegistryName(), biome, Biome.DIRECT_CODEC));

        //List<BlockState> states = List.of(
        //        Blocks.AMETHYST_BLOCK.defaultBlockState(),
        //        Blocks.LAPIS_BLOCK.defaultBlockState(),
        //        Blocks.DIAMOND_BLOCK.defaultBlockState(),
        //        Blocks.EMERALD_BLOCK.defaultBlockState(),
        //        Blocks.GOLD_BLOCK.defaultBlockState(),
        //        Blocks.REDSTONE_BLOCK.defaultBlockState(),
        //        AetherBlocks.HOLYSTONE.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true)
        //);

        List<BlockState> states = List.of(
                // -1.0
                Blocks.RED_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.ORANGE_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.LIME_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.GREEN_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                // -0.5
                Blocks.CYAN_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.BLUE_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.PURPLE_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.MAGENTA_WOOL.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                // SIGN BOUNDARY - 0.0
                Blocks.RED_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.ORANGE_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.YELLOW_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.LIME_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.GREEN_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                // 0.5
                Blocks.CYAN_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.BLUE_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.PURPLE_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                Blocks.MAGENTA_CONCRETE.defaultBlockState(),
                Blocks.AIR.defaultBlockState()
                // 1.0
        );

        List<BlockState> glassStates = List.of(
                // -1.0
                Blocks.RED_STAINED_GLASS.defaultBlockState(),
                Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
                Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
                Blocks.LIME_STAINED_GLASS.defaultBlockState(),
                Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
                // -0.5
                Blocks.CYAN_STAINED_GLASS.defaultBlockState(),
                Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
                Blocks.BLUE_STAINED_GLASS.defaultBlockState(),
                Blocks.PURPLE_STAINED_GLASS.defaultBlockState(),
                Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(),
                // SIGN BOUNDARY - 0.0 - DEEP UNDERGROUND
                Blocks.RED_STAINED_GLASS.defaultBlockState(),
                Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),
                Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),
                Blocks.LIME_STAINED_GLASS.defaultBlockState(),
                Blocks.GREEN_STAINED_GLASS.defaultBlockState(),
                // 0.5 - UNDERGROUND
                Blocks.CYAN_STAINED_GLASS.defaultBlockState(),
                Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(),
                Blocks.BLUE_STAINED_GLASS.defaultBlockState(),
                Blocks.PURPLE_STAINED_GLASS.defaultBlockState(),
                Blocks.MAGENTA_STAINED_GLASS.defaultBlockState()
                // 1.0 - Surface
        );

        List<BlockState> onlyHolystone = List.of(AetherBlocks.HOLYSTONE.get().defaultBlockState().setValue(AetherBlockStateProperties.DOUBLE_DROPS, true));

        //Matrix3x3 basis = Matrix3x3.identityScaled(1f).add(
        //        0, 0.25f, 0,
        //        0, 0, 0.0625f,
        //        0.125f, 0, 0
        //);

        Matrix3x3 basis = Matrix3x3.identityScaled(1f).add(
                0, 0, 0,
                0, 0, 0,
                0, 0, 0
        );

        CelledSpaceGenerator debug = new CelledSpaceGenerator(aetherChunkGen, basis, new BlockPos(64, 64, 64), this.aetherSurfaceRules(), states);
        //GraphingGenerator debug = new GraphingGenerator(aetherChunkGen, states, glassStates);

        this.serialize(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(Aether.MODID, "the_aether"), new LevelStem(() -> dimensionType, debug), LevelStem.CODEC);
    }
}