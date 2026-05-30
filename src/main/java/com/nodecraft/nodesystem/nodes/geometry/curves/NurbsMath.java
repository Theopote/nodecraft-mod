package com.nodecraft.nodesystem.nodes.geometry.curves;

import net.minecraft.util.math.Vec3d;

import java.util.List;

final class NurbsMath {

    private NurbsMath() {
    }

    static double[] buildClampedUniformKnots(int knotCount, int degree, int n) {
        double[] knots = new double[knotCount];
        int last = knotCount - 1;
        int domainEnd = n - degree + 1;

        for (int i = 0; i < knotCount; i++) {
            if (i <= degree) {
                knots[i] = 0.0d;
            } else if (i >= n + 1) {
                knots[i] = domainEnd;
            } else {
                knots[i] = i - degree;
            }
        }

        knots[last] = domainEnd;
        return knots;
    }

    static double basis(int i, int degree, double u, double[] knots) {
        if (degree == 0) {
            return (knots[i] <= u && u < knots[i + 1]) ? 1.0d : 0.0d;
        }

        double leftDenominator = knots[i + degree] - knots[i];
        double rightDenominator = knots[i + degree + 1] - knots[i + 1];

        double left = 0.0d;
        double right = 0.0d;

        if (leftDenominator > 0.0d) {
            left = ((u - knots[i]) / leftDenominator) * basis(i, degree - 1, u, knots);
        }
        if (rightDenominator > 0.0d) {
            right = ((knots[i + degree + 1] - u) / rightDenominator) * basis(i + 1, degree - 1, u, knots);
        }

        return left + right;
    }

    static Vec3d evaluateBSpline(List<Vec3d> controlPoints, double[] knots, int degree, double u, int n) {
        if (u >= knots[n + 1]) {
            Vec3d end = controlPoints.get(n);
            return new Vec3d(end.x, end.y, end.z);
        }

        double x = 0.0d;
        double y = 0.0d;
        double z = 0.0d;

        for (int i = 0; i <= n; i++) {
            double basis = basis(i, degree, u, knots);
            if (basis == 0.0d) {
                continue;
            }
            Vec3d point = controlPoints.get(i);
            x += basis * point.x;
            y += basis * point.y;
            z += basis * point.z;
        }

        return new Vec3d(x, y, z);
    }

    static Vec3d evaluateNurbs(List<Vec3d> controlPoints, List<Double> weights,
                               double[] knots, int degree, double u, int n,
                               double epsilon) {
        if (u >= knots[n + 1]) {
            Vec3d end = controlPoints.get(n);
            return new Vec3d(end.x, end.y, end.z);
        }

        double numeratorX = 0.0d;
        double numeratorY = 0.0d;
        double numeratorZ = 0.0d;
        double denominator = 0.0d;

        for (int i = 0; i <= n; i++) {
            double basis = basis(i, degree, u, knots);
            if (basis == 0.0d) {
                continue;
            }
            double weightedBasis = basis * weights.get(i);
            Vec3d point = controlPoints.get(i);
            numeratorX += weightedBasis * point.x;
            numeratorY += weightedBasis * point.y;
            numeratorZ += weightedBasis * point.z;
            denominator += weightedBasis;
        }

        if (denominator <= epsilon) {
            Vec3d fallback = controlPoints.get(0);
            return new Vec3d(fallback.x, fallback.y, fallback.z);
        }

        return new Vec3d(numeratorX / denominator, numeratorY / denominator, numeratorZ / denominator);
    }
}