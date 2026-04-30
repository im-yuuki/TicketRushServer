FROM eclipse-temurin:25-jdk-alpine AS builder

ENV NVM_DIR=/root/.nvm

RUN apk add --no-cache bash curl && \
    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash && \
    bash -c ". \"$NVM_DIR/nvm.sh\" && \
        nvm install --lts && \
        nvm use --lts && \
        ln -sf \"\$(nvm which current)\" /usr/local/bin/node && \
        ln -sf \"\$(dirname \"\$(nvm which current)\")/npm\" /usr/local/bin/npm && \
        ln -sf \"\$(dirname \"\$(nvm which current)\")/npx\" /usr/local/bin/npx"

WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:25-jre-alpine
LABEL authors="Yuuki"
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
