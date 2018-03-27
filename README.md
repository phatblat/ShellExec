# ShellExec

[![Tube](https://jenkins.log-g.co/buildStatus/icon?job=phatblat/ShellExec/master)](https://jenkins.log-g.co/job/phatblat/job/ShellExec/job/master/)
[ ![Download](https://api.bintray.com/packages/phatblat/maven-open-source/ShellExec/images/download.svg) ](https://bintray.com/phatblat/maven-open-source/ShellExec/_latestVersion)

A simpler extension point than [`Exec`](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html) for ad-hoc Gradle tasks that run shell commands.

## Features

- Specify entire `command` line in one string (instead of `List<CharSequence>`).
- Append/prepend to the current `PATH`.
- Execute a `command | pipe` or `conditional && command || chain`.
- Easy access to `stdout` and `stderr`.

:warning: Not compatible with `Exec` tasks yet (see [#11](https://github.com/phatblat/ShellExec/issues/11)), but the current API is very similar.

## Example `build.gradle`

```gradle
buildscript {
    repositories.jcenter()
    dependencies.classpath 'at.phatbl:shellexec:+'
}

import at.phatbl.shellexec.ShellExec

task lolCowFortune(type: ShellExec) {
    command "fortune | cowsay | lolcat"
}
```

### Task Output

```
> Task :lolCowFortune
 _________________________________________
/ Do not clog intellect's sluices with    \
\ bits of knowledge of questionable uses. /
 -----------------------------------------
        \   ^__^
         \  (oo)\_______
            (__)\       )\/\
                ||----w |
                ||     ||


BUILD SUCCESSFUL in 2s
1 actionable task: 1 executed
```

## License

This repo is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for rights and limitations.
