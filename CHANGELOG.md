# 📝 ShellExec CHANGELOG

## Unreleased

- ✨ Gradle logging to file/s #59

## 1.4.1

- 👐🏻 Open API #58

## 1.4.0

- ♻️ Refactor `stdout` & `stderr` convenience properties #56
  - 💥 Return type is now nullable
  - Output now goes to temp files when not using streams
  - New `ShellCommandTimeoutException` is thrown with details when a command doesn't complete before the current timeout.
- `baseDir` argument is now optional #56
- ⬆️ Bintray plugin (1.8.4) #55

## 1.3.0

- ⏱ Add timeout property #54
- ⬆️🐘 Gradle (5.5.1)
- ⬆️🐘 Gradle (5.5)
- ➖🔌 Clamp
- ⬆️🧠 Kotlin (1.3.41) #52
- ⬆️🐘 Gradle (4.10.2) #50

## 1.2.0

- 🐛 Fixed string version of standard output from commands #47
- ⬆️ Kotlin (1.2.71) #48
- ⬆️🐘 Gradle (4.7) #46
- ⬆️ Kotlin (1.2.41) #46
- ⬆️ JUnit Platform (1.2.0) #46
- ⬆️ Clamp (1.1.0) #46
- ⬆️🔌 ShellExec (1.1.3) #45

## 1.1.3

- ➕ Clamp ([1.0.0](https://github.com/phatblat/Clamp/releases/tag/1.0.0)). #44
- ⬆️ Kotlin (1.2.31). #43

## 1.1.2

- 🐛Fixed issue when current directory contains space. #38
- 👮‍♀️ Update build badge. #40
- 🔧 Update Gradle configuration. #39
- ⬆️ Kotlin (1.2.30) #39
- ⬆️ JUnit Platform (1.1.0) #39
- ⬆️ Detekt (1.0.0.RC6-4) #39
- ⬆️ Publishing Plugin (0.9.10) #39

## 1.1.1

- 🐛 Set default `command` to empty string. Allows for `command` to be populated in `preExec`. #36
- ⬆️ Upgraded Gradle wrapper to [4.6](https://github.com/gradle/gradle/releases/tag/v4.6.0). #33

## 1.1.0

- Added `preExec` and `postExec` hooks for custom logic. #31

## 1.0.2

- Published to [Gradle Plugin Portal](https://plugins.gradle.org/plugin/at.phatbl.shellexec). #27

## 1.0.1

- Fixed publishing metadata. #22
  - artifactId is now `shellexec`

## 1.0.0

- Initial release
  - Simpler API than Exec (`command` string vs. `commandLine` list)
  - Append/prepend to `PATH`
  - Supports command pipes and conditional command chains
  - Easy access to `stdout`/`stderr`

⚠️ Not compatible with `Exec` tasks yet (#11), but the current API is very similar.

## 0.1.0

- Test release
- Original name: [SimpleExec](https://bintray.com/phatblat/maven-open-source/SimpleExec)
