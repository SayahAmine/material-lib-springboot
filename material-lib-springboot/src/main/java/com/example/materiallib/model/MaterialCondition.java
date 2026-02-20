package com.example.materiallib.model;

public record MaterialCondition(
        long id,
        long materialId,
        String conditionName,
        String processRoute,
        String productForm,
        String heatTreatment,
        String notes,
        boolean isDefault,
        String createdAt,
        String updatedAt
) {}
