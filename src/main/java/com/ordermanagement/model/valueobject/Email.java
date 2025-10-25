package com.ordermanagement.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Immutable with built-in validation.
 *
 * Design Pattern: Value Object Pattern
 * Use Case: Encapsulate email validation logic
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class Email implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private String address;

    private Email(String address) {
        validate(address);
        this.address = address.toLowerCase().trim();
    }

    /**
     * Creates an Email with validation
     */
    public static Email of(String address) {
        return new Email(address);
    }

    /**
     * Gets the domain part of the email
     */
    public String getDomain() {
        if (address == null || !address.contains("@")) {
            return "";
        }
        return address.substring(address.indexOf("@") + 1);
    }

    /**
     * Gets the local part of the email (before @)
     */
    public String getLocalPart() {
        if (address == null || !address.contains("@")) {
            return address;
        }
        return address.substring(0, address.indexOf("@"));
    }

    /**
     * Validates email format
     */
    private void validate(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }

        String trimmed = address.trim();

        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Email address is too long (max 255 characters)");
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email address format: " + address);
        }
    }

    /**
     * Checks if email matches a pattern (for domain validation, etc.)
     */
    public boolean matches(String pattern) {
        return this.address.matches(pattern);
    }

    @Override
    public String toString() {
        return address;
    }
}
