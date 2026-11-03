# Aion 4.8: isolated level staging experiment

No Aion 4.8 client level files are present in the GitHub server repository. This experiment is a safe preparation step, **not** proof of client-level editing.

## On Windows, with an authorized local client

1. Locate an existing level folder in the Aion 4.8 client. Verify its exact path rather than assuming the layout.
2. Run `python tools/silkroad/stage_aion_level.py --source "C:\\path\\to\\client\\level" --output "C:\\aion-tests\\level-copy" --stage` from the repository checkout. The destination must not exist.
3. Review `aion-level-manifest.json`: file names, byte sizes and SHA-256 checksums. Original files remain untouched.
4. Inspect copied files using a viewer supporting the exact client build. The tool **does not** parse or modify CryEngine .pak archives.
5. Only after format verification, choose a reversible one-object or texture modification on a separate test installation. Validate rendering, collision, and server world registration; never patch production or bypass integrity checks.

Do not distribute extracted third-party assets or assume any arbitrary new world ID is supported. The staged copy is not automatically loadable by the client.

Test: `python -m unittest discover -s tools/silkroad -p 'test_*.py' -v`.
