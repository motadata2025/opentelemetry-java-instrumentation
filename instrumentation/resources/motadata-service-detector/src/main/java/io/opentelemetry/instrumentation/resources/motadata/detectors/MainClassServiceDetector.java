/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import io.opentelemetry.instrumentation.resources.motadata.ServiceNameDetector;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector that extracts service name from the main class name when not running a JAR.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code java com.example.PaymentApplication} → {@code PaymentApplication}
 *   <li>{@code java com.company.payment.PaymentService} → {@code PaymentService}
 * </ul>
 *
 * <p>Extracts the last component of the fully-qualified class name as the service name.
 */
public final class MainClassServiceDetector implements ServiceNameDetector {

  private static final Pattern MAIN_CLASS_PATTERN =
      Pattern.compile("^\\s*([a-zA-Z_$][a-zA-Z0-9_$.]*\\.([a-zA-Z_$][a-zA-Z0-9_$]*))(?:\\s|$)");

  @Override
  public Optional<String> detect() {
    String javaCommand = System.getProperty("sun.java.command");
    if (javaCommand == null || javaCommand.isEmpty()) {
      return Optional.empty();
    }

    Matcher matcher = MAIN_CLASS_PATTERN.matcher(javaCommand);
    if (matcher.find()) {
      String fullClassName = matcher.group(1);
      int lastDot = fullClassName.lastIndexOf('.');
      if (lastDot > 0 && lastDot < fullClassName.length() - 1) {
        String simpleClassName = fullClassName.substring(lastDot + 1);
        return Optional.of(simpleClassName);
      }
    }

    return Optional.empty();
  }
}
