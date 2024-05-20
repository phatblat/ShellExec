default:
    @just --list

dirty:
    git clean -xd --force --dry-run

clean:
    ./gradlew clean
    git clean -xd --force

deps:
    ./gradlew buildEnvironment
    ./gradlew dependencies

lint:
    ./gradlew validatePlugins
    ./gradlew detekt --rerun-tasks
    ./gradlew ktlintCheck

build:
    ./gradlew assemble

test:
    ./gradlew test --rerun
    ./gradlew testAggregateTestReport

example:
    ./gradlew --project-dir example/ --continue \
        customPath \
        customTask \
        emptyCommand \
        errorLogging \
        failingCommand \
        helloWorld \
        lolBoxFortune \
        successfulCommand || true
