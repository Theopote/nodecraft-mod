package com.nodecraft.nodesystem.nodes.geometry.curves;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.LineData;
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
 * Rebuilds a path into near-uniform arc-length samples from Curve/Polyline/Line inputs.
 */
@NodeInfo(
    id = "geometry.curves.rebuild_curve_length",
    displayName = "Curve Rebuild By Length",
    description = "Rebuilds a curve/path to uniform arc-length samples using spacing, or using a total point count (count wins when both are provided)",
    category = "geometry.curves",
    order = 13
)
public class CurveRebuildByLengthNode extends BaseNode {

    private static final double EPS = 1.0e-9d;

    private static final String INPUT_CURVE_ID = "input_curve";
    private static final String INPUT_POLYLINE_ID = "input_polyline";
    private static final String INPUT_LINE_ID = "input_line";
    private static final String INPUT_SPACING_ID = "input_spacing";
    private static final String INPUT_COUNT_ID = "input_count";

    private static final String OUTPUT_CURVE_ID = "output_curve";
    private static final String OUTPUT_POLYLINE_ID = "output_polyline";
    private static final String OUTPUT_POINTS_ID = "output_points";
    private static final String OUTPUT_LENGTH_ID = "output_length";
    private static final String OUTPUT_VALID_ID = "output_valid";

    public CurveRebuildByLengthNode() {
        super(UUID.randomUUID(), "geometry.curves.rebuild_curve_length");

        addInputPort(new BasePort(INPUT_CURVE_ID, "Curve",
            "Curve to rebuild by arc length", NodeDataType.CURVE, this));
        addInputPort(new BasePort(INPUT_POLYLINE_ID, "Polyline",
            "Fallback polyline to rebuild when no curve is connected", NodeDataType.POLYLINE, this));
        addInputPort(new BasePort(INPUT_LINE_ID, "Line",
            "Fallback line segment when no curve/polyline is connected", NodeDataType.LINE, this));
        addInputPort(new BasePort(INPUT_SPACING_ID, "Spacing",
            "Target distance between samples along the path (> 0 when used)", NodeDataType.DOUBLE, this));
        addInputPort(new BasePort(INPUT_COUNT_ID, "Count",
            "Target number of samples along the path (>= 2). When set, overrides spacing", NodeDataType.INTEGER, this));

        addOutputPort(new BasePort(OUTPUT_CURVE_ID, "Curve",
            "Rebuilt sampled curve as a linear control path", NodeDataType.CURVE, this));
        addOutputPort(new BasePort(OUTPUT_POLYLINE_ID, "Polyline",
            "Rebuilt polyline", NodeDataType.POLYLINE, this));
        addOutputPort(new BasePort(OUTPUT_POINTS_ID, "Points",
            "Rebuilt points as a list of Vector3d positions", NodeDataType.VECTOR_LIST, this));
        addOutputPort(new BasePort(OUTPUT_LENGTH_ID, "Length",
            "Total input path length used for rebuilding", NodeDataType.DOUBLE, this));
        addOutputPort(new BasePort(OUTPUT_VALID_ID, "Valid",
            "True when rebuilding succeeded", NodeDataType.BOOLEAN, this));
    }

    @Override
    public String getDescription() {
        return "Rebuilds a curve/path to uniform arc-length samples using spacing, or using a total point count (count wins when both are provided)";
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

        List<Vector3d> samples = new ArrayList<>(sampleDistances.size());
        for (double d : sampleDistances) {
            samples.add(sampleAtDistance(unique, closed, cumulative, d));
        }

        if (closed && samples.size() >= 2) {
            while (samples.size() >= 2 && samples.get(0).distance(samples.get(samples.size() - 1)) < 1.0e-6d) {
                samples.remove(samples.size() - 1);
            }
        }

        List<Vec3d> rebuilt = new ArrayList<>(samples.size() + (closed ? 1 : 0));
        for (Vector3d p : samples) {
            rebuilt.add(new Vec3d(p.x, p.y, p.z));
        }
        if (closed) {
            rebuilt.add(rebuilt.get(0));
        }

        try {
            PolylineData polyline = new PolylineData(rebuilt);
            Curve curve = new Curve(Curve.CurveType.LINEAR, 2);
            for (Vec3d point : rebuilt) {
                curve.addControlPoint(point);
            }
            outputValues.put(OUTPUT_CURVE_ID, curve);
            outputValues.put(OUTPUT_POLYLINE_ID, polyline);
            outputValues.put(OUTPUT_POINTS_ID, List.copyOf(samples));
            outputValues.put(OUTPUT_LENGTH_ID, total);
            outputValues.put(OUTPUT_VALID_ID, true);
        } catch (IllegalArgumentException ex) {
            writeInvalid();
        }
    }

    private void writeInvalid() {
        outputValues.put(OUTPUT_CURVE_ID, null);
        outputValues.put(OUTPUT_POLYLINE_ID, null);
        outputValues.put(OUTPUT_POINTS_ID, List.of());
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
}
