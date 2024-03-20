package dev.jb.befit.backend.data.converters;

import discord4j.common.util.Snowflake;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SnowflakeConverter implements AttributeConverter<Snowflake, Long> {
    @Override
    public Long convertToDatabaseColumn(Snowflake snowflake) {
        return snowflake.asLong();
    }

    @Override
    public Snowflake convertToEntityAttribute(Long aLong) {
        return Snowflake.of(aLong);
    }
}
