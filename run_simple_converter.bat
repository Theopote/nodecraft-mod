@echo off
echo ========================================
echo Compiling and Running Simple Converter
echo ========================================
echo.

cd /d "%~dp0"

echo Step 1: Compiling SimplePresetConverter...
echo.
javac -cp "build/classes/java/main;lib/*" -d build/classes/java/main src/main/java/com/nodecraft/nodesystem/preset/SimplePresetConverter.java

if errorlevel 1 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✓ Compilation successful
echo.

echo Step 2: Running converter...
echo.
java -cp "build/classes/java/main;lib/*" com.nodecraft.nodesystem.preset.SimplePresetConverter

if errorlevel 1 (
    echo ❌ Conversion failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Step 3: Replacing files...
echo.

if exist src\main\resources\nodecraft\graph_presets_updated.json (
    copy /Y src\main\resources\nodecraft\graph_presets_updated.json src\main\resources\nodecraft\graph_presets.json
    echo ✓ File replaced successfully!
) else (
    echo ❌ Output file not found!
    pause
    exit /b 1
)

echo.
echo ========================================
echo ALL DONE!
echo ========================================
echo.
echo Now restart NodeCraft to see all presets working!
echo.
pause
