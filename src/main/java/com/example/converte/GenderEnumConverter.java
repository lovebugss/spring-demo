package com.example.converte;

import com.example.enums.GenderEnum;
import org.springframework.core.convert.converter.Converter;

import java.util.Locale;

/**
 *  GenderEnum转换器
 *
 * @author renjp
 * @date 2021/8/20 10:40
 */
public class GenderEnumConverter implements Converter<String, GenderEnum> {


    @Override
    public GenderEnum convert(String s) {
        return GenderEnum.valueOf(s.toUpperCase(Locale.ROOT));
    }
}
