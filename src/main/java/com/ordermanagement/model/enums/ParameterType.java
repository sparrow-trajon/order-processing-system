package com.ordermanagement.model.enums;

/**
 * Enum representing configuration parameter data types.
 */
public enum ParameterType {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    DOUBLE("Double"),
    BOOLEAN("Boolean"),
    JSON("JSON");

    private final String displayName;

    ParameterType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
