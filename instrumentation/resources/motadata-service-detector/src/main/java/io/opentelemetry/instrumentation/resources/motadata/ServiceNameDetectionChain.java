/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.resources.motadata;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Orchestrates a chain of {@link ServiceNameDetector}s in priority order.
 *
 * <p>The first detector that successfully returns a service name wins. If no detector succeeds, the
 * chain returns empty.
 */
final class ServiceNameDetectionChain {

  private static final Logger logger = Logger.getLogger(ServiceNameDetectionChain.class.getName());

  private final List<ServiceNameDetector> detectors;

  ServiceNameDetectionChain(List<ServiceNameDetector> detectors, ConfigProperties config) {
    this.detectors = Objects.requireNonNull(detectors, "detectors cannot be null");
    Objects.requireNonNull(config, "config cannot be null");
  }

  /**
   * Runs the detection chain and returns the first successful detection result.
   *
   * @return an Optional containing the detected service name, or empty if no detector succeeded
   */
  Optional<String> detect() {
    logger.log(FINER, "Starting Motadata service name detection chain");

    for (ServiceNameDetector detector : detectors) {
      String detectorName = detector.getClass().getSimpleName();
      try {
        Optional<String> detected = detector.detect();
        if (detected.isPresent()) {
          String serviceName = detected.get();
          logger.log(
              FINE,
              "Service name detected using {0}: {1}",
              new Object[] {detectorName, serviceName});
          return detected;
        }
        logger.log(FINER, "No service name detected by {0}", detectorName);
      } catch (RuntimeException e) {
        logger.log(
            FINER,
            "Error during service name detection by " + detectorName + ": " + e.getMessage());
      }
    }

    logger.log(FINER, "No service name detected by any detector in the chain");
    return Optional.empty();
  }

  /**
   * Creates a builder for constructing a detection chain.
   *
   * @param config the OpenTelemetry configuration
   * @return a new builder
   */
  static Builder builder(ConfigProperties config) {
    return new Builder(config);
  }

  /** Builder for constructing a ServiceNameDetectionChain. */
  static final class Builder {

    private final ConfigProperties config;
    private final List<ServiceNameDetector> detectors = new ArrayList<>();

    Builder(ConfigProperties config) {
      this.config = config;
    }

    /**
     * Adds a detector to the chain.
     *
     * @param detector the detector to add
     * @return this builder
     */
    @CanIgnoreReturnValue
    Builder addDetector(ServiceNameDetector detector) {
      this.detectors.add(detector);
      return this;
    }

    /**
     * Builds the detection chain.
     *
     * @return a new ServiceNameDetectionChain
     */
    ServiceNameDetectionChain build() {
      return new ServiceNameDetectionChain(new ArrayList<>(detectors), config);
    }
  }
}
