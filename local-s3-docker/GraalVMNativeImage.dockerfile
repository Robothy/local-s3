FROM frolvlad/alpine-glibc:glibc-2.34

MAINTAINER Fuxiang Luo <robothyluo@gmail.com>

WORKDIR /app

COPY build/bin/s3 /app/s3

EXPOSE 80

CMD exec ./s3