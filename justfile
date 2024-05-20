default:
    @just --list

dirty:
    git clean -xd --force --dry-run

clean:
    ./gradlew clean

lint:
    ./gradlew validatePlugins
    ./gradlew detekt --rerun-tasks

build:
    ./gradlew build

test:
    ./gradlew test --rerun
    ./gradlew testAggregateTestReport
