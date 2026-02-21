package cz.ivosahlik.robotization.offertoolmock.model;

import java.math.BigDecimal;

public record PriceAmount(
        BigDecimal priceWithVat,
        BigDecimal priceWithoutVat
) {}
