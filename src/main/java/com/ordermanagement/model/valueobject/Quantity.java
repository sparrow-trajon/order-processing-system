package com.ordermanagement.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Value Object representing a quantity.
 * Immutable and validates business rules.
 *
 * Design Pattern: Value Object Pattern
 * Use Case: Encapsulate quantity logic with validation
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class Quantity implements Serializable, Comparable<Quantity> {

    private Integer value;

    private Quantity(Integer value) {
        validate(value);
        this.value = value;
    }

    /**
     * Creates a Quantity with validation
     */
    public static Quantity of(Integer value) {
        return new Quantity(value);
    }

    /**
     * Creates a zero quantity
     */
    public static Quantity zero() {
        return new Quantity(0);
    }

    /**
     * Creates a unit quantity (1)
     */
    public static Quantity one() {
        return new Quantity(1);
    }

    /**
     * Adds two quantities
     */
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    /**
     * Subtracts two quantities
     */
    public Quantity subtract(Quantity other) {
        if (this.value < other.value) {
            throw new IllegalArgumentException("Cannot subtract: result would be negative");
        }
        return new Quantity(this.value - other.value);
    }

    /**
     * Multiplies quantity by a factor
     */
    public Quantity multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Quantity(this.value * factor);
    }

    /**
     * Checks if quantity is zero
     */
    public boolean isZero() {
        return this.value == 0;
    }

    /**
     * Checks if quantity is positive
     */
    public boolean isPositive() {
        return this.value > 0;
    }

    /**
     * Checks if this quantity is greater than another
     */
    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }

    /**
     * Checks if this quantity is less than another
     */
    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }

    /**
     * Validates quantity business rules
     */
    private void validate(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        // Max quantity check will be done at business layer with configuration
    }

    @Override
    public int compareTo(Quantity other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
