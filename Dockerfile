FROM ubuntu:22.04

# 기본 패키지 설치 및 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 필요한 패키지 설치
RUN apt-get update && \
    apt-get install -y \
    openjdk-11-jdk \
    curl \
    gnupg2 \
    && rm -rf /var/lib/apt/lists/*

# SBT 설치
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y sbt && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 프로젝트 파일 복사
COPY . .

# SBT로 프로젝트 빌드
RUN sbt clean compile stage

# 실행 가능한 JAR 파일 위치
ENV APP_JAR=/app/target/universal/stage/bin/re-elfin-summary-data-service

# 포트 노출
EXPOSE 9000

# 애플리케이션 실행
CMD ["sh", "-c", "java -jar ${APP_JAR}"]
