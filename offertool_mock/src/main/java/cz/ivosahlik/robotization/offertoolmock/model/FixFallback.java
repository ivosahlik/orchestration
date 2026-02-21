package cz.ivosahlik.robotization.offertoolmock.model;

import java.math.BigDecimal;

public record FixFallback(
        BigDecimal priceFixed,
        BigDecimal priceMobile
) {}
