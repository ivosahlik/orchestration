package cz.ivosahlik.robotization.offertoolmock.controller;

import cz.ivosahlik.robotization.offertoolmock.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock REST controller returning hardcoded Offertool offer data.
 *
 * <p>Endpoint: {@code GET /api/offers/variants}
 */
@Slf4j
@RestController
@RequestMapping("/api/offers")
public class OfferController {

    @GetMapping("/variants")
    public OfferResponse getOffer() {
        PriceAmount commitment = new PriceAmount(
                new BigDecimal("121.00000000"),
                new BigDecimal("100.000000")
        );

        Address billingAddress = new Address(
                "Olomouc 1",
                "street",
                "1079/219",
                "77900"
        );

        Address installAddress = new Address(
                "Olomouc 1",
                "street",
                "/1079/21",
                "77900"
        );

        OfferResponse offerResponse = new OfferResponse(
                1,
                ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE),
                "admintest",
                "test@test.com",
                "1040093072",
                "56150563",
                "2.0",
                null,
                null,
                "1-1617870225_0",
                Map.of(),
                "V4",
                "Akvizice",
                24,
                24,
                null,
                "2024-01-18T00:00:00.000+00:00",
                "2026-01-17T00:00:00.000+00:00",
                18,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                commitment,
                BigDecimal.ZERO,
                commitment,
                commitment,
                commitment,
                BigDecimal.ZERO,
                new BigDecimal("200.000000"),
                List.of(new BillingGroup(
                        "97fbc71d-add7-45c6-8f3d-028329f7d213",
                        0,
                        "Výchozí",
                        billingAddress,
                        List.of(),
                        List.of()
                )),
                List.of(new InstallationPlace(
                        "305da18a-25f9-4032-bcbb-6140b4e6a0ed",
                        "BMSL Test",
                        "X",
                        "420735425136",
                        installAddress
                )),
                new MobileFallback(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new FixFallback(BigDecimal.ZERO, BigDecimal.ZERO),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(new VtvItem(
                        "TVTARIFF001",
                        "ivosahlik TV - Klasik",
                        "Tarif",
                        null,
                        "New",
                        BigDecimal.ZERO,
                        "97fbc71d-add7-45c6-8f3d-028329f7d213",
                        "",
                        List.of(),
                        null,
                        List.of()
                )),
                List.of(),
                List.of(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "KeZpracovaniRoBotem",
                null,
                null,
                null,
                true,
                null,
                new TsSection(List.of()),
                List.of(),
                false,
                BigDecimal.ZERO,
                0
        );
        log.info("Producer: {}", offerResponse);
        return offerResponse;
    }
}
