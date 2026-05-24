FROM eclipse-temurin:21-jdk

COPY . /project

WORKDIR /project

RUN find src -name "*.java" > sources.txt
RUN javac -d bin -cp "lib/*" @sources.txt

CMD ["java", "-cp", "bin:lib/*", "main"]