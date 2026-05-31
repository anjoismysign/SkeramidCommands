# Example 3: Shop Command — Deep Nesting & the Child/Parameter Rule

This example teaches two things:

1. **Deep nesting** — children of children (grandchildren). A `Command` returned by `child()` is itself a `Command`, so you can call `child()` on it again.
2. **The child/parameter rule in action** — why `ChildNotAllowedException` is thrown and how to design your tree to avoid it.

---

## Project Structure

```
src/
  main/
    resources/
      plugin.yml
    java/com/example/shop/
      ShopPlugin.java
      ShopCommand.java
```

---

## `plugin.yml`

```yaml
name: ShopPlugin
main: com.example.shop.ShopPlugin
version: 1.0.0
api-version: 1.21

commands:
  shop:
    description: In-game shop
    permission: shopplugin.shop
```

---

## `ShopPlugin.java`

```java
package com.example.shop;

import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new ShopCommand(this);
    }
}
```

---

## `ShopCommand.java`

```java
package com.example.shop;

import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandBuilder;
import io.github.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.CommandTargetBuilder;
import io.github.anjoismysign.skeramidcommands.commandtarget.LogicCommandParameters;

import java.util.Map;

public class ShopCommand {

    public ShopCommand(ShopPlugin plugin) {

        // ╔══════════════════════════════════════════════════════════════════╗
        // ║  LEVEL 1: Root                                                 ║
        // ║  /shop                                                         ║
        // ╚══════════════════════════════════════════════════════════════════╝

        Command shop = CommandBuilder
                .of("shop")
                .permission("shopplugin.shop")
                .description("In-game shop")
                .onExecute((sender, args) -> {
                    sender.sendMessage("§6=== Shop ===");
                    sender.sendMessage("§e/shop buy <item> <amount>");
                    sender.sendMessage("§e/shop sell <item> <amount>");
                    sender.sendMessage("§e/shop balance");
                })
                .build();

        // ╔══════════════════════════════════════════════════════════════════╗
        // ║  LEVEL 2: Children of root                                     ║
        // ║  /shop buy     → a child with children (grandchildren below)   ║
        // ║  /shop sell    → a child with children (grandchildren below)   ║
        // ║  /shop balance → a leaf child (no children, no parameters)     ║
        // ╚══════════════════════════════════════════════════════════════════╝

        // ── /shop balance ─────────────────────────────────────────────────
        // Permission: shopplugin.shop.balance
        Command balanceCmd = shop.child("balance", "Check your shop balance");
        balanceCmd.onExecute((sender, args) -> {
            // Permission shopplugin.shop.balance is checked automatically
            sender.sendMessage("§aYour balance: 1000 coins");
        });

        // ── /shop buy ─────────────────────────────────────────────────────
        // Permission: shopplugin.shop.buy
        //
        // This child has NO parameters — it has CHILDREN (grandchildren of root).
        Command buyCmd = shop.child("buy", "Buy items from the shop");
        buyCmd.onExecute((sender, args) -> {
            sender.sendMessage("§cUsage: /shop buy <item> <amount>");
            sender.sendMessage("§cAvailable: diamond, iron_ingot, emerald");
        });

        // ── /shop sell ────────────────────────────────────────────────────
        // Permission: shopplugin.shop.sell
        Command sellCmd = shop.child("sell", "Sell items to the shop");
        sellCmd.onExecute((sender, args) -> {
            sender.sendMessage("§cUsage: /shop sell <item> <amount>");
        });

        // ╔══════════════════════════════════════════════════════════════════╗
        // ║  LEVEL 3: Grandchildren (children of Level 2 children)         ║
        // ║                                                               ║
        // ║  Here's the key point: buyCmd and sellCmd are also `Command`,  ║
        // ║  so child() works on them too.                                 ║
        // ║                                                               ║
        // ║  These grandchildren have PARAMETERS instead of children.      ║
        // ╚══════════════════════════════════════════════════════════════════╝

        // ── /shop buy diamond ─────────────────────────────────────────────
        // Permission: shopplugin.shop.buy.diamond
        Command buyDiamond = buyCmd.child("diamond", "Buy diamonds");
        buyDiamond.setParameters(LogicCommandParameters.POSITIVE_INTEGER());  // <amount>
        buyDiamond.onExecute((sender, args) -> {
            // Permission shopplugin.shop.buy.diamond checked automatically
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /shop buy diamond <amount>");
                return;
            }
            var amount = LogicCommandParameters.POSITIVE_INTEGER().parse(args[0]);
            if (amount == null || amount <= 0) {
                sender.sendMessage("§cInvalid amount.");
                return;
            }
            // Buy logic...
            sender.sendMessage("§aBought " + amount + " diamond(s) for " + (amount * 100) + " coins.");
        });

        // ── /shop buy iron_ingot ──────────────────────────────────────────
        // Permission: shopplugin.shop.buy.iron_ingot
        Command buyIron = buyCmd.child("iron_ingot", "Buy iron ingots");
        buyIron.setParameters(LogicCommandParameters.POSITIVE_INTEGER());
        buyIron.onExecute((sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /shop buy iron_ingot <amount>");
                return;
            }
            var amount = LogicCommandParameters.POSITIVE_INTEGER().parse(args[0]);
            if (amount == null || amount <= 0) {
                sender.sendMessage("§cInvalid amount.");
                return;
            }
            sender.sendMessage("§aBought " + amount + " iron ingot(s) for " + (amount * 10) + " coins.");
        });

        // ── /shop sell diamond ────────────────────────────────────────────
        // Permission: shopplugin.shop.sell.diamond
        Command sellDiamond = sellCmd.child("diamond", "Sell diamonds");
        sellDiamond.setParameters(LogicCommandParameters.POSITIVE_INTEGER());
        sellDiamond.onExecute((sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /shop sell diamond <amount>");
                return;
            }
            var amount = LogicCommandParameters.POSITIVE_INTEGER().parse(args[0]);
            if (amount == null || amount <= 0) {
                sender.sendMessage("§cInvalid amount.");
                return;
            }
            sender.sendMessage("§aSold " + amount + " diamond(s) for " + (amount * 50) + " coins.");
        });

        // ── /shop sell iron_ingot ─────────────────────────────────────────
        // Permission: shopplugin.shop.sell.iron_ingot
        Command sellIron = sellCmd.child("iron_ingot", "Sell iron ingots");
        sellIron.setParameters(LogicCommandParameters.POSITIVE_INTEGER());
        sellIron.onExecute((sender, args) -> {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /shop sell iron_ingot <amount>");
                return;
            }
            var amount = LogicCommandParameters.POSITIVE_INTEGER().parse(args[0]);
            if (amount == null || amount <= 0) {
                sender.sendMessage("§cInvalid amount.");
                return;
            }
            sender.sendMessage("§aSold " + amount + " iron ingot(s) for " + (amount * 5) + " coins.");
        });
    }
}
```

---

## Command Tree Visualisation

```
/shop ........................................ permission: shopplugin.shop
  ├── balance ................................ permission: shopplugin.shop.balance
  │     (leaf — no parameters, no children)
  │
  ├── buy .................................... permission: shopplugin.shop.buy
  │     (has children — CANNOT have parameters)
  │     │
  │     ├── diamond .......................... permission: shopplugin.shop.buy.diamond
  │     │     (has parameters — CANNOT have children)
  │     │
  │     └── iron_ingot ....................... permission: shopplugin.shop.buy.iron_ingot
  │           (has parameters — CANNOT have children)
  │
  └── sell ................................... permission: shopplugin.shop.sell
        (has children — CANNOT have parameters)
        │
        ├── diamond .......................... permission: shopplugin.shop.sell.diamond
        │     (has parameters — CANNOT have children)
        │
        └── iron_ingot ....................... permission: shopplugin.shop.sell.iron_ingot
              (has parameters — CANNOT have children)
```

Notice the alternating pattern:

| Level | Name | Has children? | Has parameters? |
|---|---|---|---|
| 1 (root) | `shop` | Yes (`buy`, `sell`, `balance`) | No |
| 2 | `buy` | Yes (`diamond`, `iron_ingot`) | No |
| 3 | `diamond` (under buy) | No | Yes (`POSITIVE_INTEGER`) |

A level CAN have children OR parameters, not both. Once you go "down" with children, you can't also go "sideways" with parameters at the same level.

---

## What Happens If You Break the Rule?

```java
Command buy = shop.child("buy", "Buy items");
buy.setParameters(LogicCommandParameters.POSITIVE_INTEGER());  // OK so far

// This line throws ChildNotAllowedException:
Command buyDiamond = buy.child("diamond", "Buy diamonds");
// → "Attempted to add 'diamond' to 'buy' but was not possible
//    due to 'buy' having parameters."
```

The error is thrown from `FloorCommand.child()` (line 42 of `FloorCommand.java`) when `hasParameters()` is `true`.

---

## Permission Inheritance Across 3 Levels

| Command | Permission Node | Derived From |
|---|---|---|
| `/shop` | `shopplugin.shop` | Explicit |
| `/shop balance` | `shopplugin.shop.balance` | `shop` + `.balance` |
| `/shop buy` | `shopplugin.shop.buy` | `shop` + `.buy` |
| `/shop buy diamond` | `shopplugin.shop.buy.diamond` | `shop.buy` + `.diamond` |
| `/shop buy diamond 5` | `shopplugin.shop.buy.diamond` | Same as above — parameters don't change permission |
| `/shop sell` | `shopplugin.shop.sell` | `shop` + `.sell` |
| `/shop sell iron_ingot` | `shopplugin.shop.sell.iron_ingot` | `shop.sell` + `.iron_ingot` |

---

## Key Takeaways — Deep Nesting & the Rule

1. **`child()` returns a `Command`** — you can chain `child()` calls:  
   `shop.child("buy").child("diamond")`

2. **The child/parameter rule is checked at runtime** — `ChildNotAllowedException` is a `RuntimeException`. Always design your tree so that a command has children **OR** parameters, never both.

3. **Usage is shared** — `FloorCommand.getUsage()` delegates to the parent `PyramidCommand`. All children share the root's usage list.

4. **Permissions chain down** — each level appends `.childName` to the parent's permission.

5. **Tab-completion works automatically** — the library walks the child tree and `CommandTarget` list to provide suggestions at every depth.

---

## See Also

- [`example-warp-command.md`](example-warp-command.md) — basic child commands (Level 1 only)
- [`example-moderation-command.md`](example-moderation-command.md) — children with typed parameters
