package cz.ivosahlik.robotization.offertoolmock.model;

import java.math.BigDecimal;
import java.util.List;

public record VtvItem(
        String partNo,
        String name,
        String itemType,
        Object serviceId,
        String serviceType,
        BigDecimal discount,
        String billingGroupId,
        String installationPlaceId,
        List<Object> addons,
        Object orderId,
        List<Object> otherServices
) {}
