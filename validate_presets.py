import json
import os
from pathlib import Path

def validate_preset_json(preset_dir):
    """Validate all preset.json files"""
    errors = []
    valid = []

    for root, dirs, files in os.walk(preset_dir):
        if 'preset.json' in files:
            preset_path = os.path.join(root, 'preset.json')
            try:
                with open(preset_path, 'r', encoding='utf-8') as f:
                    json.load(f)
                valid.append(preset_path)
                print(f"✅ Valid: {preset_path}")
            except json.JSONDecodeError as e:
                errors.append((preset_path, str(e)))
                print(f"❌ Error in {preset_path}:")
                print(f"   {e}")

    print(f"\n\nSummary:")
    print(f"Valid: {len(valid)}")
    print(f"Errors: {len(errors)}")

    return errors, valid

if __name__ == "__main__":
    preset_dir = r"F:\development\NC\nodecraft\presets"
    errors, valid = validate_preset_json(preset_dir)

    if errors:
        print("\n\nFiles with errors:")
        for path, error in errors:
            print(f"  - {path}")
