# Motadata Service Name Detector

Provides automatic `service.name` detection for OpenTelemetry Java applications running with the Motadata Java Agent.

## Overview

This module implements a ResourceProvider that automatically detects the application's service name when it has not been explicitly configured by the customer. It uses a chain of detectors with a well-defined priority order to determine the service name from various sources.

## Detection Priority

The detectors run in this priority order (first match wins):

1. **Explicit OpenTelemetry Configuration** - Highest Priority
   - `otel.service.name` system property or environment variable
   - `service.name` in `otel.resource.attributes`

2. **Spring Boot Application Name**
   - `spring.application.name` system property
   - `SPRING_APPLICATION_NAME` environment variable
   - Note: For more comprehensive Spring Boot detection (from configuration files, command-line arguments, etc.), the upstream OpenTelemetry Spring Boot ResourceProvider is also loaded

3. **JAR Filename**
   - Extracts service name from the JAR file name
   - Examples:
     - `java -jar payment-service.jar` → `payment-service`
     - `java -jar payment-service-1.2.3.jar` → `payment-service-1.2.3`

4. **Main Class Name**
   - Extracts the simple class name from the main class
   - Examples:
     - `java com.example.PaymentApplication` → `PaymentApplication`
     - `java com.company.payment.PaymentService` → `PaymentService`

5. **OpenTelemetry Default** - Lowest Priority
   - If no detector succeeds, OpenTelemetry's default `unknown_service:java` is used

## Configuration

### Enable/Disable Detection

```bash
# Enable automatic detection (default: true)
-Dotel.motadata.service.name.detection.enabled=true

# Disable automatic detection
-Dotel.motadata.service.name.detection.enabled=false
```

### Debug Logging

```bash
# Enable debug logging for service name detection
-Dotel.motadata.service.name.detection.debug=true
```

### Standard OpenTelemetry Configuration

Customer-provided configuration always takes precedence:

```bash
# Explicit service name (highest priority)
-Dotel.service.name=my-service

# Or via environment variable
export OTEL_SERVICE_NAME=my-service

# Or via resource attributes
-Dotel.resource.attributes=service.name=my-service
```

## Examples

### Standalone JAR Application

```bash
java -javaagent:motadata-javaagent.jar \
     -jar payment-service.jar
```

**Result**: `service.name=payment-service` (detected from JAR filename)

### Spring Boot Application

```bash
java -javaagent:motadata-javaagent.jar \
     -Dspring.application.name=order-service \
     -jar application.jar
```

**Result**: `service.name=order-service` (detected from Spring Boot configuration)

### Main Class Application

```bash
java -javaagent:motadata-javaagent.jar \
     -cp ./lib/app.jar \
     com.company.PaymentProcessor
```

**Result**: `service.name=PaymentProcessor` (detected from main class name)

### Explicit Configuration (Highest Priority)

```bash
java -javaagent:motadata-javaagent.jar \
     -Dotel.service.name=my-custom-service \
     -jar application.jar
```

**Result**: `service.name=my-custom-service` (explicit configuration, detection is skipped)

## Architecture

### Components

- **MotadataServiceNameResourceProvider** - Main SPI-registered provider that implements `ConditionalResourceProvider`
- **ServiceNameDetectionChain** - Orchestrates detectors in priority order
- **ServiceNameDetector** - Interface for individual detection strategies
- **Detectors**:
  - `ExplicitServiceNameDetector` - Checks OTel configuration
  - `SpringBootServiceNameDetector` - Checks Spring Boot properties/env
  - `JarNameServiceDetector` - Extracts from JAR filename
  - `MainClassServiceDetector` - Extracts from main class name

### How It Works

1. When the OpenTelemetry SDK initializes, it loads all ResourceProviders via SPI
2. The SDK checks `shouldApply()` on each provider to see if it should run
3. For MotadataServiceNameResourceProvider:
   - Only applies if detection is enabled AND no explicit service.name is configured
   - Runs the detection chain to find a service name
4. The detection chain tries each detector in order until one succeeds
5. The result is returned as a Resource attribute

### Order Value

The provider uses `order()` value of **500**, which means:
- It runs after OpenTelemetry's standard detectors (default order ~0-100)
- It runs before custom user extensions (typical order > 1000)
- This ensures compatibility with upstream OTel while allowing customization

## Performance Characteristics

- **Detection Timing**: Only during SDK initialization, not per-span
- **Overhead**: Minimal - simple string operations, no filesystem scans or network calls
- **Thread Safety**: Detection result is cached in the Resource object; no repeated detection
- **Caching**: Result is used for the entire application lifetime

## Limitations

### Tomcat Multi-WAR Scenario

The current OpenTelemetry architecture uses a single Resource per SDK instance. In Tomcat environments hosting multiple WARs:

- All WARs in the same Tomcat JVM share the same Resource
- Therefore, all WARs will have the same detected `service.name`
- This is a limitation of the OTel architecture, not this implementation

**Workaround**: Use explicit configuration for each WAR when running multiple applications in one JVM:

```bash
# Set via environment variable or system property per application
OTEL_SERVICE_NAME=payment-service java -jar tomcat/bin/catalina.sh run
```

### Detection Reliability

Detection relies on:
- Java command-line arguments being preserved in `sun.java.command`
- Spring Boot properties being available as system properties
- JAR filename following naming conventions

Some deployment scenarios (containerization, systemd, custom launchers) may not preserve these signals reliably. In such cases, explicit configuration is recommended.

## Testing

The module includes comprehensive unit tests:

- `MotadataServiceNameResourceProviderTest` - Tests the main provider
- `ExplicitServiceNameDetectorTest` - Tests OTel config detection
- `SpringBootServiceNameDetectorTest` - Tests Spring Boot detection
- `JarNameServiceDetectorTest` - Tests JAR filename detection
- `MainClassServiceDetectorTest` - Tests main class detection

Run tests with:

```bash
./gradlew :instrumentation:resources:motadata-service-detector:test
```

## Integration with OpenTelemetry

This module:
- Uses only public, stable OpenTelemetry SPI (`ResourceProvider`, `ConditionalResourceProvider`)
- Does NOT modify or patch OpenTelemetry core classes
- Works alongside other ResourceProviders (Spring Boot, Container, Host, etc.)
- Respects OpenTelemetry's configuration precedence
- Is easy to remove or upgrade when OpenTelemetry's built-in detection improves

## Future Enhancements

Possible improvements (not yet implemented):

- More sophisticated Spring Boot detection (reading configuration files directly)
- Tomcat context name detection (if architecture allows)
- Kubernetes downward API detection
- Environment variable prefixes for Motadata-specific settings
- Custom detector plugin mechanism
