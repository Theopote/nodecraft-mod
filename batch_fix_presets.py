import os
import re

def fix_preset_file(file_path):
    """Fix the trailing comma issue in preset files"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern: last closing brace of parameters array with comma before ]
    # Replace }  }, with }  ],
    fixed_content = re.sub(r'(\s+}\s*)\},(\s*\n\s*"graph")', r'\1],\2', content)

    if fixed_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(fixed_content)
        return True
    return False

# List of files that need fixing based on the error log
files_to_fix = [
    r"F:\development\NC\nodecraft\presets\architectural\infrastructure\stone-bridge\preset.json",
    r"F:\development\NC\nodecraft\presets\architectural\infrastructure\watchtower\preset.json",
    r"F:\development\NC\nodecraft\presets\building-elements\columns\classical-column\preset.json",
    r"F:\development\NC\nodecraft\presets\building-elements\stairs\spiral-staircase\preset.json",
    r"F:\development\NC\nodecraft\presets\building-elements\stairs\straight-staircase\preset.json",
    r"F:\development\NC\nodecraft\presets\building-elements\windows\arched-window\preset.json",
    r"F:\development\NC\nodecraft\presets\quickstart\garden-wall\preset.json",
    r"F:\development\NC\nodecraft\presets\styles\fantasy\wizard-tower\preset.json",
    r"F:\development\NC\nodecraft\presets\styles\medieval\castle-keep\preset.json",
    r"F:\development\NC\nodecraft\presets\styles\modern\glass-box-building\preset.json",
]

print("Fixing preset files...")
for file_path in files_to_fix:
    if os.path.exists(file_path):
        if fix_preset_file(file_path):
            print(f"✅ Fixed: {os.path.basename(os.path.dirname(file_path))}")
        else:
            print(f"⚠️  No change: {os.path.basename(os.path.dirname(file_path))}")
    else:
        print(f"❌ Not found: {file_path}")

print("\nDone! Run the converter again.")
