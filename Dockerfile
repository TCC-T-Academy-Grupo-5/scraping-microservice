FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y wget gnupg2 && \
    wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    sh -c 'echo "deb [arch=amd64] https://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list' && \
    apt-get update && \
    apt-get install -y google-chrome-stable && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV CHROMEDRIVER_PATH=/usr/local/bin/chromedriver
ENV CHROME_BIN=/usr/bin/google-chrome

WORKDIR /app

COPY . /app

COPY ./src/main/resources/chromedriver /usr/local/bin/chromedriver
RUN chmod +x /usr/local/bin/chromedriver

RUN ./mvnw clean package -DskipTests

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "target/scraping-microservice-0.0.1-SNAPSHOT.jar"]