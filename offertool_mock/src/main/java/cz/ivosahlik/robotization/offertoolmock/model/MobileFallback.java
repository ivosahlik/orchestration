package cz.ivosahlik.robotization.offertoolmock.model;

import java.math.BigDecimal;

public record MobileFallback(
        BigDecimal priceFixed,
        BigDecimal priceMobile,
        BigDecimal priceSms
) {}
