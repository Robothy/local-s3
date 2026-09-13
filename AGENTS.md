# AGENTS.md

## Scope

These instructions apply to the entire repository. Keep changes focused, preserve unrelated work, and follow any deeper `AGENTS.md` if one is added later.

## Project

LocalS3 is a Java 17 S3-compatible mock service. It supports in-memory and file-system storage, object versioning, S3 Vectors, JUnit 5, Testcontainers, and Java/Docker distributions.

- Build with the checked-in Gradle wrapper (`./gradlew`).
- Use `gradle/libs.versions.toml` as the dependency-version authority.
- Main library entry point: `com.robothy.s3.rest.LocalS3`.
- Docker entry point: `com.robothy.s3.docker.App`.
- Generated files under `build/` are disposable and must not be edited.

## Modules

- `local-s3-datatypes`: S3 and S3 Vectors wire models and serialization.
- `local-s3-core`: storage, metadata, object/versioning, multipart, bucket, and vector services.
- `local-s3-rest`: Netty server, routing, controllers, parsing, and serialization.
- `local-s3-jupiter`: JUnit 5 extension and client/endpoint injection.
- `local-s3-testcontainers`: LocalS3 Testcontainers integration.
- `local-s3-integrationtest`: AWS SDK integration tests and `@RealS3` support; not published.
- `local-s3-docker`: executable JAR, Docker images, and native-image tasks.

Keep HTTP/protocol behavior in `local-s3-rest`, domain and persistence behavior in `local-s3-core`, and wire models in `local-s3-datatypes`.

## Common Commands

Run commands from the repository root with JDK 17.

```sh
# Narrow test
./gradlew :local-s3-core:test --tests 'com.robothy.s3.core.service.PutObjectServiceTest'

# Module test
./gradlew :local-s3-core:test

# Unit-focused checks without Docker image builds
./gradlew :local-s3-datatypes:test :local-s3-core:test :local-s3-rest:test :local-s3-jupiter:test

# Compile or check all modules
./gradlew compileJava
./gradlew check

# CI-equivalent tests and merged coverage (requires Docker)
./gradlew mergeReports

# Build and run the executable JAR
./gradlew :local-s3-docker:jar
java -jar local-s3-docker/build/libs/s3.jar
```

`mergeReports` and `local-s3-docker:test` build Docker images. Testcontainers tests require Docker. `@RealS3` tests may contact AWS and must not be assumed offline-safe.

## Code Conventions

- Write Java 17-compatible, UTF-8 code with the existing two-space indentation and brace style.
- Prefer existing services, managers, controllers, models, and test helpers over new abstractions.
- Keep public APIs stable unless the task explicitly requires a breaking change.
- Use Lombok only where the surrounding code does, and use SLF4J for logging.
- Preserve ownership and shutdown behavior for Netty event loops, AWS clients, streams, files, and containers.
- Keep XML DTD and external-entity support disabled.
- Consider both `IN_MEMORY` and `PERSISTENCE` modes for storage changes, including versioning, delete markers, multipart operations, initial data, and restart persistence.
- Treat S3 Vectors as a first-class API, including filtering and pagination behavior.
- Keep comments concise and explain reasons rather than restating code.
- Avoid unrelated refactors and broad dependency upgrades.

## Testing

- Add focused JUnit Jupiter tests for changed behavior and failure paths.
- For storage behavior, reuse existing parameterized tests covering in-memory and file-system managers.
- Keep REST compatibility tests near the relevant parser, router, controller, or serializer.
- Close clients and clean up servers, containers, ports, and temporary directories.
- Run the narrowest relevant test first, then the affected module's test task.
- Run `./gradlew mergeReports` only when Docker and any required external services are available.
- Finish with `git diff --check` and `git status --short`.

## Safety

- Never expose or commit AWS, Docker Hub, Maven Central, signing, or other credentials. Use fake credentials for local endpoint tests.
- Review endpoint, region, bucket, and credential source before running `@RealS3` tests; they may be networked and billable.
- Do not run release, publish, signing, Docker push, or version-update tasks without explicit authorization.
- Do not overwrite persistent user data; use temporary directories in tests.
- Do not edit generated output under `build/`.
- Do not use destructive Git commands to discard work.
- Stage only explicit paths; never use `git add .` or `git add -A`.

## Workflow

1. Read the owning implementation and neighboring tests.
2. Make the smallest change in the module that owns the behavior.
3. Add or update focused tests.
4. Run the narrowest relevant validation, then the affected module's tests.
5. Inspect the final diff and leave unrelated changes untouched.