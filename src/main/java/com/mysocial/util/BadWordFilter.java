package com.mysocial.util;

import java.util.Arrays;
import java.util.List;

public class BadWordFilter {

    private static final List<String> BAD_WORDS = Arrays.asList(
        "địt", "cặc", "lồn", "buồi", "đụ", "chó", "đéo", "vãi", "vkl", "clgt", "dm", "dcm", "dmm", "fuck", "shit", "bitch", "ngu", "óc chó", "đần", "khốn nạn", "phò", "đĩ", "dâm", "rape", "hiếp", "bố mày", "mẹ mày", "con mẹ mày", "con chó", "cút", "thằng ngu", "thằng điên", "thằng khốn", "thằng chó", "thằng ranh", "thằng lol", "thằng lồn", "thằng cặc", "thằng buồi", "thằng địt", "thằng đéo", "thằng vãi"
    );

    public static boolean containsBadWords(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        for (String word : BAD_WORDS) {
            if (lower.contains(word)) return true;
        }
        return false;
    }
} 