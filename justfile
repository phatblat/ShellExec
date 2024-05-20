default:
    @just --list

clean:
    ./gradlew clean

build:
    ./gradlew build

test:
    ./gradlew test --rerun
