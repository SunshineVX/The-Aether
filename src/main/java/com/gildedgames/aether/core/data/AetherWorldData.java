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

import java.util.List;

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
                        Climate.parameters(
                                Climate.Parameter.span(-2, -0.2f),
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                0f
                        ), () -> AetherBiomeData.SPARSE_FOREST
                ),
                Pair.of(
                        Climate.parameters(
                                Climate.Parameter.span(-0.2f, 0.2f),
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                0f
                        ), () -> AetherBiomeData.SKYWOOD_FOREST
                ),
                Pair.of(
                        Climate.parameters(
                                Climate.Parameter.span(0.2f, 2),
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                FULL_RANGE,
                                0f
                        ), () -> AetherBiomeData.CRAMPED_FOREST
                )
        )));

        NoiseBasedChunkGenerator aetherChunkGen = new NoiseBasedChunkGenerator(RegistryAccess.builtin().registryOrThrow(Registry.NOISE_REGISTRY), biomes, 0L, () -> worldNoiseSettings);

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

        /*List<BlockState> states = List.of(
                Blocks.WHITE_WOOL.defaultBlockState(),
                Blocks.ORANGE_WOOL.defaultBlockState(),
                Blocks.MAGENTA_WOOL.defaultBlockState(),
                Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(),
                Blocks.LIME_WOOL.defaultBlockState(),
                Blocks.PINK_WOOL.defaultBlockState(),
                Blocks.GRAY_WOOL.defaultBlockState(),
                Blocks.LIGHT_GRAY_WOOL.defaultBlockState(),
                Blocks.CYAN_WOOL.defaultBlockState(),
                Blocks.PURPLE_WOOL.defaultBlockState(),
                Blocks.BLUE_WOOL.defaultBlockState(),
                Blocks.BROWN_WOOL.defaultBlockState(),
                Blocks.GREEN_WOOL.defaultBlockState(),
                Blocks.RED_WOOL.defaultBlockState(),
                Blocks.BLACK_WOOL.defaultBlockState(),

                //Blocks.OAK_PLANKS.defaultBlockState(),
                //Blocks.SPRUCE_PLANKS.defaultBlockState(),
                //Blocks.BIRCH_PLANKS.defaultBlockState(),
                //Blocks.JUNGLE_PLANKS.defaultBlockState(),
                //Blocks.ACACIA_PLANKS.defaultBlockState(),
                //Blocks.DARK_OAK_PLANKS.defaultBlockState(),
                //Blocks.CRIMSON_PLANKS.defaultBlockState(),
                //Blocks.WARPED_PLANKS.defaultBlockState(),

                //Blocks.WAXED_COPPER_BLOCK.defaultBlockState(),
                //Blocks.WAXED_EXPOSED_COPPER.defaultBlockState(),
                //Blocks.WAXED_WEATHERED_COPPER.defaultBlockState(),
                //Blocks.WAXED_OXIDIZED_COPPER.defaultBlockState(),

                Blocks.WHITE_CONCRETE.defaultBlockState(),
                Blocks.ORANGE_CONCRETE.defaultBlockState(),
                Blocks.MAGENTA_CONCRETE.defaultBlockState(),
                Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
                Blocks.YELLOW_CONCRETE.defaultBlockState(),
                Blocks.LIME_CONCRETE.defaultBlockState(),
                Blocks.PINK_CONCRETE.defaultBlockState(),
                Blocks.GRAY_CONCRETE.defaultBlockState(),
                Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(),
                Blocks.CYAN_CONCRETE.defaultBlockState(),
                Blocks.PURPLE_CONCRETE.defaultBlockState(),
                Blocks.BLUE_CONCRETE.defaultBlockState(),
                Blocks.BROWN_CONCRETE.defaultBlockState(),
                Blocks.GREEN_CONCRETE.defaultBlockState(),
                Blocks.RED_CONCRETE.defaultBlockState(),
                Blocks.BLACK_CONCRETE.defaultBlockState()//,

                //Blocks.OAK_WOOD.defaultBlockState(),
                //Blocks.SPRUCE_WOOD.defaultBlockState(),
                //Blocks.BIRCH_WOOD.defaultBlockState(),
                //Blocks.JUNGLE_WOOD.defaultBlockState(),
                //Blocks.ACACIA_WOOD.defaultBlockState(),
                //Blocks.DARK_OAK_WOOD.defaultBlockState(),
                //Blocks.CRIMSON_NYLIUM.defaultBlockState(),
                //Blocks.WARPED_NYLIUM.defaultBlockState(),

                //Blocks.WAXED_CUT_COPPER.defaultBlockState(),
                //Blocks.WAXED_EXPOSED_CUT_COPPER.defaultBlockState(),
                //Blocks.WAXED_WEATHERED_CUT_COPPER.defaultBlockState(),
                //Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState(),

                //Blocks.WHITE_TERRACOTTA.defaultBlockState(),
                //Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
                //Blocks.MAGENTA_TERRACOTTA.defaultBlockState(),
                //Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(),
                //Blocks.YELLOW_TERRACOTTA.defaultBlockState(),
                //Blocks.LIME_TERRACOTTA.defaultBlockState(),
                //Blocks.PINK_TERRACOTTA.defaultBlockState(),
                //Blocks.GRAY_TERRACOTTA.defaultBlockState(),
                //Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState(),
                //Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                //Blocks.PURPLE_TERRACOTTA.defaultBlockState(),
                //Blocks.BLUE_TERRACOTTA.defaultBlockState(),
                //Blocks.BROWN_TERRACOTTA.defaultBlockState(),
                //Blocks.GREEN_TERRACOTTA.defaultBlockState(),
                //Blocks.RED_TERRACOTTA.defaultBlockState(),
                //Blocks.BLACK_TERRACOTTA.defaultBlockState()
        );*/

        List<BlockState> states = List.of(
                // -1.0
                Blocks.RED_WOOL.defaultBlockState(),
                Blocks.ORANGE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(),
                Blocks.LIME_WOOL.defaultBlockState(),
                Blocks.GREEN_WOOL.defaultBlockState(),
                // -0.5
                Blocks.CYAN_WOOL.defaultBlockState(),
                Blocks.LIGHT_BLUE_WOOL.defaultBlockState(),
                Blocks.BLUE_WOOL.defaultBlockState(),
                Blocks.PURPLE_WOOL.defaultBlockState(),
                Blocks.MAGENTA_WOOL.defaultBlockState(),
                // SIGN BOUNDARY - 0.0
                Blocks.RED_CONCRETE.defaultBlockState(),
                Blocks.ORANGE_CONCRETE.defaultBlockState(),
                Blocks.YELLOW_CONCRETE.defaultBlockState(),
                Blocks.LIME_CONCRETE.defaultBlockState(),
                Blocks.GREEN_CONCRETE.defaultBlockState(),
                // 0.5
                Blocks.CYAN_CONCRETE.defaultBlockState(),
                Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState(),
                Blocks.BLUE_CONCRETE.defaultBlockState(),
                Blocks.PURPLE_CONCRETE.defaultBlockState(),
                Blocks.MAGENTA_CONCRETE.defaultBlockState()
                // 1.0

                //Blocks.RED_TERRACOTTA.defaultBlockState(),
                //Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
                //Blocks.YELLOW_TERRACOTTA.defaultBlockState(),
                //Blocks.LIME_TERRACOTTA.defaultBlockState(),
                //Blocks.GREEN_TERRACOTTA.defaultBlockState(),

                //Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                //Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState(),
                //Blocks.BLUE_TERRACOTTA.defaultBlockState(),
                //Blocks.PURPLE_TERRACOTTA.defaultBlockState(),
                //Blocks.MAGENTA_TERRACOTTA.defaultBlockState()
        );

        Matrix3x3 basis = Matrix3x3.identityScaled(1f).add(
                0, 0.25f, 0,
                0, 0, 0.0625f,
                0.125f, 0, 0
        );

        CelledSpaceGenerator debug = new CelledSpaceGenerator(aetherChunkGen, basis, new BlockPos(64, 64, 64), this.aetherSurfaceRules(), states);

        this.serialize(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(Aether.MODID, "the_aether"), new LevelStem(() -> dimensionType, debug), LevelStem.CODEC);
    }
}