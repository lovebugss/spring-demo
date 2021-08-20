package com.example.bean;

import com.example.enums.GenderEnum;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * TODO
 *
 * @author renjp
 * @date 2021/8/20 10:29
 */
@Data
public class User {

    private String name;
    @Max(value = 99)
    @Min(value = 0)
    private int age;
    private GenderEnum gender;
    private Address address;
}
