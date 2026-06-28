@echo off
echo ========================================
echo Using Gradle to Run Converter
echo ========================================
echo.

cd /d "%~dp0"

echo Running converter via Gradle...
echo.

call gradlew runPresetConverter

if errorlevel 1 (
    echo.
    echo ❌ Conversion failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Checking output...
echo.

if exist src\main\resources\nodecraft\graph_presets_updated.json (
    echo ✓ File generated successfully!

    REM Get file size
    for %%A in (src\main\resources\nodecraft\graph_presets_updated.json) do echo File size: %%~zA bytes

    echo.
    echo Replacing graph_presets.json...
    copy /Y src\main\resources\nodecraft\graph_presets_updated.json src\main\resources\nodecraft\graph_presets.json
    echo ✓ Done!
) else (
    echo ❌ Output file not found!
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS!
echo ========================================
echo.
echo All presets have been converted and installed.
echo Now restart NodeCraft!
echo.
pause
