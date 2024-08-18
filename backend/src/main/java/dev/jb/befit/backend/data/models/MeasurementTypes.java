package dev.jb.befit.backend.data.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeasurementTypes {
    KG("kg", "kilograms"),
    G("g", "grams"),
    KM("km", "kilometers"),
    M("m", "meters"),
    CM("cm", "centimeters"),
    KMH("km/h", "kilometers per hour"),
    AMOUNT("x", "times"),
    HOURS("h", "hours"),
    MINUTES("m", "minutes"),
    SECONDS("s", "seconds"),
    BPM("bpm", "beats per minute");

    private final String shortName;
    private final String longName;
}
