# ===========================
# Stage 1 - Build Angular UI
# ===========================
FROM node:20 AS ui-builder

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
RUN mkdir -p boot/src/main/resources/static

COPY --from=ui-builder /ui/dist/ui/browser/ boot/src/main/resources/static/

RUN chmod +x boot/gradlew

WORKDIR /app/boot

RUN ./gradlew bootJar -x test

# ===========================
# Stage 3 - Runtime
# ===========================
FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=builder /app/boot/build/libs/tp2intervals.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]