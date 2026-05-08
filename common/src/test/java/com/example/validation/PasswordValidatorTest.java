package com.example.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    @Test
    void isValid_nullPassword_returnsFalse() {
        assertThat(passwordValidator.isValid(null, context)).isFalse();
    }

    @Test
    void isValid_validPassword_returnsTrue() {
        assertThat(passwordValidator.isValid("Secure@1", context)).isTrue();
    }

    @Test
    void isValid_allRequirementsMet_returnsTrue() {
        assertThat(passwordValidator.isValid("StrongP@ss1", context)).isTrue();
    }

    @Test
    void isValid_missingUppercase_returnsFalse() {
        assertThat(passwordValidator.isValid("secure@1", context)).isFalse();
    }

    @Test
    void isValid_missingLowercase_returnsFalse() {
        assertThat(passwordValidator.isValid("SECURE@1", context)).isFalse();
    }

    @Test
    void isValid_missingDigit_returnsFalse() {
        assertThat(passwordValidator.isValid("Secure@!", context)).isFalse();
    }

    @Test
    void isValid_missingSpecialCharacter_returnsFalse() {
        assertThat(passwordValidator.isValid("Secure123", context)).isFalse();
    }

    @Test
    void isValid_tooShort_returnsFalse() {
        assertThat(passwordValidator.isValid("Se@1", context)).isFalse();
    }

    @Test
    void isValid_exactlyMinLength_returnsTrue() {
        assertThat(passwordValidator.isValid("Secur@1x", context)).isTrue();
    }

    @Test
    void isValid_emptyString_returnsFalse() {
        assertThat(passwordValidator.isValid("", context)).isFalse();
    }

    @Test
    void isValid_onlySpaces_returnsFalse() {
        assertThat(passwordValidator.isValid("        ", context)).isFalse();
    }

    @Test
    void isValid_specialCharAt_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid@1pw", context)).isTrue();
    }

    @Test
    void isValid_specialCharDollar_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid$1pw", context)).isTrue();
    }

    @Test
    void isValid_specialCharExclamation_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid!1pw", context)).isTrue();
    }

    @Test
    void isValid_specialCharPercent_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid%1pw", context)).isTrue();
    }

    @Test
    void isValid_specialCharAsterisk_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid*1pw", context)).isTrue();
    }

    @Test
    void isValid_specialCharQuestion_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid?1pw", context)).isTrue();
    }

    @Test
    void isValid_specialCharAmpersand_returnsTrue() {
        assertThat(passwordValidator.isValid("Valid&1pw", context)).isTrue();
    }

    @Test
    void isValid_unsupportedSpecialChar_returnsFalse() {
        assertThat(passwordValidator.isValid("Valid#1pw", context)).isFalse();
    }

    @Test
    void isValid_sevenCharsAllRequirements_returnsFalse() {
        assertThat(passwordValidator.isValid("Sec@re1", context)).isFalse();
    }

    @Test
    void isValid_longValidPassword_returnsTrue() {
        assertThat(passwordValidator.isValid("ThisIsAVeryStr0ng&SecurePassword!", context)).isTrue();
    }
}