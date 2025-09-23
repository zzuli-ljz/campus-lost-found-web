package com.campus.lostfound.entity.converter;

import com.campus.lostfound.entity.Location;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 将 Location 与数据库字符串之间转换
 * 兼容：
 * 1) 枚举名（如 LIBRARY）
 * 2) 中文描述（如 图书馆）
 */
@Converter(autoApply = false)
public class LocationConverter implements AttributeConverter<Location, String> {

    @Override
    public String convertToDatabaseColumn(Location attribute) {
        if (attribute == null) return null;
        // 存储中文描述，便于直观查看
        return attribute.getDescription();
    }

    @Override
    public Location convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return Location.OTHER;
        // 先按枚举名匹配
        try {
            return Location.valueOf(dbData);
        } catch (IllegalArgumentException ignore) {
        }
        // 按中文描述匹配
        for (Location l : Location.values()) {
            if (l.getDescription().equals(dbData)) {
                return l;
            }
        }
        return Location.OTHER;
    }
}

