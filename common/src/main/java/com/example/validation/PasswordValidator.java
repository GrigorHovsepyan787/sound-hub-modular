package com.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {

        if (password == null) return false;

        boolean hasUpper =
                password.matches(".*[A-Z].*");

        boolean hasLower =
                password.matches(".*[a-z].*");

        boolean hasDigit =
                password.matches(".*\\d.*");

        boolean hasSpecial =
                password.matches(".*[@$!%*?&].*");

        boolean hasLength =
                password.length() >= 8;

        return hasUpper
                && hasLower
                && hasDigit
                && hasSpecial
                && hasLength;
    }
}