/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class SpringBootServiceNameDetectorTest {

  private final SpringBootServiceNameDetector detector = new SpringBootServiceNameDetector();

  @Test
  void detect_springApplicationNameProperty() {
    String originalValue = System.getProperty("spring.application.name");
    try {
      System.setProperty("spring.application.name", "my-spring-app");

      Optional<String> result = detector.detect();

      assertThat(result).hasValue("my-spring-app");
    } finally {
      if (originalValue != null) {
        System.setProperty("spring.application.name", originalValue);
      } else {
        System.clearProperty("spring.application.name");
      }
    }
  }

  @Test
  void detect_springApplicationNameEnv() {
    String originalValue = System.getProperty("spring.application.name");
    try {
      System.clearProperty("spring.application.name");

      Optional<String> result = detector.detect();

      if (System.getenv("SPRING_APPLICATION_NAME") != null) {
        assertThat(result).hasValue(System.getenv("SPRING_APPLICATION_NAME"));
      } else {
        assertThat(result).isEmpty();
      }
    } finally {
      if (originalValue != null) {
        System.setProperty("spring.application.name", originalValue);
      }
    }
  }

  @Test
  void detect_notFound() {
    String originalValue = System.getProperty("spring.application.name");
    try {
      System.clearProperty("spring.application.name");

      Optional<String> result = detector.detect();

      if (System.getenv("SPRING_APPLICATION_NAME") == null) {
        assertThat(result).isEmpty();
      }
    } finally {
      if (originalValue != null) {
        System.setProperty("spring.application.name", originalValue);
      }
    }
  }

  @Test
  void detect_emptyProperty() {
    String originalValue = System.getProperty("spring.application.name");
    try {
      System.setProperty("spring.application.name", "");

      Optional<String> result = detector.detect();

      assertThat(result).isEmpty();
    } finally {
      if (originalValue != null) {
        System.setProperty("spring.application.name", originalValue);
      } else {
        System.clearProperty("spring.application.name");
      }
    }
  }
}
