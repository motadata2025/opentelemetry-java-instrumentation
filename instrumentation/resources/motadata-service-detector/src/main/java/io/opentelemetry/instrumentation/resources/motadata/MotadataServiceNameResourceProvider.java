/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;

import com.google.auto.service.AutoService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.instrumentation.resources.motadata.detectors.ExplicitServiceNameDetector;
import io.opentelemetry.instrumentation.resources.motadata.detectors.JarNameServiceDetector;
import io.opentelemetry.instrumentation.resources.motadata.detectors.MainClassServiceDetector;
import io.opentelemetry.instrumentation.resources.motadata.detectors.SpringBootServiceNameDetector;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A {@link ResourceProvider} that automatically detects the service name using Motadata-specific
 * detection strategies.
 *
 * <p>This provider uses a chain of detectors to determine the service name in the following order:
 *
 * <ol>
 *   <li>Explicit OpenTelemetry configuration ({@code otel.service.name} or {@code
 *       otel.resource.attributes})
 *   <li>Spring Boot application name ({@code spring.application.name})
 *   <li>JAR filename (e.g., {@code payment-service.jar} → {@code payment-service})
 *   <li>Main class name (e.g., {@code com.example.PaymentApplication} → {@code PaymentApplication})
 * </ol>
 *
 * <p>If no detector succeeds, the provider returns an empty Resource, allowing OpenTelemetry's
 * default {@code unknown_service:java} to be used.
 *
 * <p>Detection can be controlled via configuration:
 *
 * <ul>
 *   <li>{@code otel.motadata.service.name.detection.enabled=true} (default: true) - Enable/disable
 *       automatic detection
 *   <li>{@code otel.motadata.service.name.detection.debug=false} (default: false) - Enable debug
 *       logging
 * </ul>
 */
@AutoService(ResourceProvider.class)
public final class MotadataServiceNameResourceProvider implements ConditionalResourceProvider {

  private static final Logger logger =
      Logger.getLogger(MotadataServiceNameResourceProvider.class.getName());

  private static final String DETECTION_ENABLED_KEY =
      "otel.motadata.service.name.detection.enabled";

  @Override
  public Resource createResource(ConfigProperties config) {
    if (logger.isLoggable(FINER)) {
      logger.log(FINER, "Motadata service name detection started");
    }

    ServiceNameDetectionChain chain =
        ServiceNameDetectionChain.builder(config)
            .addDetector(new ExplicitServiceNameDetector(config))
            .addDetector(new SpringBootServiceNameDetector())
            .addDetector(new JarNameServiceDetector())
            .addDetector(new MainClassServiceDetector())
            .build();

    Optional<String> detectedServiceName = chain.detect();

    if (detectedServiceName.isPresent()) {
      String serviceName = detectedServiceName.get();
      return Resource.create(Attributes.of(ServiceAttributes.SERVICE_NAME, serviceName));
    }

    logger.log(FINER, "No service name detected; using OpenTelemetry default");
    return Resource.empty();
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    if (!isDetectionEnabled(config)) {
      logger.log(FINER, "Motadata service name detection is disabled");
      return false;
    }

    String serviceName = config.getString("otel.service.name");
    if (serviceName != null) {
      logger.log(FINE, "Explicit otel.service.name is already set; skipping automatic detection");
      return false;
    }

    Map<String, String> resourceAttributes = config.getMap("otel.resource.attributes");
    if (resourceAttributes.containsKey(ServiceAttributes.SERVICE_NAME.getKey())) {
      logger.log(
          FINE,
          "Service name is already set in otel.resource.attributes; skipping automatic detection");
      return false;
    }

    Object existingServiceNameObj = existing.getAttribute(ServiceAttributes.SERVICE_NAME);
    if (existingServiceNameObj != null) {
      String existingServiceName = (String) existingServiceNameObj;
      if (!existingServiceName.equals("unknown_service:java")) {
        logger.log(
            FINE, "Service name is already set in the resource; skipping automatic detection");
        return false;
      }
    }

    return true;
  }

  @Override
  public int order() {
    return 500;
  }

  private static boolean isDetectionEnabled(ConfigProperties config) {
    String enabled = config.getString(DETECTION_ENABLED_KEY);
    if (enabled == null) {
      return true;
    }
    return Boolean.parseBoolean(enabled);
  }
}
