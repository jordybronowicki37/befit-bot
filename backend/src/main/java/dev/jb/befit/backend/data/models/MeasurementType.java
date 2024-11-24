package dev.jb.befit.backend.data.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static dev.jb.befit.backend.data.models.MeasurementCategory.*;

@Getter
@RequiredArgsConstructor
public enum MeasurementType {
    KG("kg", "kilograms", WEIGHT),
    G("g", "grams", WEIGHT),
    KM("km", "kilometers", DISTANCE),
    M("m", "meters", DISTANCE),
    CM("cm", "centimeters", DISTANCE),
    KMH("km/h", "kilometers per hour", SPEED),
    AMOUNT("x", "times", MeasurementCategory.AMOUNT),
    HOURS("hrs", "hours", TIME),
    MINUTES("min", "minutes", TIME),
    SECONDS("sec", "seconds", TIME),
    PERCENTAGE("%", "percentage", MeasurementCategory.AMOUNT),
    CALORIES("cal", "calories", MeasurementCategory.AMOUNT),
    BPM("bpm", "beats per minute", MeasurementCategory.AMOUNT);

    private final String shortName;
    private final String longName;
    private final MeasurementCategory category;
}
