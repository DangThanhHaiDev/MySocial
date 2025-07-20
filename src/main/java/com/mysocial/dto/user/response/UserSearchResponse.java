package com.mysocial.dto.user.response;

import com.mysocial.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {
    private List<UserSearchResult> users;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSearchResult {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String address;
        private String biography;
        private SearchReason reason;
        private int mutualFriendsCount;
        private boolean isFriend;

        public static UserSearchResult fromUser(User user, SearchReason reason, int mutualFriendsCount, boolean isFriend) {
            UserSearchResult result = new UserSearchResult();
            result.setId(user.getId());
            result.setEmail(user.getEmail());
            result.setFirstName(user.getFirstName());
            result.setLastName(user.getLastName());
            result.setAvatarUrl(user.getAvatarUrl());
            result.setAddress(user.getAddress());
            result.setBiography(user.getBiography());
            result.setReason(reason);
            result.setMutualFriendsCount(mutualFriendsCount);
            result.setFriend(isFriend);
            return result;
        }
    }

    public enum SearchReason {
        FRIEND("Bạn bè"),
        MUTUAL_FRIEND("Bạn chung"),
        SAME_PROVINCE("Cùng tỉnh"),
        RANDOM("Đề xuất");

        private final String displayName;

        SearchReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}