/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExplicitServiceNameDetectorTest {

  @Test
  void detect_fromOtelServiceName() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn("my-service");
    when(config.getMap("otel.resource.attributes")).thenReturn(Collections.emptyMap());

    ExplicitServiceNameDetector detector = new ExplicitServiceNameDetector(config);
    Optional<String> result = detector.detect();

    assertThat(result).hasValue("my-service");
  }

  @Test
  void detect_fromResourceAttributes() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    Map<String, String> attributes = new HashMap<>();
    attributes.put("service.name", "my-service");
    when(config.getMap("otel.resource.attributes")).thenReturn(attributes);

    ExplicitServiceNameDetector detector = new ExplicitServiceNameDetector(config);
    Optional<String> result = detector.detect();

    assertThat(result).hasValue("my-service");
  }

  @Test
  void detect_otelServiceNameTakesPrecedence() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn("from-property");
    Map<String, String> attributes = new HashMap<>();
    attributes.put("service.name", "from-attributes");
    when(config.getMap("otel.resource.attributes")).thenReturn(attributes);

    ExplicitServiceNameDetector detector = new ExplicitServiceNameDetector(config);
    Optional<String> result = detector.detect();

    assertThat(result).hasValue("from-property");
  }

  @Test
  void detect_notFound() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    when(config.getMap("otel.resource.attributes")).thenReturn(Collections.emptyMap());

    ExplicitServiceNameDetector detector = new ExplicitServiceNameDetector(config);
    Optional<String> result = detector.detect();

    assertThat(result).isEmpty();
  }
}
