FROM eclipse-temurin:17

RUN addgroup --system app && adduser --system --ingroup app app
USER app:app

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","za.co.sheeptracker"]
