# VersatiaExample-Lifecycle-Reload

A plugin designed for reload: values re-read by `/versatia reload announcer`, a derived value rebuilt, a repeating task rescheduled only when its interval changes, and a reload started from code.

Part of the [Versatia examples](https://github.com/VersatiaFramework/VersatiaExamples); documentation: [Reloading plugins](https://versatia.dev/docs/lifecycle/reload).

**Start with** [`Announcer.kt`](src/main/kotlin/dev/versatia/example/reload/Announcer.kt), then [`AnnouncerPlugin.kt`](src/main/kotlin/dev/versatia/example/reload/AnnouncerPlugin.kt); the configuration is [`resources/config.yml`](src/main/resources/resources/config.yml).

## What the log shows

```
[VersatiaExample-Lifecycle-Reload] Announcement: Tip: edit config.yml and run /versatia reload announcer
[VersatiaExample-Lifecycle-Reload] Announcing every 30 s
(every 30 s) Tip: edit config.yml and run /versatia reload announcer
```

## Things to try

- Edit `plugins/VersatiaExample-Lifecycle-Reload/config.yml`, change `text` to
  `"<green>Reloaded!</green>"` and run `/versatia reload announcer`: `Announcement: Reloaded!` is logged, and
  `Interval still 30 s, the task keeps running`, because the interval did not change; the next
  broadcast already says `Reloaded!`, since the task reads the property.
- Set `interval-seconds: 10` and reload: `Interval 30 s -> 10 s, task rescheduled`. The bean is
  the one created at start; only the task is replaced.
- Set `auto-reload: true` and reload once. From now on saving the file is enough: within two
  seconds the log shows `config.yml changed, reloading` and the lines of a reload, started from
  code with `versatiaReload()`.
- With `auto-reload` on, set `interval-seconds: soon` and save: `Reload failed, the previous
  values stay: ...` names the key, and the announcement keeps its old text and interval.
- In `apply`, copy the announcement into a local `val line = announcement` and broadcast `line`
  from the task; then change only `text` and reload. The log shows the new announcement, but the
  broadcasts keep the old one: the task was not rescheduled and holds a copy, which is why it
  reads the property instead.

## Building

Requires JDK 25 and VersatiaAPI in your
local Maven repository (`./gradlew publishToMavenLocal` in each checkout; see
[Building the framework](https://versatia.dev/docs/contributing/building));
running it requires a Paper 26.3 server with VersatiaCore installed, and the
[CommandAPI](https://commandapi.jorel.dev) 12.x plugin for `/versatia reload` (without it, the
plugin still reloads itself with `auto-reload: true`).

```bash
./gradlew build
```

To have the JAR copied to your server after every build, copy `local.properties.example` to
`local.properties` and set `pluginsDir`. The generated descriptor is in
`build/generated/ksp/main/kotlin`; open it to see exactly what the runtime executes.
