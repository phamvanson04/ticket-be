package com.cinebee.shared.util;

import java.text.Normalizer;

public class UserUtils {
    /**
     * Remove Vietnamese diacritics from a string for username generation.
     * @param str Input string
     * @return String without Vietnamese tones
     */
    public static String removeVietnameseTones(String str) {
        if (str == null || str.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        return normalized.replaceAll("[^a-zA-Z0-9]", "");
    }
}

