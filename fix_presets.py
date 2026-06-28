import json
import os
import re

def fix_preset_json(file_path):
    """Fix common JSON syntax errors in preset files"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Try to parse - if successful, no fix needed
        try:
            json.loads(content)
            print(f"✅ Already valid: {file_path}")
            return True
        except json.JSONDecodeError as e:
            print(f"🔧 Fixing: {file_path}")
            print(f"   Error: {e}")

            # Common issue: missing closing bracket after parameters array
            # Look for pattern: }  ], (should be }  ],)
            # The issue is: last parameter has } but no comma, then array closes with ]

            lines = content.split('\n')
            fixed_lines = []
            in_parameters = False

            for i, line in enumerate(lines):
                if '"parameters":' in line:
                    in_parameters = True
                elif in_parameters and line.strip() == '},':
                    # This is the end of the parameters array - should be just ]
                    fixed_lines.append(line)
                elif in_parameters and line.strip() == '],' or line.strip() == ']':
                    # End of parameters array
                    in_parameters = False
                    fixed_lines.append(line)
                else:
                    fixed_lines.append(line)

            fixed_content = '\n'.join(fixed_lines)

            # Try parsing fixed content
            try:
                json.loads(fixed_content)
                # Write back
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(fixed_content)
                print(f"   ✅ Fixed successfully!")
                return True
            except:
                print(f"   ❌ Auto-fix failed, needs manual review")
                return False

    except Exception as e:
        print(f"❌ Error processing {file_path}: {e}")
        return False

def fix_all_presets(preset_dir):
    """Fix all preset.json files"""
    fixed = 0
    failed = 0

    for root, dirs, files in os.walk(preset_dir):
        if 'preset.json' in files:
            preset_path = os.path.join(root, 'preset.json')
            if fix_preset_json(preset_path):
                fixed += 1
            else:
                failed += 1

    print(f"\n{'='*60}")
    print(f"Summary: {fixed} fixed/valid, {failed} need manual review")
    return fixed, failed

if __name__ == "__main__":
    preset_dir = r"F:\development\NC\nodecraft\presets"
    fix_all_presets(preset_dir)
