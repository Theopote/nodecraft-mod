# NodeCraft Preset Library Implementation Specification

**Version**: 1.0  
**Date**: 2026-06-28  
**Status**: Design Specification

---

## 1. Preset System Architecture

### 1.1 Core Components

```
PresetSystem/
├── PresetLoader          # Load and parse preset files
├── PresetRegistry        # Manage available presets
├── PresetBrowser         # UI for browsing/searching
├── PresetInstantiator    # Create node graphs from presets
├── PresetParameterizer   # Handle preset parameters
└── PresetExporter        # Save graphs as presets
```

### 1.2 File Structure

```
nodecraft/
└── presets/
    ├── manifest.json                    # Preset catalog index
    ├── quickstart/
    │   ├── basic-house/
    │   │   ├── preset.json             # Preset definition
    │   │   ├── thumbnail.png           # Preview image
    │   │   └── previews/               # Additional screenshots
    │   │       ├── view1.png
    │   │       └── view2.png
    │   └── ...
    ├── architectural/
    ├── building-elements/
    └── styles/
```

---

## 2. Preset JSON Schema

### 2.1 Complete Schema

```json
{
  "$schema": "https://nodecraft.mod/preset-schema-v1.json",
  "preset_id": "quickstart.basic_house",
  "version": "1.0.0",
  "schema_version": "1.0",
  
  "metadata": {
    "name": "Basic House",
    "name_i18n": {
      "zh_CN": "基础房屋",
      "en_US": "Basic House"
    },
    "description": "A simple house with door, windows, and gable roof. Perfect for beginners.",
    "description_i18n": {
      "zh_CN": "带有门、窗户和山墙屋顶的简单房屋。非常适合初学者。"
    },
    "author": "NodeCraft Team",
    "tags": ["beginner", "house", "residential", "quickstart"],
    "category": "quickstart",
    "difficulty": "beginner",
    "estimated_build_time": "2 minutes",
    "estimated_node_count": 8
  },
  
  "thumbnails": {
    "main": "thumbnail.png",
    "previews": [
      "previews/view1.png",
      "previews/view2.png"
    ]
  },
  
  "parameters": [
    {
      "id": "width",
      "name": "Width",
      "type": "integer",
      "default": 8,
      "min": 4,
      "max": 20,
      "step": 1,
      "description": "Building width in blocks",
      "group": "Dimensions"
    },
    {
      "id": "length",
      "name": "Length",
      "type": "integer",
      "default": 10,
      "min": 4,
      "max": 20,
      "step": 1,
      "group": "Dimensions"
    },
    {
      "id": "height",
      "name": "Wall Height",
      "type": "integer",
      "default": 4,
      "min": 3,
      "max": 8,
      "step": 1,
      "group": "Dimensions"
    },
    {
      "id": "wall_material",
      "name": "Wall Material",
      "type": "block_selector",
      "default": "minecraft:oak_planks",
      "group": "Materials"
    },
    {
      "id": "roof_material",
      "name": "Roof Material",
      "type": "block_selector",
      "default": "minecraft:oak_stairs",
      "group": "Materials"
    },
    {
      "id": "roof_style",
      "name": "Roof Style",
      "type": "dropdown",
      "default": "gable",
      "options": [
        {"value": "gable", "label": "Gable"},
        {"value": "hip", "label": "Hip"},
        {"value": "flat", "label": "Flat"}
      ],
      "group": "Style"
    }
  ],
  
  "graph": {
    "nodes": [
      {
        "id": "node_1",
        "type": "geometry.primitives.box",
        "position": {"x": 100, "y": 100},
        "parameters": {
          "center": {"x": 0, "y": 0, "z": 0},
          "size": {
            "x": {"param": "width"},
            "y": {"param": "height"},
            "z": {"param": "length"}
          }
        }
      },
      {
        "id": "node_2",
        "type": "geometry.architectural_primitives.window_array",
        "position": {"x": 300, "y": 100},
        "parameters": {
          "window_width": 2,
          "window_height": 2,
          "columns": 2,
          "rows": 1
        }
      }
    ],
    "connections": [
      {
        "from": {"node": "node_1", "port": "geometry"},
        "to": {"node": "node_2", "port": "base_geometry"}
      }
    ]
  },
  
  "documentation": {
    "learning_notes": "This preset demonstrates basic box modeling, window placement, and roof generation. Try adjusting the parameters to explore variations.",
    "tips": [
      "Increase wall height for multi-story buildings",
      "Change roof style to match different architectural periods",
      "Use different wood types for varied appearances"
    ],
    "related_presets": [
      "quickstart.simple_tower",
      "architectural.residential.cottage"
    ]
  }
}
```

### 2.2 Parameter Types

| Type | Description | UI Control |
|------|-------------|------------|
| `integer` | Whole number | Slider or input |
| `float` | Decimal number | Slider or input |
| `boolean` | True/false | Checkbox |
| `string` | Text input | Text field |
| `dropdown` | Select from options | Dropdown menu |
| `block_selector` | Minecraft block | Block picker |
| `color` | Color value | Color picker |
| `vector3` | 3D coordinate | X/Y/Z inputs |
| `angle` | Angle in degrees | Angle picker |

---

## 3. Preset Browser UI

### 3.1 UI Components

```
PresetBrowser/
├── SearchBar               # Text search
├── CategoryTree            # Hierarchical navigation
├── FilterPanel             # Tag and difficulty filters
├── PresetGrid              # Thumbnail grid view
├── PresetDetail            # Detailed view with parameters
└── PresetActions           # Insert, Favorite, Learn More
```

### 3.2 UI Layout (Mockup)

```
┌─────────────────────────────────────────────────────────┐
│ Preset Library                                    [×]   │
├─────────────────────────────────────────────────────────┤
│ [Search...........................] [🔍]                │
├───────────────┬─────────────────────────────────────────┤
│ Categories    │  [Grid] [List]  Filter: All ▼          │
│               │                                         │
│ ⊞ Quickstart  │ ┌────────┐ ┌────────┐ ┌────────┐     │
│ ⊟ Architectural│ │[Thumb] │ │[Thumb] │ │[Thumb] │     │
│   • Residential│ │ Basic  │ │ Simple │ │ Garden │     │
│   • Public    │ │ House  │ │ Tower  │ │ Wall   │     │
│   • Infra     │ │ ⭐⭐⭐  │ │ ⭐⭐   │ │ ⭐⭐⭐ │     │
│ ⊞ Elements    │ └────────┘ └────────┘ └────────┘     │
│ ⊞ Styles      │                                         │
│               │ ┌────────┐ ┌────────┐ ┌────────┐     │
│ ⊞ Patterns    │ │[Thumb] │ │[Thumb] │ │[Thumb] │     │
│               │ │        │ │        │ │        │     │
│               │ └────────┘ └────────┘ └────────┘     │
├───────────────┴─────────────────────────────────────────┤
│ Selected: Basic House                                   │
│ A simple house with door, windows, and gable roof       │
│ [Insert]  [★ Favorite]  [? Learn More]                 │
└─────────────────────────────────────────────────────────┘
```

### 3.3 Preset Detail View

```
┌─────────────────────────────────────────────────────────┐
│ Basic House                                       [×]   │
├─────────────────────────────────────────────────────────┤
│ [Large Preview Image]                                   │
│                                                         │
│ Description:                                            │
│ A simple house with door, windows, and gable roof.     │
│ Perfect for beginners.                                  │
│                                                         │
│ Parameters:                                             │
│ ┌─ Dimensions ────────────────────────────────┐       │
│ │ Width:  [8    ] (4-20)                       │       │
│ │ Length: [10   ] (4-20)                       │       │
│ │ Height: [4    ] (3-8)                        │       │
│ └──────────────────────────────────────────────┘       │
│ ┌─ Materials ─────────────────────────────────┐       │
│ │ Wall:  [Oak Planks ▼]                        │       │
│ │ Roof:  [Oak Stairs ▼]                        │       │
│ └──────────────────────────────────────────────┘       │
│ ┌─ Style ────────────────────────────────────┐       │
│ │ Roof Style: [Gable ▼]                        │       │
│ └──────────────────────────────────────────────┘       │
│                                                         │
│ [Preview Changes]  [Insert]  [Cancel]                  │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Implementation Classes

### 4.1 PresetDefinition

```java
public class PresetDefinition {
    private String presetId;
    private String version;
    private PresetMetadata metadata;
    private List<PresetParameter> parameters;
    private PresetGraph graph;
    private PresetDocumentation documentation;
    
    public NodeGraph instantiate(Map<String, Object> paramValues) {
        // Create node graph with parameter substitution
    }
}
```

### 4.2 PresetParameter

```java
public class PresetParameter {
    private String id;
    private String name;
    private ParameterType type;
    private Object defaultValue;
    private Object minValue;
    private Object maxValue;
    private String group;
    private String description;
    
    public Object validateValue(Object value) {
        // Validate against constraints
    }
}
```

### 4.3 PresetRegistry

```java
public class PresetRegistry {
    private static final PresetRegistry INSTANCE = new PresetRegistry();
    private Map<String, PresetDefinition> presets = new HashMap<>();
    
    public void loadPresets(Path presetDirectory) {
        // Scan and load all preset.json files
    }
    
    public List<PresetDefinition> search(String query, List<String> tags) {
        // Search presets by query and tags
    }
    
    public PresetDefinition getPreset(String presetId) {
        return presets.get(presetId);
    }
}
```

### 4.4 PresetInstantiator

```java
public class PresetInstantiator {
    public NodeGraph instantiate(PresetDefinition preset, Map<String, Object> paramValues) {
        NodeGraph graph = new NodeGraph();
        
        // 1. Create nodes
        for (PresetNodeDefinition nodeDef : preset.getGraph().getNodes()) {
            INode node = createNode(nodeDef, paramValues);
            graph.addNode(node);
        }
        
        // 2. Create connections
        for (PresetConnectionDefinition connDef : preset.getGraph().getConnections()) {
            graph.connect(connDef.getFrom(), connDef.getTo());
        }
        
        return graph;
    }
    
    private INode createNode(PresetNodeDefinition nodeDef, Map<String, Object> paramValues) {
        INode node = NodeRegistry.getInstance().createNodeInstance(nodeDef.getType());
        
        // Substitute parameter placeholders
        for (Map.Entry<String, Object> param : nodeDef.getParameters().entrySet()) {
            Object value = resolveValue(param.getValue(), paramValues);
            node.setInput(param.getKey(), value);
        }
        
        return node;
    }
    
    private Object resolveValue(Object value, Map<String, Object> paramValues) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map) value;
            if (map.containsKey("param")) {
                // Parameter reference: {"param": "width"}
                return paramValues.get(map.get("param"));
            }
        }
        return value;
    }
}
```

---

## 5. Preset Creation Workflow

### 5.1 User Workflow

```
1. Create Node Graph
   ↓
2. Right-click → "Save as Preset"
   ↓
3. Preset Export Dialog Opens
   ↓
4. Mark Parameterizable Inputs
   - Select node inputs to expose as parameters
   - Define parameter properties (name, range, default)
   ↓
5. Add Metadata
   - Name, description, tags
   - Category, difficulty
   - Learning notes
   ↓
6. Add Thumbnails
   - Capture screenshots
   - Set main thumbnail
   ↓
7. Preview and Test
   - Test parameter variations
   - Verify instantiation
   ↓
8. Save Preset
   - Export to preset library
   - Or share as file
```

### 5.2 Parameter Marking UI

```
┌─────────────────────────────────────────────────────────┐
│ Export as Preset: Mark Parameters                      │
├─────────────────────────────────────────────────────────┤
│ Node Graph Preview              Selected Node:          │
│                                 Box by Center + Size    │
│ [Node Graph                     ┌─────────────────────┐│
│  Visualization]                 │ Inputs:              ││
│                                 │ □ center             ││
│                                 │ ☑ size.x → "width"  ││
│                                 │ ☑ size.y → "height" ││
│                                 │ ☑ size.z → "length" ││
│                                 └─────────────────────┘│
│                                                         │
│ Exposed Parameters:                                     │
│ 1. width (from Box.size.x)                             │
│    Type: [integer▼] Min: [4] Max: [20] Default: [8]   │
│    Group: [Dimensions▼]                                 │
│                                                         │
│ 2. height (from Box.size.y)                            │
│    Type: [integer▼] Min: [3] Max: [8] Default: [4]    │
│    Group: [Dimensions▼]                                 │
│                                                         │
│ [+ Add Parameter]                                       │
│                                                         │
│ [< Back]  [Next >]                                     │
└─────────────────────────────────────────────────────────┘
```

---

## 6. Initial Preset Specifications

### 6.1 Quickstart Preset: Basic House

**Node Graph**:
```
[Player Position] → [Box by Center+Size]
                      ↓ (width, length, height params)
                    [Get Box Face] (front)
                      ↓
                    [Window Array] (2x1, spacing)
                      ↓
                    [Difference] ← [Box] (geometry)
                      ↓
                    [Get Box Face] (top)
                      ↓
                    [Roof Generator] (gable, overhang)
                      ↓
                    [Union]
                      ↓
                    [Assign Block Type] (wall material param)
                      ↓
                    [Bake Geometry To Blocks]
                      ↓
                    [Preview Blocks]
```

**Parameters**:
- width (integer, 4-20, default 8)
- length (integer, 4-20, default 10)
- height (integer, 3-8, default 4)
- wall_material (block_selector, default oak_planks)
- roof_material (block_selector, default oak_stairs)
- roof_style (dropdown: gable/hip/flat, default gable)

**Estimated Complexity**: 8-10 nodes

### 6.2 Quickstart Preset: Simple Tower

**Node Graph**:
```
[Player Position] → [Cylinder by Axis+Radius]
                      ↓ (height, radius params)
                    [Get Box Face] (side, repeated)
                      ↓
                    [Window Array] (radial)
                      ↓
                    [Difference]
                      ↓
                    [Top edge] → [Polar Array] (crenellations)
                      ↓
                    [Union]
                      ↓
                    [Assign Block Type] (stone param)
                      ↓
                    [Bake Geometry To Blocks]
```

**Parameters**:
- height (integer, 10-40, default 20)
- radius (integer, 3-10, default 5)
- material (block_selector, default stone_bricks)
- crenellation_style (dropdown: battlements/smooth/pointed)

**Estimated Complexity**: 10-12 nodes

### 6.3 Building Element: Medieval Window

**Node Graph**:
```
[Rectangle Profile] (width x height)
  ↓
[Arch Profile] (top, rounded)
  ↓
[Union]
  ↓
[Extrude] (depth)
  ↓
[Frame offset] → [Difference] (create frame)
  ↓
[Assign Block Type] (frame: stone, glass: glass_pane)
  ↓
[Bake]
```

**Parameters**:
- width (integer, 1-4, default 2)
- height (integer, 2-6, default 3)
- depth (integer, 1-3, default 2)
- arch_height (float, 0-2, default 1)
- frame_material (block_selector, default stone_bricks)
- glass_type (block_selector, default glass_pane)

**Estimated Complexity**: 8 nodes

---

## 7. Testing Plan

### 7.1 Unit Tests

```java
@Test
public void testPresetLoading() {
    PresetDefinition preset = PresetLoader.load("presets/quickstart/basic-house/preset.json");
    assertNotNull(preset);
    assertEquals("quickstart.basic_house", preset.getPresetId());
    assertEquals(6, preset.getParameters().size());
}

@Test
public void testPresetInstantiation() {
    PresetDefinition preset = PresetRegistry.getInstance().getPreset("quickstart.basic_house");
    Map<String, Object> params = Map.of(
        "width", 10,
        "height", 5,
        "length", 12
    );
    NodeGraph graph = PresetInstantiator.instantiate(preset, params);
    assertNotNull(graph);
    assertTrue(graph.getNodes().size() > 0);
}

@Test
public void testParameterValidation() {
    PresetParameter param = new PresetParameter("width", ParameterType.INTEGER, 8, 4, 20);
    assertEquals(8, param.validateValue(8)); // Valid
    assertEquals(4, param.validateValue(2)); // Clamped to min
    assertEquals(20, param.validateValue(30)); // Clamped to max
}
```

### 7.2 Integration Tests

1. **Load all presets** - Verify all preset files parse correctly
2. **Instantiate each preset** - Create graphs with default parameters
3. **Execute preset graphs** - Run execution and verify outputs
4. **Parameter variation** - Test min/max/default values
5. **UI workflow** - Test browser, search, insert

### 7.3 User Acceptance Tests

1. **Beginner user** - Can find and use quickstart preset in < 5 minutes
2. **Intermediate user** - Can modify preset parameters effectively
3. **Advanced user** - Can create and export custom preset
4. **Search functionality** - Can find presets by name, tag, category
5. **Performance** - Preset browser loads in < 1 second

---

## 8. Documentation Requirements

### 8.1 User Documentation

**Preset Library Guide**:
1. Introduction to Presets
2. Browsing and Searching Presets
3. Using Presets (inserting, parameterizing)
4. Creating Custom Presets
5. Sharing Presets
6. Best Practices

**Preset Catalog**:
- Alphabetical list of all presets
- Category-organized listing
- Search index

### 8.2 Developer Documentation

**Preset Developer Guide**:
1. Preset JSON Schema Reference
2. Creating Preset Node Graphs
3. Parameter System Guide
4. Thumbnail and Screenshot Guide
5. Testing Presets
6. Publishing Presets

**API Reference**:
- PresetDefinition class
- PresetRegistry API
- PresetInstantiator API

---

## 9. Future Enhancements

### 9.1 Phase 2 Features

- **Preset Variants**: Save modified presets as variants
- **Preset History**: Track usage and recent presets
- **Preset Collections**: Bundle related presets
- **Preset Templates**: Templates for creating presets

### 9.2 Phase 3 Features

- **Online Preset Repository**: Cloud-based sharing
- **Preset Rating/Reviews**: Community feedback
- **Preset Versioning**: Update installed presets
- **Preset Dependencies**: Presets that reference other presets

### 9.3 Phase 4 Features

- **AI Preset Generation**: Generate presets from descriptions
- **Preset Remixing**: Combine multiple presets
- **Parametric Preset Editing**: Edit preset graphs directly
- **Preset Marketplace**: Commercial preset distribution

---

**Document Status**: Design Specification Complete  
**Next Step**: Implementation Phase 1  
**Estimated Implementation Time**: 2-3 weeks for core system + 20 initial presets
