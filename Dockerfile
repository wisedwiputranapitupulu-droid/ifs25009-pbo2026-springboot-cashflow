# ---------- Tahap 1: build JAR (wajib profil production Vaadin) ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B clean package -Pproduction -DskipTests

# ---------- Tahap 2: jalankan ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8080
# SerialGC + C1 saja: start & respons pertama lebih cepat, memori kecil (cocok untuk app kecil di container)
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=75 -Xshare:auto"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
