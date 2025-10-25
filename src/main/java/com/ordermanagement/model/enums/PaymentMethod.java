package com.ordermanagement.model.enums;

/**
 * Enum representing different payment methods.
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit Card", true),
    DEBIT_CARD("Debit Card", true),
    UPI("UPI", true),
    NET_BANKING("Net Banking", true),
    DIGITAL_WALLET("Digital Wallet", true),
    CASH_ON_DELIVERY("Cash on Delivery", false),
    BANK_TRANSFER("Bank Transfer", false),
    PAYPAL("PayPal", true),
    GOOGLE_PAY("Google Pay", true),
    APPLE_PAY("Apple Pay", true),
    CRYPTOCURRENCY("Cryptocurrency", true),
    CHEQUE("Cheque", false),
    OTHER("Other", false);

    private final String displayName;
    private final boolean isOnline;

    PaymentMethod(String displayName, boolean isOnline) {
        this.displayName = displayName;
        this.isOnline = isOnline;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean requiresVerification() {
        return isOnline;
    }
}
