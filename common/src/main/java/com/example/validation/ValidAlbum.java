package com.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AlbumValidator.class)
@Documented
public @interface ValidAlbum {
    String message() default "Either artistId or bandId must be set, but not both";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

