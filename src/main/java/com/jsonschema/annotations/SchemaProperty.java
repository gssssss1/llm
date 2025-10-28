package com.jsonschema.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SchemaProperty {
    String name() default "";
    
    boolean required() default false;
    
    String format() default "";
    
    String pattern() default "";
    
    double minimum() default Double.NEGATIVE_INFINITY;
    
    double maximum() default Double.POSITIVE_INFINITY;
    
    int minLength() default -1;
    
    int maxLength() default -1;
    
    int minItems() default -1;
    
    int maxItems() default -1;
    
    String[] enumValues() default {};
}
