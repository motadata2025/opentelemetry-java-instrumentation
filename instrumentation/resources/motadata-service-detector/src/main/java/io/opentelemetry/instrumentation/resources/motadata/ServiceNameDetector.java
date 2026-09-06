/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata;

import java.util.Optional;

/**
 * A detector that attempts to determine the service name using a specific detection strategy.
 *
 * <p>Implementations should be stateless and thread-safe. Detection should be fast and avoid
 * expensive operations like filesystem scans or network calls.
 */
public interface ServiceNameDetector {

  /**
   * Attempts to detect the service name.
   *
   * @return an Optional containing the detected service name, or empty if detection was not
   *     successful
   */
  Optional<String> detect();
}
