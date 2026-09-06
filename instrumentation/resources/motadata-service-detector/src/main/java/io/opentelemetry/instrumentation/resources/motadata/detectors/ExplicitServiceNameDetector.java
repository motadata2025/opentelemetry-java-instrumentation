/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import io.opentelemetry.instrumentation.resources.motadata.ServiceNameDetector;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Optional;

/**
 * Detector that checks for explicitly configured service name via OpenTelemetry standard
 * configuration mechanisms.
 *
 * <p>Checks in order:
 *
 * <ol>
 *   <li>{@code otel.service.name} system property / environment variable
 *   <li>{@code service.name} in {@code otel.resource.attributes}
 * </ol>
 */
public final class ExplicitServiceNameDetector implements ServiceNameDetector {

  private final ConfigProperties config;

  public ExplicitServiceNameDetector(ConfigProperties config) {
    this.config = config;
  }

  @Override
  public Optional<String> detect() {
    String serviceName = config.getString("otel.service.name");
    if (serviceName != null) {
      return Optional.of(serviceName);
    }

    java.util.Map<String, String> resourceAttributes = config.getMap("otel.resource.attributes");
    String explicitServiceName = resourceAttributes.get("service.name");
    if (explicitServiceName != null) {
      return Optional.of(explicitServiceName);
    }

    return Optional.empty();
  }
}
