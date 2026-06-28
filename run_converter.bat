@echo off
echo ========================================
echo NodeCraft Preset Converter
echo ========================================
echo.

cd /d "%~dp0"

echo Running preset converter...
echo.

java -cp "build/classes/java/main;lib/*" com.nodecraft.nodesystem.preset.PresetConverterTool

echo.
echo ========================================
echo Conversion complete!
echo ========================================
echo.
echo Next steps:
echo 1. Check the output: src\main\resources\nodecraft\graph_presets_updated.json
echo 2. If it looks good, replace the original:
echo    copy src\main\resources\nodecraft\graph_presets_updated.json src\main\resources\nodecraft\graph_presets.json
echo 3. Restart NodeCraft
echo.
pause
