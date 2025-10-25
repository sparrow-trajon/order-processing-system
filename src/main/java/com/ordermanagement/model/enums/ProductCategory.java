package com.ordermanagement.model.enums;

/**
 * Enum representing product categories.
 * Can be extended or replaced with database table for more flexibility.
 */
public enum ProductCategory {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing & Apparel"),
    FOOD_BEVERAGE("Food & Beverage"),
    HOME_GARDEN("Home & Garden"),
    BOOKS("Books & Media"),
    TOYS_GAMES("Toys & Games"),
    SPORTS_OUTDOORS("Sports & Outdoors"),
    HEALTH_BEAUTY("Health & Beauty"),
    AUTOMOTIVE("Automotive"),
    OFFICE_SUPPLIES("Office Supplies"),
    OTHER("Other");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
