FROM openjdk:17-ea-slim

COPY build/libs/s3.jar /app/s3.jar

WORKDIR /app

VOLUME /data

EXPOSE 80

CMD exec java -jar s3.jar