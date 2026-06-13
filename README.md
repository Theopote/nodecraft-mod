# NodeCraft

NodeCraft is a Fabric mod for Minecraft that adds an in-game node graph editor for procedural building, geometry generation, world operations, previews, and AI-assisted graph planning.

## Features

- Visual node graph editor inside Minecraft.
- Geometry, curve, pattern, material, world query, world write, preview, and export nodes.
- Live preview workflows for generated geometry, blocks, paths, regions, labels, and vectors.
- AI assistant panel that can turn natural-language requests into node graph plans.
- Local template/mock planning for common graph requests.
- Optional remote AI planner support through OpenAI-compatible or Anthropic-style chat APIs.
- Apply and undo support for AI-generated graph changes.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.4 or newer
- Fabric API
- Java 21

## Building

```powershell
.\gradlew build
```

The built mod jar will be written to `build/libs`.

For a faster compile-only check:

```powershell
.\gradlew --no-daemon --console plain compileJava
```

## Using NodeCraft

1. Install the mod with Fabric and Fabric API.
2. Launch Minecraft.
3. Find the NodeCraft tool item in the Tools creative tab.
4. Right-click with the tool to open the NodeCraft editor.
5. Build a graph manually, or use the AI Assistant tab in the Inspector panel to generate a graph plan.

## AI Assistant

The AI Assistant can create or modify graph plans from natural-language prompts. Remote planning is optional and can be configured from the assistant settings panel.

Supported configuration includes:

- API base URL
- API key or environment variables
- Model name
- Provider strategy
- Request timeout
- Conversation history size

Environment variables checked for API keys:

- `NODECRAFT_AI_API_KEY`
- `OPENAI_API_KEY`
- `ANTHROPIC_API_KEY`

Generated plans are validated before they can be applied. Connection creation uses the same type compatibility rules as the editor.

## Project Structure

- `src/main/java/com/nodecraft/core`: mod entrypoints and item registration.
- `src/main/java/com/nodecraft/gui`: editor UI, panels, dialogs, AI assistant UI, and ImGui integration.
- `src/main/java/com/nodecraft/nodesystem`: node API, graph model, execution, preview, data types, and node implementations.
- `src/main/resources/assets/nodecraft`: mod assets, language files, models, textures, fonts, and node icons.

## License

NodeCraft is licensed under the MIT License. See [LICENSE](LICENSE).
