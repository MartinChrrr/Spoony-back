package com.spoony.backend.domain.tasklog.model;

/**
 * Immutable task data copied into a dated log.
 */
public record TaskSnapshot(String name, int spoonCost) {
}
