/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import io.opentelemetry.instrumentation.resources.motadata.ServiceNameDetector;
import java.util.Optional;

/**
 * Detector that checks for Spring Boot application name configuration.
 *
 * <p>Checks in order:
 *
 * <ol>
 *   <li>{@code spring.application.name} system property
 *   <li>{@code SPRING_APPLICATION_NAME} environment variable
 * </ol>
 *
 * <p>Note: For more comprehensive Spring Boot detection (including configuration files,
 * command-line arguments, etc.), consider using the upstream OpenTelemetry Spring Boot
 * ResourceProvider.
 */
public final class SpringBootServiceNameDetector implements ServiceNameDetector {

  @Override
  public Optional<String> detect() {
    String serviceName = System.getProperty("spring.application.name");
    if (serviceName != null && !serviceName.isEmpty()) {
      return Optional.of(serviceName);
    }

    String envServiceName = System.getenv("SPRING_APPLICATION_NAME");
    if (envServiceName != null && !envServiceName.isEmpty()) {
      return Optional.of(envServiceName);
    }

    return Optional.empty();
  }
}
