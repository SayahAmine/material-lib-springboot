package com.example.materiallib.model;

public record Curve(
        long id,
        long conditionId,
        String curveType,
        String xLabel,
        String yLabel,
        String xUnit,
        String yUnit,
        Double testTemperatureC,
        Double strainRate,
        Double frequencyHz,
        String environment,
        String standard,
        String notes,
        String createdAt,
        String updatedAt
) {}
