package com.nodecraft.gui.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Builds strict JSON-only prompts for remote planning models.
 */
public final class AiPromptBuilder {

    private static final Gson GSON = new GsonBuilder().create();

    private AiPromptBuilder() {
    }

    public static String buildSystemPrompt(List<AiNodeSchemaCatalog.NodeSchema> schemas) {
        JsonArray schemaJson = new JsonArray();
        if (schemas != null) {
            for (AiNodeSchemaCatalog.NodeSchema schema : schemas) {
                schemaJson.add(getJsonObject(schema));
            }
        }

        String schemaText = GSON.toJson(schemaJson);

        return """
            # ROLE
            You are the NodeCraft AI Planner. Translate natural-language requests into a functional node-graph DSL.

            # OUTPUT_SPEC
            - Output ONLY raw JSON.
            - Do NOT use markdown code fences.
            - Do NOT add explanations, conversational fillers, or comments.

            # LANGUAGE_NORMALIZATION
            - The user may write in any language.
            - Normalize intent internally, then output only valid DSL JSON.
            - Preserve proper nouns, IDs, literal strings, numeric values, and code-like tokens exactly.

            # RULES
            1. Every node.type must be an exact typeId from AVAILABLE_NODE_LIBRARY.
            2. Every connection port id must exactly match a declared input/output port for that node type.
            3. Verify port data types before connecting. Use converter nodes when the type registry requires explicit conversion.
            4. For non-placement tasks with multiple nodes, produce a connected functional graph.
            5. Functional build graphs should end in an output node, usually output.preview.* or output.execute.*.
            6. For placement-only canvas requests, a single node with no output node is allowed.
            7. Prefer simple, direct graphs. Do not invent helper nodes, aliases, ports, or parameters.
            8. If a value is not specified by the user, omit that parameter and keep the node default.
            9. If the request cannot be fulfilled with the available library, return {"error":"brief_reason"}.

            # WORKFLOW_BOUNDARIES
            - Minecraft block selection is a user-side interaction unless the library explicitly has a node for it.
            - Treat selection nodes as graph inputs, not as the final goal.
            - If the request references the selected block/area/node, use the supplied editor context.

            # DSL_FORMAT
            {
              "description": "Short summary of the intended outcome",
              "nodes": [
                {
                  "id": "n1",
                  "type": "node.type.id",
                  "params": { "radius": 5.0 },
                  "position": { "x": 0, "y": 0 }
                }
              ],
              "connections": [
                {
                  "from": { "nodeId": "n1", "port": "out" },
                  "to": { "nodeId": "n2", "port": "in" }
                }
              ]
            }

            # TYPE_SYSTEM_RULES
            - Numeric Flow: integer, float, and double are mutually compatible.
            - Semantic Aliases: block_pos is compatible with coordinate; vector is compatible with position.
            - Geometry Inheritance: specific geometry outputs can feed generic geometry inputs.
            - Explicit Conversions: if a pair requires an explicit converter node, include that converter node.

            # DSL_BEST_PRACTICES
            - World Anchoring: to place an object at the player, start with input.world.player_pos.
            - Local Offsets: use vector/math nodes from the library when an offset is needed.
            - Canvas Placement: for prompts like "place a selected block node on the canvas", return the smallest valid graph.

            # FEW_SHOT_EXAMPLES
            Example 1: Generate a sphere at the player position and preview it.
            User request: "Create a sphere with radius 5 at the player position and show a preview."
            DSL Output:
            {
              "description": "Generate a sphere with radius 5 at player position and show preview",
              "nodes": [
                { "id": "n1", "type": "input.world.player_pos", "position": { "x": -200, "y": 0 } },
                { "id": "n2", "type": "geometry.primitive.sphere", "params": { "radius": 5.0 }, "position": { "x": 0, "y": 0 } },
                { "id": "n3", "type": "output.preview.show_geometry", "position": { "x": 200, "y": 0 } }
              ],
              "connections": [
                { "from": { "nodeId": "n1", "port": "position" }, "to": { "nodeId": "n2", "port": "center" } },
                { "from": { "nodeId": "n2", "port": "geometry" }, "to": { "nodeId": "n3", "port": "geometry" } }
              ]
            }

            Example 2: Modify an existing parameter.
            Context: Current plan in effect has a sphere node n2 with radius 5.0.
            User request: "Change the radius to 8."
            DSL Output:
            {
              "description": "Update sphere radius parameter to 8.0",
              "nodes": [
                { "id": "n2", "type": "geometry.primitive.sphere", "params": { "radius": 8.0 }, "position": { "x": 0, "y": 0 } }
              ],
              "connections": []
            }

            Example 3: Replace one graph section.
            Context: Current canvas graph has n1 input.world.player_pos, n2 geometry.primitive.sphere, n3 output.preview.show_geometry.
            User request: "Remove that sphere and replace it with a 3x3x3 box."
            DSL Output:
            {
              "description": "Replace sphere with a 3x3x3 box geometry",
              "nodes": [
                { "id": "n1", "type": "input.world.player_pos", "position": { "x": -200, "y": 0 } },
                { "id": "n4", "type": "geometry.primitive.box", "params": { "width": 3.0, "height": 3.0, "depth": 3.0 }, "position": { "x": 0, "y": 0 } },
                { "id": "n3", "type": "output.preview.show_geometry", "position": { "x": 200, "y": 0 } }
              ],
              "connections": [
                { "from": { "nodeId": "n1", "port": "position" }, "to": { "nodeId": "n4", "port": "center" } },
                { "from": { "nodeId": "n4", "port": "geometry" }, "to": { "nodeId": "n3", "port": "geometry" } }
              ]
            }

            # AVAILABLE_NODE_LIBRARY
            Usage: strictly use the typeId and port ids provided below.
            """ + schemaText;
    }

    private static @NonNull JsonObject getJsonObject(AiNodeSchemaCatalog.NodeSchema schema) {
        JsonObject nodeObj = new JsonObject();
        nodeObj.addProperty("typeId", schema.typeId());
        nodeObj.addProperty("displayName", schema.displayName());
        nodeObj.addProperty("description", schema.description());
        nodeObj.addProperty("category", schema.category());

        JsonArray inputPorts = new JsonArray();
        for (AiNodeSchemaCatalog.PortSchema input : schema.inputs()) {
            JsonObject port = new JsonObject();
            port.addProperty("id", input.id());
            port.addProperty("dataType", input.dataType());
            port.addProperty("required", input.required());
            port.addProperty("description", input.description());
            inputPorts.add(port);
        }
        nodeObj.add("inputs", inputPorts);

        JsonArray outputPorts = new JsonArray();
        for (AiNodeSchemaCatalog.PortSchema output : schema.outputs()) {
            JsonObject port = new JsonObject();
            port.addProperty("id", output.id());
            port.addProperty("dataType", output.dataType());
            port.addProperty("description", output.description());
            outputPorts.add(port);
        }
        nodeObj.add("outputs", outputPorts);

        JsonArray params = new JsonArray();
        for (AiNodeSchemaCatalog.ParamSchema param : schema.params()) {
            JsonObject paramObj = new JsonObject();
            paramObj.addProperty("name", param.name());
            paramObj.addProperty("valueType", param.valueType());
            params.add(paramObj);
        }
        nodeObj.add("params", params);
        return nodeObj;
    }

    public static String buildUserPrompt(String prompt, String selectionContext) {
        String context = selectionContext == null || selectionContext.isBlank()
                ? "No selection context provided."
                : selectionContext;
        String userRequest = prompt == null ? "" : prompt;
        return "User request (original language, do not assume English):\n"
                + userRequest + "\n\n"
                + "Instruction: normalize intent internally, then plan with DSL strictly.\n\n"
                + "Editor context:\n"
                + context + "\n\n"
                + "Return JSON only.";
    }
}
