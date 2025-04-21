# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY ./ ./
RUN gradle clean build --no-daemon -x test


# Runtime stage
FROM openjdk:21-slim
WORKDIR /app

# Copy built artifact from builder stage
COPY --from=builder /app/build/libs/BOOK_BE-0.0.1-SNAPSHOT.jar .

# Set environment variables
ENV TZ=Asia/Seoul
ENV PROFILE=dev
ENV PORT=8080

# Expose port
EXPOSE ${PORT}

# Set entrypoint
ENTRYPOINT ["java", \
            "-jar", \
            "-Dspring.profiles.active=${PROFILE}", \
            "-Dserver.port=${PORT}", \
            "-Duser.timezone=${TZ}", \
            "BOOK_BE-0.0.1-SNAPSHOT.jar"]

