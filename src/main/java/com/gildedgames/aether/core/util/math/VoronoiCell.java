package com.gildedgames.aether.core.util.math;

import com.mojang.math.Vector3f;

import java.util.Arrays;
import java.util.Objects;

public record VoronoiCell(float xCenter, float yCenter, float zCenter, DelaunayVector... delaunayVectors) {
    public double distanceSquared(float vectorX, float vectorY, float vectorZ) {
        float x = vectorX - this.xCenter();
        float y = vectorY - this.yCenter();
        float z = vectorZ - this.zCenter();

        return x * x + y * y + z * z;
    }

    public double distanceSquared(Vector3f vector) {
        float x = vector.x() - this.xCenter();
        float y = vector.y() - this.yCenter();
        float z = vector.z() - this.zCenter();

        return x * x + y * y + z * z;
    }

    public boolean reconstructNearestSpace(float targetX, float targetY, float targetZ, Vector3f vector0, Vector3f vector1, Vector3f vector2) {
        if (this.delaunayVectors.length < 3) return false; // Failed, space will not be fully constructed!

        this.delaunayVectors[0].copyToMutable(vector0);

        float dist0 = this.delaunayVectors[0].distanceSquared(targetX, targetY, targetZ);
        float dist1 = Float.MAX_VALUE;
        float dist2 = Float.MAX_VALUE;

        for (int i = 1; i < this.delaunayVectors.length; i++) {
            DelaunayVector sample = this.delaunayVectors[i];
            float dist = sample.distanceSquared(targetX, targetY, targetZ);

            if (dist < dist0) {
                dist2 = dist1;
                dist1 = dist0;
                dist0 = dist;

                vector2.set(vector1.x(), vector1.y(), vector1.z());
                vector1.set(vector0.x(), vector0.y(), vector0.z());
                sample.copyToMutable(vector0);
            } else if (dist < dist1) {
                dist2 = dist1;
                dist1 = dist;

                vector2.set(vector1.x(), vector1.y(), vector1.z());
                sample.copyToMutable(vector1);
            } else if (dist < dist2) {
                dist2 = dist;

                sample.copyToMutable(vector2);
            }
        }

        // Did dist2 change at all? The value changing indicates that it has been overwritten
        return dist2 < Float.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        VoronoiCell that = (VoronoiCell) o;
        return Float.compare(that.xCenter, this.xCenter) == 0 && Float.compare(that.yCenter, this.yCenter) == 0 && Float.compare(that.zCenter, this.zCenter) == 0 && Arrays.equals(this.delaunayVectors, that.delaunayVectors);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(this.xCenter, this.yCenter, this.zCenter);
        result = 31 * result + Arrays.hashCode(this.delaunayVectors);
        return result;
    }
}
