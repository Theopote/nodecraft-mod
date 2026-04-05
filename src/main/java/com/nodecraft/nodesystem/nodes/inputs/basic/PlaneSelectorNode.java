package com.nodecraft.nodesystem.nodes.inputs.basic;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.api.NodeProperty;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.PlaneData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NodeInfo(
    id = "inputs.basic.plane_selector",
    displayName = "平面选择器",
    description = "创建标准 XY / YZ / XZ 平面，并支持设置或输入原点",
    category = "inputs.basic"
)
public class PlaneSelectorNode extends BaseNode {

    public enum PlanePreset {
        XY,
        YZ,
        XZ
    }

    private static final String INPUT_ORIGIN_ID = "input_origin";
    private static final String OUTPUT_PLANE_ID = "output_plane";
    private static final String OUTPUT_ORIGIN_ID = "output_origin";
    private static final String OUTPUT_NORMAL_ID = "output_normal";

    @NodeProperty(displayName = "平面预设", category = "平面", order = 1,
        description = "选择标准平面的朝向")
    private PlanePreset planePreset = PlanePreset.XZ;

    @NodeProperty(displayName = "原点 X", category = "原点", order = 2,
        description = "未连接原点输入时使用的 X 坐标")
    private int originX = 0;

    @NodeProperty(displayName = "原点 Y", category = "原点", order = 3,
        description = "未连接原点输入时使用的 Y 坐标")
    private int originY = 0;

    @NodeProperty(displayName = "原点 Z", category = "原点", order = 4,
        description = "未连接原点输入时使用的 Z 坐标")
    private int originZ = 0;

    public PlaneSelectorNode() {
        super(UUID.randomUUID(), "inputs.basic.plane_selector");

        addInputPort(new BasePort(INPUT_ORIGIN_ID, "Origin", "可选的平面原点输入", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_PLANE_ID, "Plane", "生成的平面数据", NodeDataType.PLANE, this));
        addOutputPort(new BasePort(OUTPUT_ORIGIN_ID, "Origin", "平面原点", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_NORMAL_ID, "Normal", "平面法线", NodeDataType.VECTOR, this));
    }

    @Override
    public String getDescription() {
        return "创建标准 XY / YZ / XZ 平面，并支持设置或输入原点。";
    }

    @Override
    public void processNode(@Nullable ExecutionContext context) {
        BlockPos origin = resolveOrigin(inputValues.get(INPUT_ORIGIN_ID));
        Vector3d normal = resolveNormal();
        PlaneData plane = new PlaneData(
            new Vector3d(origin.getX(), origin.getY(), origin.getZ()),
            new Vector3d(normal)
        );

        outputValues.put(OUTPUT_PLANE_ID, plane);
        outputValues.put(OUTPUT_ORIGIN_ID, origin);
        outputValues.put(OUTPUT_NORMAL_ID, normal);
    }

    private BlockPos resolveOrigin(Object originObj) {
        if (originObj instanceof BlockPos blockPos) {
            return blockPos.toImmutable();
        }
        return new BlockPos(originX, originY, originZ);
    }

    private Vector3d resolveNormal() {
        return switch (planePreset) {
            case XY -> new Vector3d(0.0d, 0.0d, 1.0d);
            case YZ -> new Vector3d(1.0d, 0.0d, 0.0d);
            case XZ -> new Vector3d(0.0d, 1.0d, 0.0d);
        };
    }

    public PlanePreset getPlanePreset() {
        return planePreset;
    }

    public void setPlanePreset(PlanePreset planePreset) {
        if (planePreset != null && this.planePreset != planePreset) {
            this.planePreset = planePreset;
            markDirty();
        }
    }

    public int getOriginX() {
        return originX;
    }

    public void setOriginX(int originX) {
        if (this.originX != originX) {
            this.originX = originX;
            markDirty();
        }
    }

    public int getOriginY() {
        return originY;
    }

    public void setOriginY(int originY) {
        if (this.originY != originY) {
            this.originY = originY;
            markDirty();
        }
    }

    public int getOriginZ() {
        return originZ;
    }

    public void setOriginZ(int originZ) {
        if (this.originZ != originZ) {
            this.originZ = originZ;
            markDirty();
        }
    }

    @Override
    public Object getNodeState() {
        Map<String, Object> state = new HashMap<>();
        state.put("planePreset", planePreset.name());
        state.put("originX", originX);
        state.put("originY", originY);
        state.put("originZ", originZ);
        return state;
    }

    @Override
    public void setNodeState(Object state) {
        if (!(state instanceof Map<?, ?> map)) {
            return;
        }

        if (map.get("planePreset") instanceof String preset) {
            try {
                setPlanePreset(PlanePreset.valueOf(preset));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (map.get("originX") instanceof Number x) {
            setOriginX(x.intValue());
        }
        if (map.get("originY") instanceof Number y) {
            setOriginY(y.intValue());
        }
        if (map.get("originZ") instanceof Number z) {
            setOriginZ(z.intValue());
        }
    }
}
