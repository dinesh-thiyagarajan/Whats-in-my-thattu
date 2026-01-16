#!/usr/bin/env python3
"""
Script to generate dependency-map.json for Ruler CLI
Usage: python3 generate-dependency-map.py [output-file] [--relative]

By default, generates absolute paths so you can run Ruler from anywhere.
Use --relative flag to generate relative paths (must run Ruler from project directory).
"""

import json
import os
import sys
from pathlib import Path
from typing import List, Dict, Tuple

def find_modules(project_root: Path) -> List[str]:
    """Parse settings.gradle.kts to find all modules."""
    settings_file = project_root / "settings.gradle.kts"
    modules = []

    if settings_file.exists():
        with open(settings_file, 'r') as f:
            for line in f:
                # Match include(":app") or include(":feature:login")
                if 'include(' in line and '"' in line:
                    # Extract module name between quotes
                    start = line.find('"')
                    end = line.find('"', start + 1)
                    if start != -1 and end != -1:
                        modules.append(line[start+1:end])

    # Default to :app if no modules found
    if not modules:
        modules = [":app"]

    return modules

def find_jars(module_name: str, module_path: Path, use_absolute_paths: bool = True) -> List[Dict[str, str]]:
    """Find all JAR files for a module."""
    jars = []
    build_dir = module_path / "build"

    if not build_dir.exists():
        return jars

    # Find all JARs, but limit to avoid too many
    jar_files = list(build_dir.rglob("*.jar"))[:20]

    for jar_file in jar_files:
        # Use absolute path or relative path
        if use_absolute_paths:
            jar_path = str(jar_file.absolute())
        else:
            try:
                jar_path = str(jar_file.relative_to(Path.cwd()))
            except ValueError:
                # Path is not relative to cwd, skip
                continue

        jars.append({
            "jar": jar_path,
            "module": module_name
        })

    return jars

def find_resources(module_name: str, module_path: Path) -> List[Dict[str, str]]:
    """Find all resource files for a module."""
    resources = []
    res_dir = module_path / "src" / "main" / "res"

    if not res_dir.exists():
        return resources

    # Limit to avoid too many entries
    res_files = list(res_dir.rglob("*"))[:100]

    for res_file in res_files:
        if res_file.is_file():
            # Convert to APK path format: /res/drawable/icon.xml
            try:
                relative_to_res = res_file.relative_to(res_dir)
                apk_path = f"/res/{relative_to_res.as_posix()}"
                resources.append({
                    "filename": apk_path,
                    "module": module_name
                })
            except ValueError:
                continue

    return resources

def find_assets(module_name: str, module_path: Path) -> List[Dict[str, str]]:
    """Find all asset files for a module."""
    assets = []

    # Check assets directory
    assets_dir = module_path / "src" / "main" / "assets"
    if assets_dir.exists():
        for asset_file in assets_dir.rglob("*"):
            if asset_file.is_file():
                try:
                    relative_to_assets = asset_file.relative_to(assets_dir)
                    apk_path = f"/assets/{relative_to_assets.as_posix()}"
                    assets.append({
                        "filename": apk_path,
                        "module": module_name
                    })
                except ValueError:
                    continue

    # Check ML models directory (they become assets)
    ml_dir = module_path / "src" / "main" / "ml"
    if ml_dir.exists():
        for ml_file in ml_dir.rglob("*"):
            if ml_file.is_file():
                apk_path = f"/ml/{ml_file.name}"
                assets.append({
                    "filename": apk_path,
                    "module": module_name
                })

    return assets

def generate_dependency_map(output_file: str = "dependency-map.json", use_absolute_paths: bool = True):
    """Generate dependency-map.json for the current project."""
    project_root = Path.cwd()

    print("🔍 Searching for modules and dependencies...")
    print(f"📍 Using {'absolute' if use_absolute_paths else 'relative'} paths")

    # Find all modules
    modules = find_modules(project_root)
    print(f"📦 Found modules: {modules}")

    all_jars = []
    all_resources = []
    all_assets = []

    # Process each module
    for module in modules:
        # Convert :app to app, :feature:login to feature/login
        module_path = Path(module.lstrip(':').replace(':', '/'))

        if not module_path.exists():
            print(f"⚠️  Module path {module_path} does not exist, skipping...")
            continue

        print(f"Processing module {module} at {module_path}...")

        jars = find_jars(module, project_root / module_path, use_absolute_paths)
        resources = find_resources(module, project_root / module_path)
        assets = find_assets(module, project_root / module_path)

        all_jars.extend(jars)
        all_resources.extend(resources)
        all_assets.extend(assets)

        print(f"  ✓ JARs: {len(jars)}, Resources: {len(resources)}, Assets: {len(assets)}")

    # Create the dependency map
    dependency_map = {
        "jars": all_jars,
        "resources": all_resources,
        "assets": all_assets
    }

    # Write to file with pretty formatting
    output_path = project_root / output_file
    with open(output_path, 'w') as f:
        json.dump(dependency_map, f, indent=2)

    print(f"\n✅ Generated {output_file}")
    print(f"\n📊 Summary:")
    print(f"   - JARs: {len(all_jars)}")
    print(f"   - Resources: {len(all_resources)}")
    print(f"   - Assets: {len(all_assets)}")

    if len(all_jars) == 0:
        print("\n⚠️  Warning: No JARs found!")
        print("💡 Make sure you've built your project first:")
        print("   ./gradlew :app:assembleRelease")

    return dependency_map

def main():
    # Parse arguments
    output_file = "dependency-map.json"
    use_absolute_paths = True

    for arg in sys.argv[1:]:
        if arg == "--relative":
            use_absolute_paths = False
        elif arg == "--absolute":
            use_absolute_paths = True
        elif arg in ["--help", "-h"]:
            print(__doc__)
            print("\nExamples:")
            print("  python3 generate-dependency-map.py")
            print("  python3 generate-dependency-map.py --relative")
            print("  python3 generate-dependency-map.py my-deps.json")
            print("  python3 generate-dependency-map.py my-deps.json --relative")
            sys.exit(0)
        elif not arg.startswith("--"):
            output_file = arg

    generate_dependency_map(output_file, use_absolute_paths)

if __name__ == "__main__":
    main()
