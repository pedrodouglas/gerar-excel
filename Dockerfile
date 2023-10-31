# Use an official OpenJDK runtime as a parent image
FROM adoptopenjdk:11-jre-hotspot

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged JAR file into the container at the specified path
COPY target/gerar-excel-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the application when the container launches
CMD ["java", "-jar", "app.jar"]
