/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.instrumentation.resources.motadata.detectors.ExplicitServiceNameDetector;
import io.opentelemetry.instrumentation.resources.motadata.detectors.JarNameServiceDetector;
import io.opentelemetry.instrumentation.resources.motadata.detectors.MainClassServiceDetector;
import io.opentelemetry.instrumentation.resources.motadata.detectors.SpringBootServiceNameDetector;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ServiceNameDetectionChainTest {

  @Test
  void detectsUsingFirstMatchingDetector() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    when(config.getMap("otel.resource.attributes")).thenReturn(Collections.emptyMap());

    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "payment-service.jar arg1");

      ServiceNameDetectionChain chain =
          ServiceNameDetectionChain.builder(config)
              .addDetector(new ExplicitServiceNameDetector(config))
              .addDetector(new SpringBootServiceNameDetector())
              .addDetector(new JarNameServiceDetector())
              .addDetector(new MainClassServiceDetector())
              .build();

      Optional<String> result = chain.detect();

      assertThat(result).hasValue("payment-service");
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      } else {
        System.clearProperty("sun.java.command");
      }
    }
  }

  @Test
  void respectsDetectorPriority() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn("explicit-service");
    when(config.getMap("otel.resource.attributes")).thenReturn(Collections.emptyMap());

    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "jar-service.jar arg1");

      ServiceNameDetectionChain chain =
          ServiceNameDetectionChain.builder(config)
              .addDetector(new ExplicitServiceNameDetector(config))
              .addDetector(new JarNameServiceDetector())
              .build();

      Optional<String> result = chain.detect();

      // Explicit config should win over JAR detection
      assertThat(result).hasValue("explicit-service");
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      } else {
        System.clearProperty("sun.java.command");
      }
    }
  }

  @Test
  void returnsEmptyWhenNoDetectorSucceeds() {
    ConfigProperties config = mock(ConfigProperties.class);
    when(config.getString("otel.service.name")).thenReturn(null);
    when(config.getMap("otel.resource.attributes")).thenReturn(Collections.emptyMap());

    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.clearProperty("sun.java.command");

      ServiceNameDetectionChain chain =
          ServiceNameDetectionChain.builder(config)
              .addDetector(new ExplicitServiceNameDetector(config))
              .addDetector(new SpringBootServiceNameDetector())
              .addDetector(new JarNameServiceDetector())
              .addDetector(new MainClassServiceDetector())
              .build();

      Optional<String> result = chain.detect();

      assertThat(result).isEmpty();
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      }
    }
  }
}
