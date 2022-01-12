package com.gildedgames.aether.common.registry;

import com.gildedgames.aether.Aether;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class AetherBiomeKeys {
    public static final ResourceKey<Biome> UNDERGROUND = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Aether.MODID, "underground"));
    public static final ResourceKey<Biome> SKYWOOD_GROVE = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Aether.MODID, "skywood_grove"));
    public static final ResourceKey<Biome> SKYWOOD_FOREST = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Aether.MODID, "skywood_forest"));
    public static final ResourceKey<Biome> SKYWOOD_THICKET = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Aether.MODID, "skywood_thicket"));
    public static final ResourceKey<Biome> GOLDEN_FOREST = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Aether.MODID, "golden_forest"));
}
