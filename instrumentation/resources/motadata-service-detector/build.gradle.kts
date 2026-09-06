plugins {
  id("otel.sdk-extension")
}

dependencies {
  compileOnly("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi")
  implementation("io.opentelemetry:opentelemetry-sdk-common")
  implementation("io.opentelemetry.semconv:opentelemetry-semconv")

  annotationProcessor("com.google.auto.service:auto-service")
  compileOnly("com.google.auto.service:auto-service-annotations")
  testCompileOnly("com.google.auto.service:auto-service-annotations")

  testImplementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito:mockito-junit-jupiter")
}
