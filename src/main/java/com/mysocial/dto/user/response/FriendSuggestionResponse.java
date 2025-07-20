package com.mysocial.dto.user.response;

import com.mysocial.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FriendSuggestionResponse {
    private List<UserSuggestion> suggestions;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    @Data
    public static class UserSuggestion {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private boolean gender;
        private String avatarUrl;
        private String phone;
        private LocalDateTime birthDate;
        private String address;
        private String biography;
        private String suggestionReason; // "Bạn chung", "Cùng tỉnh", "Đề xuất"
        private int mutualFriendsCount;

        public static UserSuggestion fromUser(User user, String reason, int mutualFriendsCount) {
            UserSuggestion suggestion = new UserSuggestion();
            suggestion.setId(user.getId());
            suggestion.setEmail(user.getEmail());
            suggestion.setFirstName(user.getFirstName());
            suggestion.setLastName(user.getLastName());
            suggestion.setGender(user.isGender());
            suggestion.setAvatarUrl(user.getAvatarUrl());
            suggestion.setPhone(user.getPhone());
            suggestion.setBirthDate(user.getBirthDate());
            suggestion.setAddress(user.getAddress());
            suggestion.setBiography(user.getBiography());
            suggestion.setSuggestionReason(reason);
            suggestion.setMutualFriendsCount(mutualFriendsCount);
            return suggestion;
        }
    }
}

