# Example 2: Moderation Command — Children with Parameters

This example teaches how child commands can **themselves have parameters** (`setParameters`). A child command that takes arguments (like `/ban <player> <reason>`) needs `CommandTarget` entries for tab-completion and parsing, even though it's already a child of a root command.

**Key rule:** A command can have **parameters OR children, not both**. A child `Command` can have parameters. It cannot then also have grandchildren.

---

## Project Structure

```
src/
  main/
    resources/
      plugin.yml
    java/com/example/moderation/
      ModerationPlugin.java
      ModerationCommand.java
```

---

## `plugin.yml`

```yaml
name: ModerationPlugin
main: com.example.moderation.ModerationPlugin
version: 1.0.0
api-version: 1.21

commands:
  mod:
    description: Moderation commands
    permission: modplugin.mod
```

---

## `ModerationPlugin.java`

```java
package com.example.moderation;

import org.bukkit.plugin.java.JavaPlugin;

public class ModerationPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new ModerationCommand(this);
    }
}
```

---

## `ModerationCommand.java`

```java
package com.example.moderation;

import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import io.github.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.LogicCommandParameters;

public class ModerationCommand {

    public ModerationCommand(ModerationPlugin plugin) {

        // ──────────────────────────────────────────────────────────────────
        // STEP 1 — Root command
        // ──────────────────────────────────────────────────────────────────

        Command mod = CommandBuilder
                .of("mod")
                .permission("modplugin.mod")
                .description("Moderation commands")
                .onExecute((sender, args) -> {
                    // Permission: modplugin.mod
                    sender.sendMessage("§cUsage: /mod <ban | kick | mute | warn>");
                })
                .build();

        // ──────────────────────────────────────────────────────────────────
        // STEP 2 — Child: /mod ban <player> <reason>
        // ──────────────────────────────────────────────────────────────────
        //
        // This child has PARAMETERS, not children of its own.
        // It is still created via child() — it's a child of the root.
        // After that, we call setParameters() to define its arguments.

        // Permission: modplugin.mod.ban
        Command banCmd = mod.child("ban", "Ban a player");
        banCmd.setParameters(
                BukkitCommandTarget.ONLINE_PLAYERS(),   // arg 0: target player
                LogicCommandParameters.STRING()          // arg 1: reason (free text)
        );
        banCmd.onExecute((sender, args) -> {
            // Permission modplugin.mod.ban is checked automatically.
            // The handler only runs if the sender passes isAuthorized().
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /mod ban <player> <reason>");
                return;
            }

            // Parse arg 0 — player name
            var target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return;
            }

            // args[1] onward is the reason
            String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            // Ban logic...
            sender.sendMessage("§cBanned " + target.getName() + ": " + reason);
            target.kickPlayer("Banned: " + reason);
        });

        // ──────────────────────────────────────────────────────────────────
        // STEP 3 — Child: /mod kick <player>
        // ──────────────────────────────────────────────────────────────────

        // Permission: modplugin.mod.kick
        Command kickCmd = mod.child("kick", "Kick a player");
        kickCmd.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        kickCmd.onExecute((sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /mod kick <player>");
                return;
            }
            var target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return;
            }
            target.kickPlayer("Kicked by a moderator.");
            sender.sendMessage("§eKicked " + target.getName());
        });

        // ──────────────────────────────────────────────────────────────────
        // STEP 4 — Child: /mod mute <player> <minutes>
        // ──────────────────────────────────────────────────────────────────

        // Permission: modplugin.mod.mute
        Command muteCmd = mod.child("mute", "Mute a player");
        muteCmd.setParameters(
                BukkitCommandTarget.ONLINE_PLAYERS(),
                LogicCommandParameters.POSITIVE_INTEGER()
        );
        muteCmd.onExecute((sender, args) -> {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /mod mute <player> <minutes>");
                return;
            }
            var target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return;
            }
            var minutes = LogicCommandParameters.POSITIVE_INTEGER().parse(args[1]);
            if (minutes == null) {
                sender.sendMessage("§cInvalid minutes: " + args[1]);
                return;
            }
            // Mute logic...
            sender.sendMessage("§eMuted " + target.getName() + " for " + minutes + " minute(s).");
        });

        // ──────────────────────────────────────────────────────────────────
        // STEP 5 — Child: /mod warn <player>
        // ──────────────────────────────────────────────────────────────────

        // Permission: modplugin.mod.warn
        Command warnCmd = mod.child("warn", "Warn a player");
        warnCmd.setParameters(BukkitCommandTarget.ONLINE_PLAYERS());
        warnCmd.onExecute((sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /mod warn <player>");
                return;
            }
            var target = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return;
            }
            // Warn logic...
            sender.sendMessage("§eWarned " + target.getName());
        });
    }
}
```

> **What if I try to add a grandchild to `banCmd`?**  
> `banCmd.child("perm", "Permanent ban")` would throw `ChildNotAllowedException` because `banCmd` has parameters (`setParameters` was called). A command can have **parameters or children, never both**.

---

## What `LogicCommandParameters.STRING()` ?

`LogicCommandParameters.STRING()` doesn't exist in the current library. The library provides `INTEGER`, `POSITIVE_INTEGER`, `NEGATIVE_INTEGER`, `BYTE`, `POSITIVE_BYTE`, `NEGATIVE_BYTE`, and `BOOLEAN`. For free-text arguments (like a ban reason), you can use a simple inline target:

```java
CommandTarget<String> FREE_TEXT = new CommandTarget<>() {
    @Override
    public List<String> get() {
        return List.of();  // no tab suggestions for free text
    }

    @Override
    public String parse(String arg) {
        return arg;  // pass through
    }
};
```

Or use `CommandTargetBuilder.fromMap()` for reason presets.

---

## Permission Table

| Command | Permission Node |
|---|---|
| `/mod` | `modplugin.mod` |
| `/mod ban <player> <reason>` | `modplugin.mod.ban` |
| `/mod kick <player>` | `modplugin.mod.kick` |
| `/mod mute <player> <minutes>` | `modplugin.mod.mute` |
| `/mod warn <player>` | `modplugin.mod.warn` |

---

## Key Takeaways — Children with Parameters

**A child command CAN have parameters:**
```java
Command ban = root.child("ban", "Ban a player");
ban.setParameters(BukkitCommandTarget.ONLINE_PLAYERS(), ...);
```

**But then it CANNOT have children:**
```java
// This throws ChildNotAllowedException:
ban.child("perm", "Permanent ban");
```

**The child/parameter rule applies to ALL commands, not just the root:**
- A `PyramidCommand` (root) can have children OR parameters
- A `FloorCommand` (child) can have children OR parameters
- Neither can have both

**How to parse typed arguments inside `onExecute`:**
```java
var player = BukkitCommandTarget.ONLINE_PLAYERS().parse(args[0]);
var amount = LogicCommandParameters.POSITIVE_INTEGER().parse(args[1]);
```

Always parse with the **same** `CommandTarget` that provides tab completions — this keeps suggestions and validation consistent.

---

## See Also

- [`example-warp-command.md`](example-warp-command.md) — basic child commands without parameters
- [`example-shop-command.md`](example-shop-command.md) — deep nesting (grandchildren) and the child/parameter rule
