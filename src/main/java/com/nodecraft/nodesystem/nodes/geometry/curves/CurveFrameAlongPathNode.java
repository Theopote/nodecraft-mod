package com.nodecraft.nodesystem.nodes.geometry.curves;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.LineData;
import com.nodecraft.nodesystem.datatypes.PlaneData;
import com.nodecraft.nodesystem.datatypes.PointData;
import com.nodecraft.nodesystem.datatypes.PolylineData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.Curve;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Samples a path and generates local frames (origin + axes) along it.
 */
@NodeInfo(
    id = "geometry.curves.frame_along_path",
    displayName = "Curve Frame Along Path",
    description = "Generates local frames along a curve/path using count or spacing, outputting origins, axes, and planes per sample",
    category = "geometry.curves",
    order = 15
)
public class CurveFrameAlongPathNode extends BaseNode {

    private static final double EPS = 1.0e-9d;

    private static final String INPUT_CURVE_ID = "input_curve";
    private static final String INPUT_POLYLINE_ID = "input_polyline";
    private static final String INPUT_LINE_ID = "input_line";
    private static final String INPUT_SPACING_ID = "input_spacing";
    private static final String INPUT_COUNT_ID = "input_count";
    private static final String INPUT_UP_VECTOR_ID = "input_up_vector";

    private static final String OUTPUT_ORIGINS_ID = "output_origins";
    private static final String OUTPUT_X_AXES_ID = "output_x_axes";
    private static final String OUTPUT_Y_AXES_ID = "output_y_axes";
    private static final String OUTPUT_Z_AXES_ID = "output_z_axes";
    private static final String OUTPUT_PLANES_ID = "output_planes";
    private static final String OUTPUT_TANGENTS_ID = "output_tangents";
    private static final String OUTPUT_POINTS_ID = "output_points";
    private static final String OUTPUT_COUNT_ID = "output_count";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public CurveFrameAlongPathNode() {
        super(UUID.randomUUID(), "geometry.curves.frame_along_path");

        addInputPort(new BasePort(INPUT_CURVE_ID, "Curve",
            "Curve to sample for frames", NodeDataType.CURVE, this));
        addInputPort(new BasePort(INPUT_POLYLINE_ID, "Polyline",
            "Fallback polyline when no curve is connected", NodeDataType.POLYLINE, this));
        addInputPort(new BasePort(INPUT_LINE_ID, "Line",
            "Fallback line when no curve/polyline is connected", NodeDataType.LINE, this));
        addInputPort(new BasePort(INPUT_SPACING_ID, "Spacing",
            "Target distance between samples (> 0 when used)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count",
            "Target sample count (>= 2). Overrides spacing when provided", NodeDataType.INTEGER, this));
        addInputPort(new BasePort(INPUT_UP_VECTOR_ID, "Up Vector",
            "Reference up vector used to stabilize frame orientation", NodeDataType.VECTOR, this));

        addOutputPort(new BasePort(OUTPUT_ORIGINS_ID, "Origins",
            "Frame origins as Vector3d list", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_X_AXES_ID, "X Axes",
            "Frame X axes (tangent) as Vector3d list", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_Y_AXES_ID, "Y Axes",
            "Frame Y axes (normal) as Vector3d list", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_Z_AXES_ID, "Z Axes",
            "Frame Z axes (binormal) as Vector3d list", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_PLANES_ID, "Planes",
            "PlaneData list built from each frame origin + Z axis", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_TANGENTS_ID, "Tangents",
            "Alias of X axes for path-direction workflows", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_POINTS_ID, "Points",
            "Frame origins as PointData list", NodeDataType.LIST, this));
        addOutputPort(new BasePort(OUTPUT_COUNT_ID, "Count",
            "Number of generated frames", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_LENGTH_ID, "Length",
            "Total path length used for sampling", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid",
            "True when frames were generated successfully", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Generates local frames along a curve/path using count or spacing, outputting origins, axes, and planes per sample";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        List<Vector3d> verts = resolveVertices();
        if (verts == null || verts.size() < 2) {
            writeInvalid();
            return;
        }

        boolean closed = isClosedPolyline(verts);
        List<Vector3d> unique = closed ? verts.subList(0, verts.size() - 1) : verts;
        if (unique.size() < 2) {
            writeInvalid();
            return;
        }

        double[] cumulative = buildCumulative(unique, closed);
        if (cumulative == null) {
            writeInvalid();
            return;
        }
        double total = cumulative[cumulative.length - 1];
        if (total <= EPS) {
            writeInvalid();
            return;
        }

        Object spacingObj = inputValues.get(INPUT_SPACING_ID);
        Object countObj = inputValues.get(INPUT_COUNT_ID);
        int count = countObj instanceof Number n ? n.intValue() : -1;
        double spacing = spacingObj instanceof Number s ? s.doubleValue() : 0.0d;

        List<Double> sampleDistances = new ArrayList<>();
        if (count >= 2) {
            for (int i = 0; i < count; i++) {
                sampleDistances.add(total * i / (double) (count - 1));
            }
        } else if (spacing > EPS) {
            for (double d = 0.0d; d <= total + EPS; d += spacing) {
                sampleDistances.add(Math.min(d, total));
            }
            if (sampleDistances.get(sampleDistances.size() - 1) < total - EPS) {
                sampleDistances.add(total);
            }
        } else {
            writeInvalid();
            return;
        }

        Vector3d up = resolveUpVector(inputValues.get(INPUT_UP_VECTOR_ID));
        List<Vector3d> origins = new ArrayList<>(sampleDistances.size());
        List<Vector3d> xAxes = new ArrayList<>(sampleDistances.size());
        List<Vector3d> yAxes = new ArrayList<>(sampleDistances.size());
        List<Vector3d> zAxes = new ArrayList<>(sampleDistances.size());
        List<PlaneData> planes = new ArrayList<>(sampleDistances.size());
        List<PointData> points = new ArrayList<>(sampleDistances.size());

        double delta = Math.max(total * 1.0e-4d, 1.0e-4d);
        for (double d : sampleDistances) {
            Vector3d origin = sampleAtDistance(unique, closed, cumulative, d);
            double backDistance = closed ? wrapDistance(d - delta, total) : Math.max(0.0d, d - delta);
            double forwardDistance = closed ? wrapDistance(d + delta, total) : Math.min(total, d + delta);

            Vector3d prev = sampleAtDistance(unique, closed, cumulative, backDistance);
            Vector3d next = sampleAtDistance(unique, closed, cumulative, forwardDistance);
            Vector3d tangent = new Vector3d(next).sub(prev);
            if (tangent.lengthSquared() <= EPS) {
                continue;
            }
            tangent.normalize();

            Vector3d binormal = new Vector3d(tangent).cross(up);
            if (binormal.lengthSquared() <= EPS) {
                Vector3d fallbackUp = Math.abs(tangent.y) < 0.9d
                    ? new Vector3d(0.0d, 1.0d, 0.0d)
                    : new Vector3d(1.0d, 0.0d, 0.0d);
                binormal = new Vector3d(tangent).cross(fallbackUp);
                if (binormal.lengthSquared() <= EPS) {
                    fallbackUp = new Vector3d(0.0d, 0.0d, 1.0d);
                    binormal = new Vector3d(tangent).cross(fallbackUp);
                }
            }
            if (binormal.lengthSquared() <= EPS) {
                continue;
            }
            binormal.normalize();

            Vector3d normal = new Vector3d(binormal).cross(tangent);
            if (normal.lengthSquared() <= EPS) {
                continue;
            }
            normal.normalize();

            origins.add(origin);
            xAxes.add(new Vector3d(tangent));
            yAxes.add(new Vector3d(normal));
            zAxes.add(new Vector3d(binormal));
            planes.add(new PlaneData(new Vector3d(origin), new Vector3d(binormal)));
            points.add(new PointData(origin.x, origin.y, origin.z));
        }

        if (origins.isEmpty()) {
            writeInvalid();
            return;
        }

        outputValues.put(OUTPUT_ORIGINS_ID, List.copyOf(origins));
        outputValues.put(OUTPUT_X_AXES_ID, List.copyOf(xAxes));
        outputValues.put(OUTPUT_Y_AXES_ID, List.copyOf(yAxes));
        outputValues.put(OUTPUT_Z_AXES_ID, List.copyOf(zAxes));
        outputValues.put(OUTPUT_PLANES_ID, List.copyOf(planes));
        outputValues.put(OUTPUT_TANGENTS_ID, List.copyOf(xAxes));
        outputValues.put(OUTPUT_POINTS_ID, List.copyOf(points));
        outputValues.put(OUTPUT_COUNT_ID, origins.size());
        outputValues.put(OUTPUT_LENGTH_ID, total);
        outputValues.put(OUTPUT_VALID_ID, true);
    }

    private void writeInvalid() {
        outputValues.put(OUTPUT_ORIGINS_ID, List.of());
        outputValues.put(OUTPUT_X_AXES_ID, List.of());
        outputValues.put(OUTPUT_Y_AXES_ID, List.of());
        outputValues.put(OUTPUT_Z_AXES_ID, List.of());
        outputValues.put(OUTPUT_PLANES_ID, List.of());
        outputValues.put(OUTPUT_TANGENTS_ID, List.of());
        outputValues.put(OUTPUT_POINTS_ID, List.of());
        outputValues.put(OUTPUT_COUNT_ID, 0);
        outputValues.put(OUTPUT_LENGTH_ID, 0.0d);
        outputValues.put(OUTPUT_VALID_ID, false);
    }

    private List<Vector3d> resolveVertices() {
        Object curveObj = inputValues.get(INPUT_CURVE_ID);
        Object polyObj = inputValues.get(INPUT_POLYLINE_ID);
        Object lineObj = inputValues.get(INPUT_LINE_ID);

        if (curveObj instanceof Curve curve) {
            List<Vec3d> pts = curve.getSamplePoints();
            if (pts.size() < 2) {
                return null;
            }
            List<Vector3d> out = new ArrayList<>(pts.size());
            for (Vec3d v : pts) {
                out.add(new Vector3d(v.x, v.y, v.z));
            }
            return out;
        }
        if (polyObj instanceof PolylineData poly) {
            List<Vec3d> pts = poly.getPoints();
            List<Vector3d> out = new ArrayList<>(pts.size());
            for (Vec3d v : pts) {
                out.add(new Vector3d(v.x, v.y, v.z));
            }
            return out;
        }
        if (lineObj instanceof LineData line) {
            Vec3d a = line.getStart();
            Vec3d b = line.getEnd();
            return List.of(new Vector3d(a.x, a.y, a.z), new Vector3d(b.x, b.y, b.z));
        }
        return null;
    }

    private Vector3d resolveUpVector(Object value) {
        if (value instanceof Vector3d vector && vector.lengthSquared() > EPS) {
            return new Vector3d(vector).normalize();
        }
        return new Vector3d(0.0d, 1.0d, 0.0d);
    }

    private static boolean isClosedPolyline(List<Vector3d> verts) {
        if (verts.size() < 3) {
            return false;
        }
        Vector3d first = verts.get(0);
        Vector3d last = verts.get(verts.size() - 1);
        return first.distance(last) < 1.0e-6d;
    }

    private static double[] buildCumulative(List<Vector3d> unique, boolean closed) {
        int segCount = closed ? unique.size() : unique.size() - 1;
        if (segCount < 1) {
            return null;
        }
        double[] cumulative = new double[segCount + 1];
        cumulative[0] = 0.0d;
        double acc = 0.0d;
        for (int i = 0; i < segCount; i++) {
            Vector3d a = unique.get(i);
            Vector3d b = unique.get((i + 1) % unique.size());
            acc += a.distance(b);
            cumulative[i + 1] = acc;
        }
        return cumulative;
    }

    private static Vector3d sampleAtDistance(List<Vector3d> unique, boolean closed, double[] cumulative, double targetDistance) {
        double clamped = Math.max(0.0d, Math.min(targetDistance, cumulative[cumulative.length - 1]));
        for (int i = 0; i < cumulative.length - 1; i++) {
            double s0 = cumulative[i];
            double s1 = cumulative[i + 1];
            if (clamped <= s1 || i == cumulative.length - 2) {
                Vector3d p0 = unique.get(i);
                Vector3d p1 = unique.get((i + 1) % unique.size());
                double segLen = s1 - s0;
                if (segLen <= EPS) {
                    return new Vector3d(p0);
                }
                double t = (clamped - s0) / segLen;
                return new Vector3d(p0).lerp(p1, t);
            }
        }
        return new Vector3d(unique.get(0));
    }

    private static double wrapDistance(double value, double length) {
        if (length <= EPS) {
            return 0.0d;
        }
        double wrapped = value % length;
        return wrapped < 0.0d ? wrapped + length : wrapped;
    }
}
