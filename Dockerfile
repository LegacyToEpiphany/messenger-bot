FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/movie-finder.jar /movie-finder/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/movie-finder/app.jar"]
