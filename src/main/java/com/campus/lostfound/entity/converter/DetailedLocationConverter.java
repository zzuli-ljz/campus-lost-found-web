package com.campus.lostfound.entity.converter;

import com.campus.lostfound.entity.DetailedLocation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 将 DetailedLocation 与数据库字符串之间转换
 * 兼容枚举名和中文描述
 */
@Converter(autoApply = false)
public class DetailedLocationConverter implements AttributeConverter<DetailedLocation, String> {

    @Override
    public String convertToDatabaseColumn(DetailedLocation attribute) {
        if (attribute == null) return null;
        return attribute.getDescription();
    }

    @Override
    public DetailedLocation convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return DetailedLocation.OTHER_UNKNOWN;
        try {
            return DetailedLocation.valueOf(dbData);
        } catch (IllegalArgumentException ignore) {
        }
        for (DetailedLocation d : DetailedLocation.values()) {
            if (d.getDescription().equals(dbData)) {
                return d;
            }
        }
        return DetailedLocation.OTHER_UNKNOWN;
    }
}

