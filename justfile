default:
    @just --list

build:
    ./gradlew build

test:
    ./gradlew test --rerun
