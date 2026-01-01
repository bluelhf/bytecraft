# Examples

This directory contains some examples for using Bytecraft. Each build of Bytecraft verifies that these still compile  correctly, so they should never break. If they do, please file an issue on the repository.

## Usage

To use any of these examples:
1. Ensure you have ByteSkript working in your project directory. If you do, you should have a file structure that looks like this:
    ```
    .
    ├── byteskript.jar
    ├── compiled
    ├── libraries
    ├── resources
    └── skript
    ```
2. Download or compile the ByteCraft .JAR and place it in `libraries/`:
    ```
    .
    ├── byteskript.jar
    ├── compiled
    ├── libraries
    │   └── bytecraft-bootstrap.jar
    ├── resources
    └── skript
    ```
3. Copy some scripts from one of the example directories to `skript/`:
    ```
    .
    ├── byteskript.jar
    ├── compiled
    ├── libraries
    │   └── bytecraft-bootstrap.jar
    ├── resources
    └── skript
        └── <... bsk files copied over from the example ...>
    ```
4. Navigate to the top directory and run this command in your terminal:
    ```shell
    java -jar byteskript.jar jar Example
    ```
5. If everything went well, you'll find your plugin in `compiled/Example.jar`. Place it in your Minecraft server's `plugins/` folder and enjoy!