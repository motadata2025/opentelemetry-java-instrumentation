/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class MotadataServiceNameResourceProviderTest {

  private final MotadataServiceNameResourceProvider provider =
      new MotadataServiceNameResourceProvider();

  @Test
  void shouldApply_withExplicitServiceName() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn("my-service");
    Resource existing = Resource.create(io.opentelemetry.api.common.Attributes.empty());

    assertThat(provider.shouldApply(config, existing)).isFalse();
  }

  @Test
  void shouldApply_withoutExplicitServiceName() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    when(config.getMap("otel.resource.attributes")).thenReturn(java.util.Collections.emptyMap());
    Resource existing =
        Resource.create(
            io.opentelemetry.api.common.Attributes.of(SERVICE_NAME, "unknown_service:java"));

    assertThat(provider.shouldApply(config, existing)).isTrue();
  }

  @Test
  void shouldApply_detectionDisabled() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    when(config.getString("otel.motadata.service.name.detection.enabled")).thenReturn("false");
    when(config.getMap("otel.resource.attributes")).thenReturn(java.util.Collections.emptyMap());
    Resource existing =
        Resource.create(
            io.opentelemetry.api.common.Attributes.of(SERVICE_NAME, "unknown_service:java"));

    assertThat(provider.shouldApply(config, existing)).isFalse();
  }

  @Test
  void createResource_empty() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    when(config.getMap("otel.resource.attributes")).thenReturn(java.util.Collections.emptyMap());

    // Clear sun.java.command to avoid detecting GradleWorkerMain during tests
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.clearProperty("sun.java.command");
      System.clearProperty("spring.application.name");

      Resource resource = provider.createResource(config);

      // When no detectors succeed, resource should be empty
      assertThat(resource.getAttributes()).isEmpty();
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      }
    }
  }

  @Test
  void order() {
    assertThat(provider.order()).isEqualTo(500);
  }
}
