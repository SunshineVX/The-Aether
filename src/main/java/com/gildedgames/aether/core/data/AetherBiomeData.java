package com.gildedgames.aether.core.data;

import com.gildedgames.aether.common.registry.AetherBiomeKeys;
import com.gildedgames.aether.core.data.provider.AetherBiomeProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

// PackageLocal: This class should never be accessed outside DataGen
class AetherBiomeData {
    final static Biome UNDERGROUND = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
    ).setRegistryName(AetherBiomeKeys.UNDERGROUND.location());

    final static Biome SPARSE_FOREST = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
    ).setRegistryName(AetherBiomeKeys.SPARSE_FOREST.location());

    final static Biome SKYWOOD_FOREST = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
            //.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureData.SKYROOT_TREE_FEATURE)
    ).setRegistryName(AetherBiomeKeys.SKYWOOD_FOREST.location());

    final static Biome CRAMPED_FOREST = AetherBiomeProvider.makeDefaultBiome(new BiomeGenerationSettings.Builder()
            //.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureData.SKYROOT_TREE_FEATURE)
            //.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AetherFeatureData.GOLDEN_OAK_TREE_FEATURE)
    ).setRegistryName(AetherBiomeKeys.CRAMPED_FOREST.location());

    final static ImmutableList<Biome> ALL_AETHER_BIOMES = ImmutableList.<Biome>builder().add(
            UNDERGROUND,
            SPARSE_FOREST,
            SKYWOOD_FOREST,
            CRAMPED_FOREST
    ).build();
}
