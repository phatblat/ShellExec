# ShellExec CHANGELOG

## Unreleased



## 1.0.1

- Fixed publishing metadata #22
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
