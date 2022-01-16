package com.gildedgames.aether.core.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToDoubleBiFunction;

public class Sector<E> {
    public final int xSize;
    public final int ySize;
    public final int zSize;
    public final Vec3i origin;
    public final int padding;

    private final E[] elements;

    public Sector(int xSpan, int ySpan, int zSpan, Vec3i origin, int padding, IntFunction<E[]> arrayConstructor, Function<BlockPos.MutableBlockPos, E> storageBuilder) {
        this.xSize = xSpan + padding * 2;
        this.ySize = ySpan + padding * 2;
        this.zSize = zSpan + padding * 2;

        if (this.xSize < 1 || this.ySize < 1 || this.zSize < 1) throw new UnsupportedOperationException("Array will contain an empty dimension!");

        this.padding = padding;
        this.origin = origin.offset(-this.padding, -this.padding, -this.padding);

        int size = this.index(this.xSize, this.ySize, this.zSize) + 1;
        if (size < 1) throw new UnsupportedOperationException("Array will be empty!");

        this.elements = arrayConstructor.apply(size);

        BlockPos.MutableBlockPos storagePos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < this.zSize; z++)
            for (int y = 0; y < this.ySize; y++)
                for (int x = 0; x < this.xSize; x++)
                    this.elements[this.indexUnbounded(x, y, z)] = storageBuilder.apply(storagePos.setWithOffset(this.origin, x, y, z));
    }

    public <P> Sector(Sector<P> parent, int borderShrink, IntFunction<E[]> arrayConstructor, RemappingFunction<P, E> remapper) {
        this.xSize = parent.xSize - borderShrink * 2;
        this.ySize = parent.ySize - borderShrink * 2;
        this.zSize = parent.zSize - borderShrink * 2;

        if (this.xSize < 1 || this.ySize < 1 || this.zSize < 1) throw new UnsupportedOperationException("Array will contain an empty dimension!");

        this.origin = new BlockPos(parent.origin);
        this.padding = parent.padding - borderShrink;

        int size = this.index(this.xSize, this.ySize, this.zSize) + 1;
        if (size < 1) throw new UnsupportedOperationException("Array will be empty!");

        this.elements = arrayConstructor.apply(size);

        BlockPos.MutableBlockPos mutableElementPos = new BlockPos.MutableBlockPos();
        for (int z = 0; z < this.zSize; z++) {
            for (int y = 0; y < this.ySize; y++) {
                for (int x = 0; x < this.xSize; x++) {
                    mutableElementPos.set(x + borderShrink, y + borderShrink, z + borderShrink);
                    this.elements[this.indexUnbounded(x, y, z)] = remapper.remap(parent, parent.get(mutableElementPos), mutableElementPos);
                }
            }
        }
    }

    public <V> E getNearest(V v, ToDoubleBiFunction<E, V> toDistance) {
        E nearest = this.elements[0];
        double distance = toDistance.applyAsDouble(nearest, v);

        for (int i = 1; i < this.elements.length; i++) {
            double newDistance = toDistance.applyAsDouble(this.elements[i], v);

            if (newDistance < distance) {
                nearest = this.elements[i];
                distance = newDistance;
            }
        }

        return nearest;
    }

    public int index(int x, int y, int z) {
        return this.indexUnbounded(Mth.clamp(x, 0, this.xSize - 1), Mth.clamp(y, 0, this.ySize - 1), Mth.clamp(z, 0, this.zSize - 1));
    }

    private int indexUnbounded(int x, int y, int z) {
        return x + (this.xSize * y) + (this.xSize * this.ySize * z);
    }

    public E get(int index) {
        return this.elements[index];
    }

    public E get(Vec3i pos, int x, int y, int z) {
        return this.get(this.index(pos.getX() + x, pos.getY() + y, pos.getZ() + z));
    }

    public E get(int x, int y, int z) {
        return this.get(this.index(x, y, z));
    }

    public E get(Vec3i vec3i) {
        return this.get(this.index(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
    }

    public E getForUnitPosition(int x, int y, int z) {
        return this.get(this.origin.getX() + x + this.padding, this.origin.getY() + y + this.padding, this.origin.getZ() + z + this.padding);
    }

    public int flatSize() {
        return this.elements.length;
    }

    public int xSize() {
        return this.xSize;
    }

    public int ySize() {
        return this.ySize;
    }

    public int zSize() {
        return this.zSize;
    }

    @FunctionalInterface
    public interface RemappingFunction<A, B> {
        B remap(Sector<A> context, A a, BlockPos.MutableBlockPos elementPos);
    }
}
