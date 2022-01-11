package com.gildedgames.aether.common.world.gen.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class VoronoiSamplingGenerator extends DelegatedChunkGenerator implements VoronoiSampler {
    public static final Codec<VoronoiSamplingGenerator> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ChunkGenerator.CODEC.fieldOf("delegate").forGetter(o -> o.delegate),
            Codec.LONG.fieldOf("seed").forGetter(o -> o.strongholdSeed)
    ).apply(inst, VoronoiSamplingGenerator::new));

    public VoronoiSamplingGenerator(ChunkGenerator delegate, long seed) {
        super(delegate, delegate.getBiomeSource(), delegate.getSettings(), seed);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
