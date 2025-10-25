package com.ordermanagement.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Value Object representing a physical address.
 * Immutable by design.
 *
 * Design Pattern: Value Object Pattern
 * Use Case: Encapsulate address information
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Address implements Serializable {

    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    /**
     * Creates an Address with validation
     */
    public static Address of(String street, String city, String state, String zipCode, String country) {
        validate(street, city, state, zipCode, country);
        return new Address(street, city, state, zipCode, country);
    }

    /**
     * Gets formatted address as single string
     */
    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s, %s",
            street != null ? street : "",
            city != null ? city : "",
            state != null ? state : "",
            zipCode != null ? zipCode : "",
            country != null ? country : ""
        ).replaceAll(", ,", ",").trim();
    }

    /**
     * Validates address fields
     */
    private static void validate(String street, String city, String state, String zipCode, String country) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street cannot be null or empty");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }

    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
