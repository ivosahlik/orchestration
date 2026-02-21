package cz.ivosahlik.robotization.offertoolmock.model;

import java.util.List;

public record BillingGroup(
        String id,
        int billingAccountId,
        String recipient,
        Address address,
        List<Object> volumeDiscounts,
        List<Object> invoiceDiscounts
) {}
