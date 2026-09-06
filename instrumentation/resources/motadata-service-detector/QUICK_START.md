# Motadata Service Name Detection - Quick Start

## Automatic Detection (Just Works!)

```bash
java -javaagent:motadata-javaagent.jar -jar payment-service.jar
# Result: service.name=payment-service ✅
```

The agent automatically detects your service name from:
1. **Explicit Config** - Highest priority
2. **Spring Boot** - If using Spring
3. **JAR Filename** - Default for standalone apps
4. **Main Class** - If running classpath app
5. **OpenTelemetry Default** - Fallback

## Configuration

### Explicit Service Name (Always Works)
```bash
java -javaagent:motadata-javaagent.jar \
     -Dotel.service.name=my-service \
     -jar app.jar
```

### Environment Variable
```bash
export OTEL_SERVICE_NAME=my-service
java -javaagent:motadata-javaagent.jar -jar app.jar
```

### Disable Auto-Detection
```bash
java -javaagent:motadata-javaagent.jar \
     -Dotel.motadata.service.name.detection.enabled=false \
     -jar app.jar
```

## Common Scenarios

### Spring Boot App
```bash
# Auto-detects from spring.application.name
java -javaagent:motadata-javaagent.jar \
     -Dspring.application.name=order-service \
     -jar application.jar
```

### Docker Container
```dockerfile
FROM openjdk:11
COPY motadata-javaagent.jar /app/
COPY app.jar /app/
ENTRYPOINT ["java", "-javaagent:/app/motadata-javaagent.jar", "-jar", "/app/app.jar"]
# Result: service.name will be auto-detected from app.jar filename
```

### Kubernetes Deployment
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: payment-service
spec:
  containers:
  - name: app
    image: my-app:latest
    env:
    - name: OTEL_SERVICE_NAME
      value: "payment-service"
    - name: OTEL_EXPORTER_OTLP_ENDPOINT
      value: "http://otel-collector:4317"
```

### Systemd Service
```ini
[Service]
Environment="OTEL_SERVICE_NAME=payment-service"
Environment="OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317"
ExecStart=/usr/bin/java \
  -javaagent:/opt/motadata-javaagent.jar \
  -jar /opt/app.jar
```

## Verify Detection

### View Telemetry Logs
With debug logging enabled:
```bash
java -javaagent:motadata-javaagent.jar \
     -Dotel.javaagent.debug=true \
     -jar app.jar
```

Look for log messages like:
```
Service name detected using SpringBootServiceNameDetector: order-service
```

### Check Traces in Backend
Your traces should show `service.name` attribute:
```json
{
  "resource": {
    "attributes": {
      "service.name": "payment-service"
    }
  },
  "spans": [...]
}
```

## Troubleshooting

### Service Name Shows as "unknown_service:java"
- Detection failed or disabled
- Solution: Use explicit config:
  ```bash
  -Dotel.service.name=my-service
  ```

### Wrong Service Name Detected
- Another detector ran first
- Solution: Use explicit config (highest priority):
  ```bash
  -Dotel.service.name=correct-name
  ```

### Need Debug Information
- Enable debug logging:
  ```bash
  -Dotel.javaagent.debug=true
  ```
- Look for "Motadata service name detection" log messages

## JAR Naming Conventions

For auto-detection to work, use these patterns:

### ✅ Works
- `payment-service.jar`
- `payment-service-1.2.3.jar`
- `app-2024-01-15.jar`
- Filename doesn't need to match app function exactly

### ⚠️ May Be Unclear
- `app.jar` - becomes "app"
- `service.jar` - becomes "service"
- Use explicit config if unclear

### ❌ Problems
- No JAR in command line
- Main class name is very generic
- Use explicit config:
  ```bash
  -Dotel.service.name=my-service
  ```

## Real-World Examples

### Example 1: Startup Script
```bash
#!/bin/bash
APP_JAR="payment-service-1.0.0.jar"
AGENT_JAR="/opt/motadata-javaagent.jar"

java \
  -javaagent:${AGENT_JAR} \
  -Dotel.exporter.otlp.endpoint=http://otel-collector:4317 \
  -jar ${APP_JAR}
# Auto-detects: service.name=payment-service
```

### Example 2: Docker Compose
```yaml
version: '3'
services:
  app:
    image: my-app:latest
    ports:
      - "8080:8080"
    environment:
      OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4317
      # Service name auto-detected from JAR filename
  
  otel-collector:
    image: otel/opentelemetry-collector:latest
    ports:
      - "4317:4317"
```

### Example 3: Spring Boot on Kubernetes
```yaml
apiVersion: v1
kind: Deployment
metadata:
  name: payment-service
spec:
  template:
    spec:
      containers:
      - name: app
        image: payment-service:1.0
        volumeMounts:
        - name: agent
          mountPath: /opt/agent
        env:
        - name: OTEL_EXPORTER_OTLP_ENDPOINT
          value: "http://otel-collector:4317"
        - name: JAVA_TOOL_OPTIONS
          value: "-javaagent:/opt/agent/motadata-javaagent.jar"
      
      volumes:
      - name: agent
        configMap:
          name: otel-agent
          items:
          - key: agent
            path: motadata-javaagent.jar
```

## Performance Impact

- **Detection**: One-time cost at startup (~few milliseconds)
- **Runtime**: Zero impact on spans, requests, or application throughput
- **Memory**: Minimal (stores one resource object)

## Next Steps

1. **Get the Agent**
   ```bash
   # Build from source
   ./gradlew :javaagent:build
   # Output: javaagent/build/libs/opentelemetry-javaagent.jar
   ```

2. **Run Your Application**
   ```bash
   java -javaagent:opentelemetry-javaagent.jar -jar your-app.jar
   ```

3. **Verify Detection**
   - Check application logs for service name
   - View traces in your backend (Jaeger, Zipkin, etc.)
   - See service.name in resource attributes

4. **Fine-Tune Configuration**
   - Use explicit config if auto-detection isn't suitable
   - Enable debug logging for troubleshooting
   - Reference the main README.md for advanced options

## Support

For issues or questions:
- See: `instrumentation/resources/motadata-service-detector/README.md`
- Check debug logs: `-Dotel.javaagent.debug=true`
- Review detection priority: explicit > Spring > JAR > Main Class > default

---

**Happy tracing!** ✨
