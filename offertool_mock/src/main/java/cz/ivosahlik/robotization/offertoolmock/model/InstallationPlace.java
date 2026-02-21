package cz.ivosahlik.robotization.offertoolmock.model;

public record InstallationPlace(
        String id,
        String name,
        String contactPerson,
        String contactPhone,
        Address address
) {}
