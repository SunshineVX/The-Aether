package com.gildedgames.aether.core.data;

import com.gildedgames.aether.common.registry.AetherBiomeKeys;
import com.gildedgames.aether.common.registry.AetherBlocks;
import com.gildedgames.aether.core.data.provider.AetherBiomeProvider;
import com.gildedgames.aether.core.data.provider.AetherFeatureDataProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;

// PackageLocal: This class should never be accessed outside DataGen
class AetherBiomeData {
    final static Biome UNDERGROUND = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
    ).setRegistryName(AetherBiomeKeys.UNDERGROUND.location());

    // No fancy variations with trees are required, so inline these different tree decoration patterns instead
    final static Biome SKYWOOD_GROVE = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureDataProvider.treeBlendDensity(2))
    ).setRegistryName(AetherBiomeKeys.SKYWOOD_GROVE.location());

    final static Biome SKYWOOD_FOREST = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureDataProvider.treeBlendDensity(2))
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureData.SKYROOT_TREE_FEATURE_BASE.placed(
                    CountOnEveryLayerPlacement.of(1),
                    //BiomeFilter.biome(),
                    AetherFeatureDataProvider.copyBlockSurvivability(AetherBlocks.SKYROOT_SAPLING.get())
            ))
    ).setRegistryName(AetherBiomeKeys.SKYWOOD_FOREST.location());

    final static Biome SKYWOOD_THICKET = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureData.SKYROOT_TREE_FEATURE_BASE.placed(
                    CountOnEveryLayerPlacement.of(1),
                    //BiomeFilter.biome(),
                    AetherFeatureDataProvider.copyBlockSurvivability(AetherBlocks.SKYROOT_SAPLING.get())
            ))
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureDataProvider.treeBlendDensity(3))
    ).setRegistryName(AetherBiomeKeys.SKYWOOD_THICKET.location());

    final static Biome GOLDEN_FOREST = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureData.GOLDEN_OAK_FEATURE_BASE.placed(
                    CountOnEveryLayerPlacement.of(2),
                    //BiomeFilter.biome(),
                    AetherFeatureDataProvider.copyBlockSurvivability(AetherBlocks.GOLDEN_OAK_SAPLING.get())
            ))
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureDataProvider.treeBlendDensity(2))
    ).setRegistryName(AetherBiomeKeys.GOLDEN_FOREST.location());

    final static ImmutableList<Biome> ALL_AETHER_BIOMES = ImmutableList.<Biome>builder().add(
            UNDERGROUND,
            SKYWOOD_GROVE,
            SKYWOOD_FOREST,
            SKYWOOD_THICKET,
            GOLDEN_FOREST
    ).build();
}
