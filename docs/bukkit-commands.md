# Bukkit Commands with SkeramidCommands

SkeramidCommands provides a **pyramid-style hierarchical command system** for Bukkit (and Sponge) Minecraft servers. Instead of manually writing `CommandExecutor` and `TabCompleter` classes, you build a tree of _pyramid commands_ and _floor commands_ with typed parameters, automatic permission inheritance, and fluent tab completion — all through a single, uniform API.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Command Tree Model](#command-tree-model)
  - [PyramidCommand (root)](#pyramidcommand-root)
  - [FloorCommand (sub-command)](#floorcommand-sub-command)
  - [The `Command` Interface](#the-command-interface)
- [Command Registration Flow](#command-registration-flow)
  - [Registering in `plugin.yml`](#registering-in-pluginyml)
  - [Creating a Pyramid at Runtime](#creating-a-pyramid-at-runtime)
- [Permissions](#permissions)
  - [Inheritance & Registration](#inheritance--registration)
- [Execution Flow](#execution-flow)
- [Tab Completion](#tab-completion)
- [Typed Parameters (CommandTarget)](#typed-parameters-commandtarget)
  - [Bukkit-builtin Targets](#bukkit-builtin-targets)
  - [Logical / Primitive Targets](#logical--primitive-targets)
  - [Custom Targets](#custom-targets)
- [API Reference](#api-reference)
  - [`Command` (interface)](#command-interface)
  - [`PyramidCommand`](#pyramidcommand)
  - [`FloorCommand`](#floorcommand)
  - [`CommandBuilder`](#commandbuilder)
  - [`SkeramidCommandsAPI`](#skeramidcommandsapi)
  - [`BukkitAdapter`](#bukkitadapter)
  - [`BukkitPyramid`](#bukkitpyramid)
  - [`CommandSenderAdapter`](#commandsenderadapter)
  - [`PermissionMessenger`](#permissionmessenger)
  - [`CommandTarget<T>`](#commandtargett)
  - [`BukkitCommandTarget`](#bukkitcommandtarget)
  - [`CommandTargetBuilder`](#commandtargetbuilder)
  - [`LogicCommandParameters`](#logiccommandparameters)
- [Child / Parameter Rule](#child--parameter-rule)
- [Examples](#examples)

---

## Architecture Overview

```
Bukkit CommandSender
      │
      ▼
 BukkitPluginCommand (registered in plugin.yml)
      │
      ▼
 BukkitPyramid  ─── implements ─── CommandExecutor, TabCompleter
      │
      ▼
 PyramidCommand  ─── implements ─── Command  (the root)
      │
      ├── FloorCommand (sub-command)  ─── implements ─── Command
      │
      └── CommandTarget<T>  (typed parameters for tab-complete & parse)
```

The library auto-detects whether the server runs Bukkit or Sponge via `SkeramidCommandsAPI.getInstance()` and uses the appropriate adapter.

---

## Prerequisites

- **Paper 1.21.5+** (or any Bukkit/Spigot fork with the Paper API)
- **Java 16+**
- `SkeramidCommands` and `skeramid` added as dependencies (compile scope)

```xml
<dependency>
    <groupId>io.github.anjoismysign</groupId>
    <artifactId>skeramidcommands</artifactId>
    <version>1.0.6</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>io.github.anjoismysign</groupId>
    <artifactId>skeramid</artifactId>
    <version>1.0.1</version>
    <scope>compile</scope>
</dependency>
```

---

## Quick Start

**1.** Declare the root command in `plugin.yml`:

```yaml
name: MyPlugin
main: com.example.MyPlugin
version: 1.0
commands:
  warp:
    description: Warp to a location
    permission: myplugin.warp
```

**2.** Create a `BukkitPyramid` from the registered command, attach sub-commands, and set execution handlers:

```java
BukkitPyramid pyramid = BukkitPyramid.of("warp", "myplugin.warp", "Warp to a location");

// Add sub-command: /warp list
Command listCmd = pyramid.getPyramid().child("list", "List available warps");
listCmd.onExecute((sender, args) -> {
    sender.sendMessage("Available warps: spawn, hub, pvp");
});

// Add sub-command with parameter: /warp <name>
Command warpTo = pyramid.getPyramid().child("warpTo", "Warp to a specific warp");
warpTo.onExecute((sender, args) -> {
    if (args.length > 0) {
        sender.sendMessage("Warping to " + args[0]);
    }
});
```

All permissions, tab-completers, and executors are registered automatically.

---

## Command Tree Model

### PyramidCommand (root)

`PyramidCommand` is the top-level node. Created through `BukkitPyramid.of(...)` or `SkeramidCommandsAPI.getInstance().createCommand(...)`. It holds:

- **name** – the command name (must match `plugin.yml`)
- **permission** – the permission string
- **description** – a short description
- **children** – a list of `FloorCommand` instances
- **parameters** – a list of `CommandTarget<?>` for tab completion and argument parsing
- **usage** – usage lines shown when the command is invoked incorrectly
- **onExecute** – a `BiConsumer<PermissionMessenger, String[]>` that runs when the command is executed

### FloorCommand (sub-command)

`FloorCommand` is a child node in the pyramid. Created via `parent.child("name", "description")`. It:

- Reuses the parent's usage list (shared reference)
- Derives its permission from the parent: `parentPermission + "." + args.join(".")`
- If the parent permission is empty, all children also have empty permissions
- Can itself have children (unless it has parameters)
- Can have its own parameters

### The `Command` Interface

```java
public interface Command extends Skeramid<Command> {
    @NotNull Command child(@NotNull String name, @Nullable String description);
    @NotNull default Command child(@NotNull String name);

    @NotNull List<CommandTarget<?>> getParameters();
    void setParameters(CommandTarget<?>... targets);

    @NotNull List<String> getUsage();
    void addUsage(@NotNull String... usage);
    default void addUsage(@NotNull List<String> usage);

    default boolean hasParameters();

    void onExecute(BiConsumer<PermissionMessenger, String[]> consumer);
    boolean run(PermissionMessenger permissionMessenger, String... args);
    boolean isAuthorized(PermissionMessenger permissionMessenger);

    @NotNull String getName();
    @NotNull String getPermission();
    @NotNull String getDescription();
}
```

The `Skeramid<T>` super-interface (from the `skeramid` library) provides:

| Method | Description |
|---|---|
| `@NotNull T getParent()` | Returns the parent command. For `PyramidCommand`, returns itself. |
| `@NotNull T child(String argument)` | Shortcut for `child(name, null)`. |

---

## Command Registration Flow

### Registering in `plugin.yml`

Every root command **must** be declared in `plugin.yml` so Bukkit creates a `PluginCommand` object. SkeramidCommands then hooks into that object:

```yaml
commands:
  mycommand:
    description: My awesome command
    permission: myplugin.mycommand
```

The permission node in `plugin.yml` is optional — `BukkitPyramid.of()` uses the permission you pass programmatically. If omitted, the command name is used as the default permission.

### Creating a Pyramid at Runtime

Two factory methods on `BukkitPyramid`:

| Method | Behaviour |
|---|---|
| `BukkitPyramid.of(String name, String permission, String description)` | Creates a new `PyramidCommand` and attaches it as executor/tab-completer for the Bukkit `PluginCommand` matching `name`. |
| `BukkitPyramid.ofBukkitCommand(String name)` | Reads permission and description from the Bukkit `PluginCommand` (as declared in `plugin.yml`) and creates the pyramid. |

Both methods require the command to be registered in `plugin.yml` (throws `NullPointerException` otherwise).

```java
// Option A: specify all values
BukkitPyramid bp = BukkitPyramid.of("warp", "myplugin.warp", "Teleport to warps");

// Option B: read from plugin.yml
BukkitPyramid bp = BukkitPyramid.ofBukkitCommand("warp");
```

---

## Permissions

### Inheritance & Registration

- **Root** permission is whatever you pass (or read from `plugin.yml`).
- **FloorCommand** permission is computed as:  
  `parentPermission + "." + args.stream().map(String::toLowerCase).collect(Collectors.joining("."))`
- If the parent permission is `""` (empty), all children also get `""`.
- Every permission is automatically registered with Bukkit's `PermissionManager` as `OP`-default via `BukkitPermissionRegistry`. If a permission node is already registered, it is not re-registered.

Example hierarchy:

| Command | Permission Node |
|---|---|
| `/warp` | `myplugin.warp` |
| `/warp list` | `myplugin.warp.list` |
| `/warp set` | `myplugin.warp.set` |
| `/warp set spawn` | `myplugin.warp.set.spawn` |

Permission checking is done inside `run()` and `isAuthorized()`:

```java
// User-facing check
if (!command.isAuthorized(sender)) { ... }
```

---

## Execution Flow

When a player (or console) runs a command, the following happens:

```
Bukkit onCommand(sender, cmd, label, args[])
    │
    ▼
BukkitPyramid.onCommand(sender, cmd, label, args[])
    │  adapts sender → CommandSenderAdapter
    │  wraps args  → List<String>
    ▼
PyramidCommand.execute(PermissionMessenger, List<String> args)
    │
    ├── args.isEmpty()
    │   ├── hasParameters() = false → run(sender)                 // run root handler
    │   └── hasParameters() = true  → sendUsage(sender)           // show usage
    │
    └── args not empty
        │
        ├── child found by exact name?
        │   ├── child.hasParameters() → child.run(sender, subArgs)
        │   └── !child.hasParameters() → child.run(sender)
        │
        └── no child found
            ├── this.hasParameters() → run(sender, subArgs)       // treat args as params
            └── !this.hasParameters() → return false              // unknown command
```

- `run()` checks `isAuthorized()` first → returns `false` if not authorized.
- `sendUsage()` prints the command name header, all usage lines, and lists available sub-commands with descriptions.

---

## Tab Completion

`PyramidCommand.tabComplete(PermissionMessenger, List<String> args)` is automatically wired to Bukkit's `TabCompleter` via `BukkitPyramid`.

The algorithm:
1. If `args` is empty, return `null` (Bukkit's default).
2. Build a prefix from all args except the last.
3. Filter children whose name starts with the prefix and match the current depth.
4. Return all matching child names at the current argument position.
5. If no child matches, look for a `CommandTarget` at the corresponding parameter index.
6. If the target is a `KeyedCommandTarget` and a child was matched, call `get(key)` with the previously typed argument.

---

## Typed Parameters (CommandTarget)

`CommandTarget<T>` is the interface for argument tab-completion and parsing:

```java
public interface CommandTarget<T> {
    List<String> get();              // tab-completion suggestions
    @Nullable T parse(String arg);   // parse an argument into typed value
}
```

Parameters are positional. Set them in the order they appear in the command:

```java
command.setParameters(
    BukkitCommandTarget.ONLINE_PLAYERS(),
    LogicCommandParameters.POSITIVE_INTEGER()
);
```

### Bukkit-builtin Targets

From `BukkitCommandTarget`:

| Factory Method | Return Type | Tab Suggestions | `parse()` |
|---|---|---|---|
| `ONLINE_PLAYERS()` | `CommandTarget<Player>` | Online player names | `Bukkit.getPlayer(arg)` |
| `WORLD_NAMES()` | `CommandTarget<World>` | Loaded world names | `Bukkit.getWorld(arg)` |
| `OF_REGISTRY_KEY(RegistryKey<T>)` | `CommandTarget<T>` | All `NamespacedKey` keys in the Paper registry | `registry.get(NamespacedKey.minecraft(arg))` |

Example: `OF_REGISTRY_KEY(RegistryKey.MOB_EFFECT)` yields all potion-effect names.

### Logical / Primitive Targets

From `LogicCommandParameters`:

| Factory Method | Type | Tab Suggestions | `parse()` |
|---|---|---|---|
| `INTEGER()` | `Integer` | Example integers | `Integer.parseInt` |
| `POSITIVE_INTEGER()` | `Integer` | Positive examples | `parseInt` + `> 0` check |
| `NEGATIVE_INTEGER()` | `Integer` | Negative examples | `parseInt` + `< 0` check |
| `BYTE()` | `Byte` | Example bytes | `Byte.parseByte` |
| `POSITIVE_BYTE()` | `Byte` | Positive examples | `parseByte` + `> 0` check |
| `NEGATIVE_BYTE()` | `Byte` | Negative examples | `parseByte` + `< 0` check |
| `BOOLEAN()` | `Boolean` | `true`, `false` | case-insensitive parse |

### Custom Targets

Use `CommandTargetBuilder`:

```java
// From an enum
CommandTarget<MyEnum> target = CommandTargetBuilder.fromEnum(MyEnum.class);

// From a supplier of Map<String, T>
CommandTarget<MyObject> target = CommandTargetBuilder.fromMap(() -> myRegistry);
```

Or implement `CommandTarget<T>` directly:

```java
command.setParameters(new CommandTarget<MyType>() {
    @Override
    public List<String> get() {
        return List.of("foo", "bar");
    }

    @Override
    public MyType parse(String arg) {
        return myLookup.get(arg);
    }
});
```

---

## API Reference

### `Command` (interface)

| Method | Returns | Description |
|---|---|---|
| `child(String name)` | `Command` | Gets or creates a child (null description). Inherited from `Skeramid`. |
| `child(String name, String description)` | `Command` | Gets or creates a child with a description. |
| `getName()` | `String` | The command name. |
| `getPermission()` | `String` | The permission node. |
| `getDescription()` | `String` | The description text. |
| `getParameters()` | `List<CommandTarget<?>>` | Typed tab-complete parameters. |
| `setParameters(CommandTarget<?>...)` | `void` | Replace all parameters. |
| `getUsage()` | `List<String>` | Usage lines (shared with parent for `FloorCommand`). |
| `addUsage(String...)` | `void` | Add usage lines. |
| `hasParameters()` | `boolean` | `true` if `getParameters()` is non-empty. |
| `onExecute(BiConsumer<PermissionMessenger, String[]>)` | `void` | Set the execution handler. |
| `run(PermissionMessenger, String...)` | `boolean` | Execute the handler (checks permission first). |
| `isAuthorized(PermissionMessenger)` | `boolean` | Check permission against the messenger. |
| `getParent()` | `Command` | Parent node (`PyramidCommand` returns itself). |

### `PyramidCommand`

All `Command` methods plus:

| Method | Returns | Description |
|---|---|---|
| `getChildren()` | `List<FloorCommand>` | Direct child sub-commands. |
| `findChildren(String name)` | `Command` or `null` | Find a child by full name (space-joined). |
| `execute(PermissionMessenger, List<String>)` | `boolean` | Entry point for `onCommand`. Routes to the correct child or parameter handler. |
| `tabComplete(PermissionMessenger, List<String>)` | `List<String>` or `null` | Entry point for `onTabComplete`. |
| `sendUsage(PermissionMessenger)` | `void` | Print command usage help. |

### `FloorCommand`

All `Command` methods plus:

| Method | Returns | Description |
|---|---|---|
| `getParent()` | `Command` | The parent `PyramidCommand`. |

Note: `getUsage()` and `addUsage()` delegate to the parent pyramid — usage is shared.

### `CommandBuilder`

Fluent builder for creating commands:

```java
Command cmd = CommandBuilder.of("name")
    .permission("myplugin.name")
    .description("Does something")
    .onExecute((sender, args) -> { ... })
    .build();
```

| Method | Returns | Description |
|---|---|---|
| `of(String name)` | `CommandBuilder` | Create a builder. Defaults: `permission = name`, `description = ""`. |
| `name(String)` | `CommandBuilder` | Set command name. |
| `permission(String)` | `CommandBuilder` | Set permission node. |
| `description(String)` | `CommandBuilder` | Set description. |
| `onExecute(BiConsumer)` | `CommandBuilder` | Set execution handler. |
| `build()` | `Command` | Create the command via `SkeramidCommandsAPI`. |

### `SkeramidCommandsAPI`

Singleton API facade.

| Method | Returns | Description |
|---|---|---|
| `getInstance()` | `SkeramidCommandsAPI` | Get or create the singleton. Auto-detects Bukkit/Sponge. |
| `hasAdapter()` | `boolean` | Whether a server adapter was found. |
| `adapt(Object)` | `PermissionMessenger` | Adapt a server object (e.g., `CommandSender`) to `PermissionMessenger`. |
| `createCommand(String, String, String)` | `Command` | Create a root command via the current adapter. |
| `registerPermission(String, String)` | `void` | Register a permission in the server's permission manager. |

### `BukkitAdapter`

Implements `ServerAdapter<CommandSender>`. Singleton.

| Method | Returns | Description |
|---|---|---|
| `getInstance()` | `BukkitAdapter` | Returns the singleton. |
| `adapt(Object)` | `CommandSenderAdapter` | Wraps `CommandSender` → `PermissionMessenger`. |
| `of(PermissionMessenger)` | `CommandSender` | Unwraps `PermissionMessenger` → `CommandSender`. |
| `createCommand(String, String, String)` | `Command` | Delegates to `BukkitPyramid.of(...)`. |
| `ofBukkitCommand(String)` | `Command` | Delegates to `BukkitPyramid.ofBukkitCommand(...)`. |
| `registerPermission(String, String)` | `void` | Delegates to `BukkitPermissionRegistry`. |
| `getServerAPI()` | `ServerAPI` | Returns `ServerAPI.BUKKIT`. |

### `BukkitPyramid`

The Bukkit bridge. Implements `CommandExecutor` and `TabCompleter`.

| Method | Returns | Description |
|---|---|---|
| `of(String, String, String)` | `BukkitPyramid` | Create pyramid, register executor + tab completer on the Bukkit `PluginCommand`. |
| `ofBukkitCommand(String)` | `BukkitPyramid` | Same, but reads permission/description from `plugin.yml`. |
| `getPyramid()` | `PyramidCommand` | Get the underlying `PyramidCommand`. |
| `onCommand(CommandSender, Command, String, String[])` | `boolean` | Bukkit entry point — delegates to `pyramid.execute(...)`. |
| `onTabComplete(CommandSender, Command, String, String[])` | `List<String>` | Bukkit entry point — delegates to `pyramid.tabComplete(...)`. |

### `CommandSenderAdapter`

Implements `PermissionMessenger`. Wraps a Bukkit `CommandSender`.

| Method | Returns | Description |
|---|---|---|
| `hasPermission(String)` | `boolean` | Delegates to `CommandSender.hasPermission()`. |
| `sendMessage(String)` | `void` | Delegates to `CommandSender.sendMessage()`. |
| `getCommandSender()` | `CommandSender` | Return the underlying Bukkit sender. |

### `PermissionMessenger`

Server-agnostic abstraction for a command sender.

| Method | Returns | Description |
|---|---|---|
| `hasPermission(String)` | `boolean` | Permission check. |
| `sendMessage(String)` | `void` | Send a message. |

### `CommandTarget<T>`

| Method | Returns | Description |
|---|---|---|
| `get()` | `List<String>` | Tab-completion suggestions for the parameter position. |
| `parse(String)` | `T` or `null` | Parse the typed argument into the target type (null if invalid). |

### `BukkitCommandTarget`

Static factory for Bukkit-specific `CommandTarget` implementations.

| Method | Returns | Description |
|---|---|---|
| `ONLINE_PLAYERS()` | `CommandTarget<Player>` | Tab-complete online player names. |
| `WORLD_NAMES()` | `CommandTarget<World>` | Tab-complete loaded world names. |
| `OF_REGISTRY_KEY(RegistryKey<T>)` | `CommandTarget<T>` | Tab-complete all keys from a Paper registry (e.g., mob effects, enchantments). |

### `CommandTargetBuilder`

| Method | Returns | Description |
|---|---|---|
| `fromEnum(Class<T>)` | `CommandTarget<T>` | Tab-complete enum constant names. |
| `fromMap(Supplier<Map<String, T>>)` | `CommandTarget<T>` | Tab-complete map keys; parse returns the value. |

### `LogicCommandParameters`

Static factories for primitive/logical parameters:

| Method | Returns |
|---|---|
| `INTEGER()` | `CommandTarget<Integer>` |
| `POSITIVE_INTEGER()` | `CommandTarget<Integer>` |
| `NEGATIVE_INTEGER()` | `CommandTarget<Integer>` |
| `BYTE()` | `CommandTarget<Byte>` |
| `POSITIVE_BYTE()` | `CommandTarget<Byte>` |
| `NEGATIVE_BYTE()` | `CommandTarget<Byte>` |
| `BOOLEAN()` | `CommandTarget<Boolean>` |

---

## Child / Parameter Rule

A command **cannot** have both children and parameters. If you call `child(...)` on a command that already has parameters, a `ChildNotAllowedException` is thrown. Conversely, calling `setParameters(...)` does not automatically clear children — ensure you design the tree so that leaf nodes with parameters never receive children.

| Parent has parameters? | Can add children? |
|---|---|
| No | Yes |
| Yes | No (throws `ChildNotAllowedException`) |

---

## Examples

Walkthrough examples are located in `docs/examples/`. Each teaches child commands from a different angle:

| File | What it teaches |
|---|---|
| [`example-warp-command.md`](examples/example-warp-command.md) | **Basic child commands** — `CommandBuilder` + `child()` to create `/warp list`, `/warp set`, `/warp delete`. Permission inheritance table. No parameters on children (pure sub-command tree). |
| [`example-moderation-command.md`](examples/example-moderation-command.md) | **Children with parameters** — `/mod ban <player> <reason>`, `/mod mute <player> <minutes>`. Calls `setParameters()` on a child `Command`. Shows the child/parameter rule: a parameterized child cannot also have grandchildren. |
| [`example-shop-command.md`](examples/example-shop-command.md) | **Deep nesting & the child/parameter rule** — 3-level hierarchy: `/shop` → `buy` → `diamond <amount>`. Shows that `child()` returns a `Command` so you can chain it. Visualises the tree and explains `ChildNotAllowedException`. |

---

## See Also

- `SkeramidCommandsAPI` — singleton entry point (auto-detects Bukkit/Sponge)
- `BukkitPermissionRegistry` — Bukkit permission registration
- `ChildNotAllowedException` — thrown when trying to add children to a parameterized command
