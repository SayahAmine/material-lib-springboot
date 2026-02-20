package com.example.materiallib.model;

public record CurvePoint(
        long curveId,
        int idx,
        double x,
        double y,
        Double z
) {}
