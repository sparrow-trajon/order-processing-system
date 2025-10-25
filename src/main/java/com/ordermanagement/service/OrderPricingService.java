package com.ordermanagement.service;

import com.ordermanagement.constants.ApplicationConstants;
import com.ordermanagement.model.entity.Customer;
import com.ordermanagement.model.entity.Item;
import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.enums.CustomerType;
import com.ordermanagement.model.valueobject.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating order pricing including discounts, taxes, and shipping.
 * Implements business rules for pricing calculations.
 *
 * Design Pattern: Service Pattern, Strategy Pattern
 * Use Case: Complex pricing calculations with multiple business rules
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPricingService {

    private final ConfigurationService configService;

    /**
     * Calculate complete pricing for an order
     */
    public void calculateOrderPricing(Order order) {
        log.debug("Calculating pricing for order: {}", order.getOrderNumber());

        // Calculate item-level pricing
        for (Item item : order.getItems()) {
            calculateItemPricing(item, order.getCustomer());
        }

        // Calculate order totals
        Money subtotal = calculateSubtotal(order);
        Money discount = calculateOrderDiscount(order, subtotal);
        Money taxAmount = calculateTax(order, subtotal.subtract(discount));
        Money shippingCost = calculateShipping(order, subtotal);

        // Set calculated values
        order.setTotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setSubtotal(subtotal.subtract(discount));
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(shippingCost);

        // Final amount = subtotal - discount + tax + shipping
        Money finalAmount = subtotal.subtract(discount).add(taxAmount).add(shippingCost);
        order.setFinalAmount(finalAmount);

        log.info("Order pricing calculated: orderId={}, subtotal={}, discount={}, tax={}, shipping={}, final={}",
            order.getId(), subtotal, discount, taxAmount, shippingCost, finalAmount);
    }

    /**
     * Calculate pricing for a single item
     */
    public void calculateItemPricing(Item item, Customer customer) {
        // Item-level discount based on customer type
        Money itemDiscount = calculateItemDiscount(item, customer);
        item.applyDiscount(itemDiscount);

        // Calculate tax for item (after discount)
        Money itemTax = calculateItemTax(item);
        item.setCustomTax(itemTax);

        item.calculatePricing();
    }

    /**
     * Calculate subtotal (sum of all item totals)
     */
    private Money calculateSubtotal(Order order) {
        return order.getItems().stream()
            .map(Item::getTotalPrice)
            .reduce(Money.zero(), Money::add);
    }

    /**
     * Calculate order-level discount
     */
    private Money calculateOrderDiscount(Order order, Money subtotal) {
        Customer customer = order.getCustomer();
        Money discount = Money.zero();

        // Customer type discount
        if (customer != null) {
            double discountPercent = getCustomerDiscountPercent(customer.getType());
            if (discountPercent > 0) {
                discount = subtotal.multiply(BigDecimal.valueOf(discountPercent / 100.0));
                log.debug("Customer type discount applied: {}% for {}", discountPercent, customer.getType());
            }
        }

        // Bulk order discount
        int totalQuantity = order.getTotalQuantity();
        int bulkThreshold = configService.getInteger("order.bulk.discount.threshold",
            (int) ApplicationConstants.BusinessRules.BULK_ORDER_DISCOUNT_THRESHOLD);

        if (totalQuantity >= bulkThreshold) {
            double bulkDiscountPercent = configService.getDouble("order.bulk.discount.percent",
                ApplicationConstants.BusinessRules.BULK_ORDER_DISCOUNT_PERCENT);
            Money bulkDiscount = subtotal.multiply(BigDecimal.valueOf(bulkDiscountPercent / 100.0));
            discount = discount.add(bulkDiscount);
            log.debug("Bulk order discount applied: {}%", bulkDiscountPercent);
        }

        return discount;
    }

    /**
     * Calculate item-level discount based on customer type
     */
    private Money calculateItemDiscount(Item item, Customer customer) {
        if (customer == null) {
            return Money.zero();
        }

        double discountPercent = getCustomerDiscountPercent(customer.getType());
        return item.getTotalPrice().multiply(BigDecimal.valueOf(discountPercent / 100.0));
    }

    /**
     * Get discount percent for customer type
     */
    private double getCustomerDiscountPercent(CustomerType customerType) {
        return switch (customerType) {
            case VIP -> configService.getDouble("discount.vip.percent",
                ApplicationConstants.BusinessRules.VIP_CUSTOMER_DISCOUNT_PERCENT);
            case WHOLESALE -> configService.getDouble("discount.wholesale.percent",
                ApplicationConstants.BusinessRules.WHOLESALE_CUSTOMER_DISCOUNT_PERCENT);
            case CORPORATE -> customerType.getDefaultDiscount() * 100;
            default -> 0.0;
        };
    }

    /**
     * Calculate tax for order
     */
    private Money calculateTax(Order order, Money taxableAmount) {
        // Get tax rate from configuration (can be region-specific)
        double taxRate = configService.getDouble("tax.rate.percent",
            ApplicationConstants.BusinessRules.TAX_RATE_PERCENT);

        Money tax = taxableAmount.multiply(BigDecimal.valueOf(taxRate / 100.0));

        log.debug("Tax calculated: rate={}%, taxableAmount={}, tax={}",
            taxRate, taxableAmount, tax);

        return tax;
    }

    /**
     * Calculate tax for a single item
     */
    private Money calculateItemTax(Item item) {
        double taxRate = configService.getDouble("tax.rate.percent",
            ApplicationConstants.BusinessRules.TAX_RATE_PERCENT);

        Money subtotal = item.getSubtotal(); // After discount
        return subtotal.multiply(BigDecimal.valueOf(taxRate / 100.0));
    }

    /**
     * Calculate shipping cost
     */
    private Money calculateShipping(Order order, Money subtotal) {
        // Free shipping threshold
        BigDecimal freeShippingThreshold = configService.getDouble("shipping.free.threshold",
            ApplicationConstants.BusinessRules.FREE_SHIPPING_THRESHOLD.doubleValue())
            != null ? BigDecimal.valueOf(configService.getDouble("shipping.free.threshold",
            ApplicationConstants.BusinessRules.FREE_SHIPPING_THRESHOLD.doubleValue()))
            : ApplicationConstants.BusinessRules.FREE_SHIPPING_THRESHOLD;

        if (subtotal.getAmount().compareTo(freeShippingThreshold) >= 0) {
            log.debug("Free shipping applied (order above threshold)");
            return Money.zero();
        }

        // Calculate based on weight or flat rate
        if (order.getIsPriority() != null && order.getIsPriority()) {
            double expressShipping = configService.getDouble("shipping.express.cost", 25.0);
            log.debug("Express shipping applied: {}", expressShipping);
            return Money.of(BigDecimal.valueOf(expressShipping));
        }

        double standardShipping = configService.getDouble("shipping.standard.cost", 10.0);
        log.debug("Standard shipping applied: {}", standardShipping);
        return Money.of(BigDecimal.valueOf(standardShipping));
    }

    /**
     * Apply promotional code (future enhancement)
     */
    public Money applyPromoCode(Order order, String promoCode) {
        // TODO: Implement promo code logic
        log.warn("Promo code feature not yet implemented: {}", promoCode);
        return Money.zero();
    }

    /**
     * Calculate loyalty points earned (future enhancement)
     */
    public int calculateLoyaltyPoints(Order order) {
        int pointsPerDollar = configService.getInteger("loyalty.points.per.dollar",
            ApplicationConstants.BusinessRules.LOYALTY_POINTS_PER_DOLLAR);

        int totalDollars = order.getFinalAmount().getAmount().intValue();
        int points = totalDollars * pointsPerDollar;

        log.debug("Loyalty points calculated: {} points for ${}", points, totalDollars);
        return points;
    }
}
