package com.nodecraft.nodesystem.nodes.spatial.analysis;

import com.nodecraft.nodesystem.api.NodeDataType;
import com.nodecraft.nodesystem.api.NodeInfo;
import com.nodecraft.nodesystem.core.BaseNode;
import com.nodecraft.nodesystem.core.BasePort;
import com.nodecraft.nodesystem.datatypes.RegionData;
import com.nodecraft.nodesystem.execution.ExecutionContext;
import com.nodecraft.nodesystem.util.BlockPosList;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Bounding Box 节点: 计算 Coordinate 列表或 Region 的轴对齐包围盒
 */
@NodeInfo(
    id = "spatial.analysis.bounding_box",
    displayName = "包围盒",
    description = "计算坐标列表或区域的轴对齐包围盒",
    category = "spatial.analysis"
)
public class BoundingBoxNode extends BaseNode {

    // --- 节点属性 ---
    private String description = "计算坐标列表或区域的轴对齐包围盒";

    // --- 输入端口 IDs ---
    private static final String INPUT_COORDINATES_ID = "input_coordinates";
    private static final String INPUT_REGION_ID = "input_region";

    // --- 输出端口 IDs ---
    private static final String OUTPUT_BOUNDING_BOX_ID = "output_bounding_box";
    private static final String OUTPUT_MIN_CORNER_ID = "output_min_corner";
    private static final String OUTPUT_MAX_CORNER_ID = "output_max_corner";
    private static final String OUTPUT_SIZE_X_ID = "output_size_x";
    private static final String OUTPUT_SIZE_Y_ID = "output_size_y";
    private static final String OUTPUT_SIZE_Z_ID = "output_size_z";
    private static final String OUTPUT_VOLUME_ID = "output_volume";
    private static final String OUTPUT_CENTER_ID = "output_center";

    // --- 构造函数 ---
    public BoundingBoxNode() {
        super(UUID.randomUUID(), "spatial.analysis.bounding_box");
        
        // 创建并添加输入端口
        addInputPort(new BasePort(INPUT_COORDINATES_ID, "Coordinates", 
                "要计算包围盒的坐标列表", NodeDataType.BLOCK_LIST, this));
        addInputPort(new BasePort(INPUT_REGION_ID, "Region", 
                "要计算包围盒的区域", NodeDataType.REGION, this));

        // 创建并添加输出端口
        addOutputPort(new BasePort(OUTPUT_BOUNDING_BOX_ID, "Bounding Box", 
                "计算出的包围盒（区域）", NodeDataType.REGION, this));
        addOutputPort(new BasePort(OUTPUT_MIN_CORNER_ID, "Min Corner", 
                "包围盒的最小角坐标", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_MAX_CORNER_ID, "Max Corner", 
                "包围盒的最大角坐标", NodeDataType.BLOCK_POS, this));
        addOutputPort(new BasePort(OUTPUT_SIZE_X_ID, "Size X", 
                "包围盒的X轴尺寸", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_SIZE_Y_ID, "Size Y", 
                "包围盒的Y轴尺寸", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_SIZE_Z_ID, "Size Z", 
                "包围盒的Z轴尺寸", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_VOLUME_ID, "Volume", 
                "包围盒的体积（方块数）", NodeDataType.INTEGER, this));
        addOutputPort(new BasePort(OUTPUT_CENTER_ID, "Center", 
                "包围盒的中心点", NodeDataType.BLOCK_POS, this));
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    // --- 核心逻辑 ---
    @Override
    public void processNode(@Nullable ExecutionContext context) {
        // 获取输入值
        Object coordinatesObj = inputValues.get(INPUT_COORDINATES_ID);
        Object regionObj = inputValues.get(INPUT_REGION_ID);
        
        // 用于存储最小和最大角坐标
        BlockPos minCorner = null;
        BlockPos maxCorner = null;
        
        // 首先检查是否有坐标列表输入
        if (coordinatesObj instanceof BlockPosList) {
            BlockPosList coordinates = (BlockPosList) coordinatesObj;
            
            // 如果坐标列表不为空，计算其包围盒
            if (!coordinates.isEmpty()) {
                minCorner = calculateMinCorner(coordinates);
                maxCorner = calculateMaxCorner(coordinates);
            }
        }
        
        // 如果没有从坐标列表计算出包围盒，尝试从区域输入获取
        if (minCorner == null && regionObj instanceof RegionData) {
            RegionData region = (RegionData) regionObj;
            
            // 从区域对象获取最小最大角落
            if (region.isComplete()) {
                minCorner = region.getMinCorner();
                maxCorner = region.getMaxCorner();
            }
        }
        
        // 如果成功获取了包围盒的角落坐标，计算并设置输出
        if (minCorner != null && maxCorner != null) {
            // 创建包围盒区域
            RegionData boundingBox = new RegionData(minCorner, maxCorner);
            
            // 计算尺寸
            int sizeX = maxCorner.getX() - minCorner.getX() + 1;
            int sizeY = maxCorner.getY() - minCorner.getY() + 1;
            int sizeZ = maxCorner.getZ() - minCorner.getZ() + 1;
            
            // 计算体积
            int volume = sizeX * sizeY * sizeZ;
            
            // 计算中心点（可能不是整数点）
            BlockPos center = new BlockPos(
                minCorner.getX() + sizeX / 2,
                minCorner.getY() + sizeY / 2,
                minCorner.getZ() + sizeZ / 2
            );
            
            // 设置所有输出值
            outputValues.put(OUTPUT_BOUNDING_BOX_ID, boundingBox);
            outputValues.put(OUTPUT_MIN_CORNER_ID, minCorner);
            outputValues.put(OUTPUT_MAX_CORNER_ID, maxCorner);
            outputValues.put(OUTPUT_SIZE_X_ID, sizeX);
            outputValues.put(OUTPUT_SIZE_Y_ID, sizeY);
            outputValues.put(OUTPUT_SIZE_Z_ID, sizeZ);
            outputValues.put(OUTPUT_VOLUME_ID, volume);
            outputValues.put(OUTPUT_CENTER_ID, center);
        } else {
            // 如果没有有效输入，清除所有输出
            outputValues.clear();
        }
    }
    
    /**
     * 计算坐标列表的最小角坐标
     * @param coordinates 坐标列表
     * @return 最小角坐标
     */
    private BlockPos calculateMinCorner(BlockPosList coordinates) {
        // 初始化最小坐标为第一个坐标
        BlockPos firstPos = coordinates.getPositions().get(0);
        int minX = firstPos.getX();
        int minY = firstPos.getY();
        int minZ = firstPos.getZ();
        
        // 遍历所有坐标，找到每个轴上的最小值
        for (BlockPos pos : coordinates) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
        }
        
        return new BlockPos(minX, minY, minZ);
    }
    
    /**
     * 计算坐标列表的最大角坐标
     * @param coordinates 坐标列表
     * @return 最大角坐标
     */
    private BlockPos calculateMaxCorner(BlockPosList coordinates) {
        // 初始化最大坐标为第一个坐标
        BlockPos firstPos = coordinates.getPositions().get(0);
        int maxX = firstPos.getX();
        int maxY = firstPos.getY();
        int maxZ = firstPos.getZ();
        
        // 遍历所有坐标，找到每个轴上的最大值
        for (BlockPos pos : coordinates) {
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        
        return new BlockPos(maxX, maxY, maxZ);
    }
} 