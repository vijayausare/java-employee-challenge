package com.reliaquest.api.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringUtilsTest {
    @Test
    void containsString_shouldReturnTrueWhenSubstringExistsIgnoringCase() {
        String sourceString = "Spring Boot makes Java development easier";
        assertTrue(StringUtils.containsString(sourceString, "spring"));
        assertTrue(StringUtils.containsString(sourceString, "JAVA"));
    }

    @Test
    void containsString_shouldReturnFalseWhenSubstringDoesNotExistIgnoringCase() {
        String sourceString = "Spring Boot makes Java development easier";
        assertFalse(StringUtils.containsString(sourceString, "Python"));
        assertFalse(StringUtils.containsString(sourceString, "Kotlin"));
    }
}
