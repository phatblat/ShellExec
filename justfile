default:
    @just --list

clean:
    ./gradlew clean

lint:
    ./gradlew lint --rerun-tasks

build:
    ./gradlew build

test:
    ./gradlew test --rerun
