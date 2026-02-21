package cz.ivosahlik.robotization.scenarioengine.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for sending a signal to a running scenario.
 */
public record SignalRequest(@NotBlank String signal) {}
