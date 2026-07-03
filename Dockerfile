FROM eclipse-temurin:25-jre-alpine
VOLUME /logs
EXPOSE 8080
ARG JAR_FILE
COPY ${JAR_FILE} html-pdf-service.war
COPY additionalFonts/* /usr/share/fonts/TTF/
RUN apk --no-cache add ttf-opensans fontconfig && \
    fc-cache -f && \
    addgroup -S appgroup && adduser -S appuser -G appgroup && \
    chown appuser:appgroup /html-pdf-service.war
USER appuser
ENTRYPOINT ["java","-jar","/html-pdf-service.war"]
