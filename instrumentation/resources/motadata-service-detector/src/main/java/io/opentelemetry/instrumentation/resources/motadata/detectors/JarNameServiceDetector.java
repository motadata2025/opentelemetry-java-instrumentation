/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata.detectors;

import io.opentelemetry.instrumentation.resources.motadata.ServiceNameDetector;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detector that extracts service name from the JAR filename found in the Java command line.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code java -jar payment-service.jar} → {@code payment-service}
 *   <li>{@code java -jar payment-service-1.2.3.jar} → {@code payment-service}
 *   <li>{@code java -cp payment-service.jar com.example.Main} → {@code payment-service}
 * </ul>
 */
public final class JarNameServiceDetector implements ServiceNameDetector {

  private static final Pattern JAR_FILE_PATTERN = Pattern.compile("^\\s*(.+\\.(jar|war))(?:\\s|$)");

  @Override
  public Optional<String> detect() {
    String javaCommand = System.getProperty("sun.java.command");
    if (javaCommand == null || javaCommand.isEmpty()) {
      return Optional.empty();
    }

    Matcher matcher = JAR_FILE_PATTERN.matcher(javaCommand);
    if (matcher.find()) {
      String jarPath = matcher.group(1).trim();
      try {
        Path path = Paths.get(jarPath);
        String fileName = path.getFileName().toString();
        return Optional.of(stripExtension(fileName));
      } catch (RuntimeException e) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  private static String stripExtension(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
  }
}
