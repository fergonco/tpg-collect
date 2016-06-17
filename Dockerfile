FROM java:7
ADD target/api-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/collect.jar
CMD java -jar /usr/local/collect.jar
