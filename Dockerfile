# ===========================
# Stage 1 - Build Angular UI
# ===========================
FROM node:22.23.1-bookworm-slim AS ui-builder

WORKDIR /ui

COPY ui/package*.json ./

RUN npm ci

COPY ui/ .

RUN npm run build

# ===========================
# Stage 2 - Build Spring Boot
# ===========================
FROM amazoncorretto:21 AS builder

WORKDIR /app

COPY . .

# copiar frontend compilado
RUN rm -rf boot/src/main/resources/static && mkdir -p boot/src/main/resources/static

COPY --from=ui-builder /ui/dist/ui/browser/ boot/src/main/resources/static/

RUN chmod +x boot/gradlew

WORKDIR /app/boot

RUN ./gradlew --no-daemon bootJar -x test

# ===========================
# Stage 3 - Runtime
# ===========================
FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/boot/build/libs/workout-relay.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]