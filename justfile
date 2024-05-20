default:
    @just --list

dirty:
    git clean -xd --force --dry-run

clean:
    ./gradlew clean
    git clean -xd --force

dev-deps:
    ./gradlew buildEnvironment

deps:
    ./gradlew dependencies --configuration runtimeClasspath
    ./gradlew dependencies --configuration implementation

test-deps:
    ./gradlew dependencies --configuration testCompileClasspath
    ./gradlew dependencies --configuration testRuntimeClasspath
    ./gradlew dependencies --configuration testImplementation

lint:
    ./gradlew validatePlugins
    ./gradlew detekt --rerun-tasks
    ./gradlew ktlintCheck

format:
    ./gradlew ktlintFormat

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
