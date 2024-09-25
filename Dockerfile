FROM eclipse-temurin:21-alpine
VOLUME /logs
EXPOSE 8080
ARG JAR_FILE
ADD ${JAR_FILE} html-pdf-service.war
RUN apk --no-cache add ttf-opensans fontconfig
COPY additionalFonts/* /usr/share/fonts/TTF/
RUN fc-cache -f
ENTRYPOINT ["java","-jar","/html-pdf-service.war"]