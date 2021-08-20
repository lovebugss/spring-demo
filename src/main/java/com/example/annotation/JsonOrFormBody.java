package com.example.annotation;

import java.lang.annotation.*;

/**
 * @author renjp
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PARAMETER})
public @interface JsonOrFormBody {
}
