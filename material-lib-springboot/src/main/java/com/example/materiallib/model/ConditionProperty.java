package com.example.materiallib.model;

public record ConditionProperty(
        long id,
        long conditionId,
        String propKey,
        String propName,
        Double valueNum,
        String valueText,
        String unit,
        String basis,
        String method,
        String standard,
        String notes,
        Double temperatureC,
        Double strainRate,
        Double frequencyHz,
        String environment,
        Double uncertainty,
        String confidence
) {}
