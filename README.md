# SimpleExec

[![Pipeline Status](http://jenkins.log-g.co/buildStatus/icon?job=SimpleExec/master)](http://jenkins.log-g.co/job/SimpleExec/job/master/)

A simpler extension point than `Exec` for ad-hoc Gradle tasks that run shell commands.

## Features

- [x] Specify entire command line in one string (instead of `List<CharSequence>`).
- [x] Append/prepend to the current `PATH`.
- [X] Execute a command pipe or conditional command chain.
- [ ] Easy access to `stdout` and `stderr`.

## Example `build.gradle`

```gradle
buildscript {
    repositories.jcenter()
    dependencies.classpath 'at.phatbl:simple-exec:+'
}

import at.phatbl.simple_exec.SimpleExec

task lolCowFortune(type: SimpleExec) {
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
