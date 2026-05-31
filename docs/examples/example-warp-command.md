# Example 1: Warp Command — Basic Child Commands

This example teaches **how to use child commands** — the most important feature of SkeramidCommands. A child is a sub-command created via `Command#child(name, description)`. Each child gets its own permission (inherited from the parent) and its own `onExecute` handler.

---

## Project Structure

```
src/
  main/
    resources/
      plugin.yml
    java/com/example/warps/
      WarpPlugin.java
      WarpCommand.java
```

---

## `plugin.yml`

```yaml
name: WarpPlugin
main: com.example.warps.WarpPlugin
version: 1.0.0
api-version: 1.21

commands:
  warp:
    description: Warp teleportation system
    permission: warpplugin.warp
```

Every root command **must** appear in `plugin.yml`. The permission here is optional — you'll set it programmatically anyway.

---

## `WarpPlugin.java`

```java
package com.example.warps;

import org.bukkit.plugin.java.JavaPlugin;

public class WarpPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // The command pyramid is self-contained in the command class
        new WarpCommand(this);
    }
}
```

---

## `WarpCommand.java`

```java
package com.example.warps;

import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import org.bukkit.entity.Player;

public class WarpCommand {

    public WarpCommand(WarpPlugin plugin) {

        // ──────────────────────────────────────────────────────────────────
        // STEP 1 — Create the root command with CommandBuilder
        // ──────────────────────────────────────────────────────────────────

        Command warp = CommandBuilder
                .of("warp")                                    // must match plugin.yml
                .permission("warpplugin.warp")                  // root permission
                .description("Warp teleportation system")
                .onExecute((sender, args) -> {
                    // Runs when user types: /warp  (no arguments)
                    // Permission required: warpplugin.warp
                    sender.sendMessage("§cUsage: /warp <list | set | delete | <name>>");
                })
                .build();

        // ──────────────────────────────────────────────────────────────────
        // STEP 2 — Add child commands via child()
        // ──────────────────────────────────────────────────────────────────
        //
        // Command#child(name, description) does THREE things:
        //   1. Creates a FloorCommand with that name
        //   2. Adds it to the parent's children list
        //   3. Registers the child's permission automatically
        //
        // The child's permission is derived from the parent:
        //   parent permission + "." + child name
        //
        // So "warpplugin.warp" + "." + "list" = "warpplugin.warp.list"
        //
        // child() returns a Command — the SAME type as the root.
        // You can call child() on it again for deeper nesting.

        // ── Child: /warp list ─────────────────────────────────────────
        // Permission: warpplugin.warp.list
        Command listCmd = warp.child("list", "List all available warps");
        listCmd.onExecute((sender, args) -> {
            // The library checks warpplugin.warp.list BEFORE calling this.
            // If the sender doesn't have it, onExecute never fires.
            sender.sendMessage("§6Available warps: spawn, hub, pvp");
        });

        // ── Child: /warp set ──────────────────────────────────────────
        // Permission: warpplugin.warp.set
        Command setCmd = warp.child("set", "Set a warp at your location");
        setCmd.onExecute((sender, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can set warps.");
                return;
            }
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /warp set <name>");
                return;
            }
            // Save warp logic...
            sender.sendMessage("§aWarp '" + args[0] + "' saved.");
        });

        // ── Child: /warp delete ───────────────────────────────────────
        // Permission: warpplugin.warp.delete
        Command deleteCmd = warp.child("delete", "Delete a warp");
        deleteCmd.onExecute((sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /warp delete <name>");
                return;
            }
            // Delete warp logic...
            sender.sendMessage("§aWarp '" + args[0] + "' deleted.");
        });
    }
}
```

---

## Permission Table

| Command | Permission Node | How it's derived |
|---|---|---|
| `/warp` | `warpplugin.warp` | Set explicitly in `CommandBuilder.permission()` |
| `/warp list` | `warpplugin.warp.list` | Parent `warpplugin.warp` + `"."` + `"list"` |
| `/warp set` | `warpplugin.warp.set` | Parent `warpplugin.warp` + `"."` + `"set"` |
| `/warp delete` | `warpplugin.warp.delete` | Parent `warpplugin.warp` + `"."` + `"delete"` |

The inheritance formula is:

```
childPermission = parentPermission.isEmpty() ? "" : parentPermission + "." + childName
```

If the parent permission is empty (`""`), all children also get `""` (no permission check).

---

## Key Takeaways — Child Commands

**How to create a child:**
```java
Command child = parent.child("name", "description");
```

**What `child()` returns:**
A `Command` instance — the same type as the parent. You can:
- Call `child.onExecute(...)` to set its handler
- Call `child.child(...)` to create a grandchild (deeper nesting)
- Call `child.setParameters(...)` to add typed parameters
- Call `child.addUsage(...)` (usage is shared with the parent pyramid)

**What `child()` does automatically:**
1. Creates the `FloorCommand` object
2. Adds it to the parent's children list
3. Registers the child's permission with Bukkit's permission manager (via `BukkitPermissionRegistry`)

**What the library does for you:**
1. Routes `/warp list` → `listCmd.onExecute`
2. Checks `warpplugin.warp.list` permission before invoking the handler
3. Provides tab-completion for child names automatically

**What happens when the user types an unknown sub-command:**
If no child matches, the root's `onExecute` runs with the args. This is how `/warp pvp` (without a dedicated child) can still work — the root handler receives `args = ["pvp"]`.

---

## See Also

- [`example-moderation-command.md`](example-moderation-command.md) — adding `setParameters()` to child commands
- [`example-shop-command.md`](example-shop-command.md) — deep nesting (grandchildren) and the child/parameter rule
