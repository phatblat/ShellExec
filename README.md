# ShellExec

A simpler extension point than [`Exec`](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html) for ad-hoc Gradle tasks that run shell commands.

## Features

- Specify entire `command` line in one string (instead of `List<CharSequence>`).
- Append/prepend to the current `PATH`.
- Execute a `command | pipe` or `conditional && command || chain`.
- Easy access to `stdout` and `stderr`.

:warning: Not compatible with `Exec` tasks yet (see [#11](https://github.com/phatblat/ShellExec/issues/11)), but the current API is very similar.

## Example `build.gradle.kts`

```kts
import at.phatbl.shellexec.ShellExec

buildscript {
    repositories.gradlePluginPortal()
    dependencies.classpath("at.phatbl:shellexec:+")
}

val lolBoxFortune by registering(ShellExec::class) {
    command = "fortune | boxes --design parchment | lolcat"
}
```

> Note that the `boxes` and `lolcat` tools need to be installed for the above `lolBoxFortune` to work.

### Task Output

```text
> Task :lolBoxFortune
 ____________
/\           \
\_| Ship it. |
  |          |
  |   _______|_
   \_/_________/


BUILD SUCCESSFUL in 2s
1 actionable task: 1 executed
```

## License

This repo is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for rights and limitations.
