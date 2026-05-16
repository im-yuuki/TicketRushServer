# Testing

This project uses Gradle + JUnit 5.

## Prerequisites

- Java 25
- Gradle wrapper: `./gradlew`

## Run All Tests

```bash
./gradlew test
```

## Available Test Suites

### Controller tests

- `me.june8th.ticketrushserver.controllers.AuthControllerTest`
  - register/login/logout/reset request flows
- `me.june8th.ticketrushserver.controllers.ErrorHandlerTest`
  - exception to HTTP status mapping
- `me.june8th.ticketrushserver.controllers.FeedsControllerTest`
  - promoted/trending/recommended feed endpoints
- `me.june8th.ticketrushserver.controllers.MyAccountControllerAuthenticatedTest`
  - authenticated `/account` endpoints
- `me.june8th.ticketrushserver.controllers.UserControllerAuthenticatedTest`
  - authenticated `/user/tickets` and `/user/avatar`
- `me.june8th.ticketrushserver.controllers.PurchaseControllerAuthenticatedTest`
  - authenticated `/purchase/**` endpoints

### Service tests

- `me.june8th.ticketrushserver.services.FeedServiceTest`
  - promoted/trending/recommended feed ranking logic

### Utility and security tests

- `me.june8th.ticketrushserver.security.AccessTokenProviderTest`
  - JWT generation and parsing
- `me.june8th.ticketrushserver.utils.ValidatorTest`
  - validation rules and image header checks

## Run Specific Tests

Run one class:

```bash
./gradlew test --tests "me.june8th.ticketrushserver.controllers.AuthControllerTest"
```

Run one method:

```bash
./gradlew test --tests "me.june8th.ticketrushserver.controllers.AuthControllerTest.login_shouldSetAccessTokenCookie"
```

Run all authenticated controller tests:

```bash
./gradlew test --tests "*AuthenticatedTest"
```

Run all controller tests:

```bash
./gradlew test --tests "me.june8th.ticketrushserver.controllers.*"
```

Run all service tests:

```bash
./gradlew test --tests "me.june8th.ticketrushserver.services.*"
```

## Authenticated Test Account

Authenticated controller tests read their test account from:

- `src/test/resources/application-test.yml`

Only the test credentials live there. All other application config is inherited from:

- `src/main/resources/application.yaml`

Current keys:

- `test.auth.account.id`
- `test.auth.account.name`
- `test.auth.account.email`
- `test.auth.account.role`
- `test.auth.account.domain`
- `test.auth.account.token-version`
- `test.auth.account.avatar-key`

Current authenticated controller tests expect:

```yml
test:
  auth:
    account:
      role: USER
```

## Test Configuration

Tests use the main application config from:

- `src/main/resources/application.yaml`

Authenticated test-only credentials live in:

- `src/test/resources/application-test.yml`

Current test dependencies are declared in:

- `build.gradle`

## Notes

- The current suite is mostly controller/unit level and does not require PostgreSQL or Redis to be running.
- Testcontainers dependencies are already included for future integration tests.
