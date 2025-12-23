> [!Warning]
> This repository is a stub. While ByteCraft works, it does not include
> any additional syntaxes for interacting with the Bukkit server.

# ByteCraft

ByteCraft is a ByteSkript library that provides Bukkit server integration capabilities.

## Prerequisites

- Java 21 or higher
- ByteSkript compiler
- Bukkit server

## Installation

1. Build the project using Maven: `mvn package`
2. Move the compiled archive from `target/bytecraft-<VERSION>.jar` to your ByteSkript project's `libraries/` directory
3. Place your scripts in the project's `skript/` directory. You may use this as an example:
   ```
    on load:
        trigger:
            print "Hello from Test!"
            print "The server is on version " + getBukkitVersion() of the server
    ```
4. Place your `plugin.yml` in the project's `resources/` directory
   following this example:
    ```yaml
    # Has to be exactly this for ByteCraft to load your scripts
    main: blue.lhf.bytecraft.runtime.BukkitHook$CompiledHook
    
    # Name of your plugin
    name: MyTestPlugin
    
    # Plugin version and minimum Minecraft version
    version: 1.0.0
    api-version: "1.21"
    ```
5. Run ByteSkript as normal: `java -jar ByteSkript-<VERSION>.jar jar` (a total of three `jar`s)
6. Get your plugin from `compiled/CompiledScripts.jar` and place it in your server's `plugins/` folder
7. Restart your server
8. Enjoy!