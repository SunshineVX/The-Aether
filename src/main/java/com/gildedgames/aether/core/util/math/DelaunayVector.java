package com.gildedgames.aether.core.util.math;

import com.mojang.math.Vector3f;

import java.util.Objects;

public final class DelaunayVector {
    private final float x;
    private final float y;
    private final float z;
    private final float lengthSquared;

    public DelaunayVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.lengthSquared = x * x + y * y + z * z;
    }

    public static DelaunayVector fromVector3f(Vector3f vector3f) {
        return new DelaunayVector(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public static DelaunayVector fromVector3fOffset(Vector3f vector, float x, float y, float z) {
        return new DelaunayVector(vector.x() - x, vector.y() - y, vector.z() - z);
    }

    public void copyToMutable(Vector3f vec) {
        vec.set(this.x, this.y, this.z);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public float lengthSquared() {
        return this.lengthSquared;
    }

    public float distanceSquared(Vector3f vector) {
        float x = vector.x() - this.x;
        float y = vector.y() - this.y;
        float z = vector.z() - this.z;

        return x * x + y * y + z * z;
    }

    public float distanceSquared(float vectorX, float vectorY, float vectorZ) {
        float x = vectorX - this.x;
        float y = vectorY - this.y;
        float z = vectorZ - this.z;

        return x * x + y * y + z * z;
    }

    @Override
    public String toString() {
        return "DelaunayVector{" + "x=" + this.x + ", y=" + this.y + ", z=" + this.z + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DelaunayVector vector = (DelaunayVector) o;
        return Float.compare(vector.x, this.x) == 0 && Float.compare(vector.y, this.y) == 0 && Float.compare(vector.z, this.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
