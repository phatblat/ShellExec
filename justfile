default:
    @just --list

dirty:
    git clean -xd --force --dry-run

clean:
    ./gradlew clean
    git clean -xd --force

lint:
    ./gradlew validatePlugins
    ./gradlew detekt --rerun-tasks

build:
    ./gradlew build

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
