/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class MainClassServiceDetectorTest {

  private final MainClassServiceDetector detector = new MainClassServiceDetector();

  @Test
  void detect_simpleMainClass() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "com.example.PaymentApplication arg1 arg2");

      Optional<String> result = detector.detect();

      assertThat(result).hasValue("PaymentApplication");
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      } else {
        System.clearProperty("sun.java.command");
      }
    }
  }

  @Test
  void detect_deeplyNestedMainClass() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "com.company.payment.service.PaymentService");

      Optional<String> result = detector.detect();

      assertThat(result).hasValue("PaymentService");
    } finally {
      if (originalCommand != null) {
        System.setProperty("sun.java.command", originalCommand);
      } else {
        System.clearProperty("sun.java.command");
      }
    }
  }

  @Test
  void detect_mainClassNotMatched() {
    String originalCommand = System.getProperty("sun.java.command");
    try {
      System.setProperty("sun.java.command", "payment-service.jar");

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
