default:
    @just --list

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
