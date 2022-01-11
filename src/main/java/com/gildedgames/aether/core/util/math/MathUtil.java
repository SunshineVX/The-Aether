package com.gildedgames.aether.core.util.math;

import net.minecraft.world.phys.Vec2;

public final class MathUtil {
    // Error tolerance for precision. We can afford to be pretty loose here because this is used for approximation
    private static final float ERROR = 0.001f;

    // https://iquilezles.org/www/articles/ibilinear/ibilinear.htm
    // This is really just inverse bilinear interpolation
    public static Vec2 bilinearInterpolationInverse(Vec2 p, Vec2 v00, Vec2 v10, Vec2 v01, Vec2 v11) {
        Vec2 xBase = subtract(v10, v00);
        Vec2 yBase = subtract(v11, v00);
        Vec2 dBase = subtract(subtract(v01, v11), xBase);
        Vec2 h = subtract(p, v00);

        // Polynomials
        float a = determinant(dBase, yBase);
        float b = determinant(xBase, yBase) + determinant(h, dBase);
        float c = determinant(h, xBase);

        // Short-circuit: Use a linear equation instead
        if (Math.abs(a) < ERROR) return new Vec2((h.x * b + yBase.x * c) / (xBase.x * b - dBase.x * c), -c / b);

        float w = b * b - 4.0f * c * a;
        if (w < 0.0) return new Vec2(-1, -1);

        float ik2 = 0.5f / a;
        float v = (-b - w) * ik2;
        float u = (h.x - yBase.x * v) / (xBase.x + dBase.x * v);

        if( u < 0.0f || u > 1.0f || v < 0.0f || v > 1.0f ) {
            v = (-b + w) * ik2;
            u = (h.x - yBase.x * v) / (xBase.x + dBase.x * v);
        }

        return new Vec2(u, v);
    }

    public static Vec2 subtract(Vec2 value, Vec2 subtrahend) {
        return new Vec2(value.x - subtrahend.x, value.y - subtrahend.y);
    }

    // This is like getting the determinant of a 2x2 matrix except using column vectors directly
    // https://en.wikipedia.org/wiki/Determinant
    public static float determinant(Vec2 a, Vec2 b) {
        return a.x * b.y - a.y * b.x;
    }

    private MathUtil() {
    }
}
