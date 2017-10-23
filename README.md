# SimpleExec

A simpler extension point than `Exec` for ad-hoc Gradle tasks that run shell commands.

## Features

- [x] Specify entire command line in one string (instead of `List<CharSequence>`).
- [ ] Append/prepend to the current `PATH`.
- [ ] Execute a command pipe or conditional command chain.
- [ ] Easy access to `stdout` and `stderr`.

## Example `build.gradle`

```gradle
task shellCommand(type: SimpleExec) {
    command "fortune | cowsay | lolcat"
}
```

## License

This repo is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for rights and limitations.
