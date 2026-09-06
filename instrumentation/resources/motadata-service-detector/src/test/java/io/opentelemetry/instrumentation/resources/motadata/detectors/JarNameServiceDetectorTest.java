/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class JarNameServiceDetectorTest {

  private final JarNameServiceDetector detector = new JarNameServiceDetector();

  @Test
  void detect_simpleJarName() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "payment-service.jar arg1 arg2");

      Optional<String> result = detector.detect();

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
  void detect_jarWithVersion() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "payment-service-1.2.3.jar");

      Optional<String> result = detector.detect();

      assertThat(result).hasValue("payment-service-1.2.3");
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      } else {
        System.clearProperty("sun.java.command");
      }
    }
  }

  @Test
  void detect_jarWithPath() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "/path/to/app/payment-service.jar arg1");

      Optional<String> result = detector.detect();

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
  void detect_noJar() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "com.example.Main");

      Optional<String> result = detector.detect();

      assertThat(result).isEmpty();
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      } else {
        System.clearProperty("sun.java.command");
      }
    }
  }

  @Test
  void detect_noSunJavaCommand() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.clearProperty("sun.java.command");

      Optional<String> result = detector.detect();

      assertThat(result).isEmpty();
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      }
    }
  }
}
