#!/usr/bin/env python3
"""Safely inspect and stage a local Aion 4.8 level without modifying originals."""
import argparse
import hashlib
import json
import shutil
from pathlib import Path

def sha256(path):
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def inventory_level(source):
    source = Path(source).resolve()
    if not source.is_dir():
        raise ValueError("Source level directory does not exist")
    files = []
    for item in sorted(source.rglob("*")):
        if item.is_symlink():
            raise ValueError("Symlinks not allowed in level source")
        if item.is_file():
            files.append({"path": item.relative_to(source).as_posix(),
                          "size_bytes": item.stat().st_size,
                          "sha256": sha256(item),
                          "extension": item.suffix.lower()})
    return {"schema_version": 1, "level_source": str(source),
            "client_version_verified": False, "runtime_tested": False,
            "files": files}

def stage_level(source, destination):
    source, destination = Path(source).resolve(), Path(destination).resolve()
    if not source.is_dir():
        raise ValueError("Source level directory does not exist")
    if source == destination or source in destination.parents or destination in source.parents:
        raise ValueError("Source and destination must be disjoint directories")
    if destination.exists():
        raise ValueError("Destination must not already exist")
    manifest = inventory_level(source)
    destination.mkdir(parents=True)
    try:
        for entry in manifest["files"]:
            target = destination / entry["path"]
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source / entry["path"], target)
            if sha256(target) != entry["sha256"]:
                raise ValueError("Copy verification failed")
        (destination / "aion-level-manifest.json").write_text(
            json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    except Exception:
        shutil.rmtree(destination)
        raise
    return manifest

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--source", required=True, help="Local Aion level directory")
    p.add_argument("--output", required=True, help="JSON report file or staging directory")
    p.add_argument("--stage", action="store_true", help="Copy source into new isolated staging directory")
    args = p.parse_args()
    if args.stage:
        manifest = stage_level(args.source, args.output)
    else:
        manifest = inventory_level(args.source)
        target = Path(args.output).resolve()
        source = Path(args.source).resolve()
        if target == source or source in target.parents:
            raise ValueError("Write report outside the original level")
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(f"Indexed {len(manifest['files'])} level files. Client loading not tested.")

if __name__ == "__main__":
    main()
