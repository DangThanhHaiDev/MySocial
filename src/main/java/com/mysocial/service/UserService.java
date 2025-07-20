package com.mysocial.service;

import com.mysocial.config.JwtProvider;
import com.mysocial.dto.user.request.UserUpdateRequest;
import com.mysocial.dto.user.response.ProfileResponse;
import com.mysocial.dto.user.response.UserSearchResponse;
import com.mysocial.model.User;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    public User findUserProfileByJwt(String jwt){
        String email = jwtProvider.getEmailFromToken(jwt);
        User user = userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found with email: "+email));
        return user;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Long getUserIdFromToken(String token) {
        String email = jwtProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return user.getId();
    }

    public User updateUserHandler(User user, UserUpdateRequest request){
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(request.isGender());
        user.setBirthDate(request.getBirthDay());
        user.setAddress(request.getAddress());
        user.setBiography(request.getBio());
        return userRepository.save(user);
    }
    public ProfileResponse getUserById(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        ProfileResponse response = new ProfileResponse();
        response.setUserId(userId);
        response.setBio(user.getBiography());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAddress(user.getAddress());
        response.setBirthDate(user.getBirthDate());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        return response;
    }
    public String updateAvatar(User user, String imageUrl){
        if(imageUrl.equals("")){
            return "Ảnh lỗi";
        }
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
        return "Update thành công";
    }

    public UserSearchResponse searchUsers(Long currentUserId, String searchTerm, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User currentUser = currentUserOpt.get();

        // Lấy tất cả kết quả tìm kiếm trước, sau đó phân trang
        List<UserSearchResponse.UserSearchResult> allResults = getAllSearchResults(currentUserId, currentUser, searchTerm);

        // Tính toán pagination
        int totalElements = allResults.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        // Lấy dữ liệu cho trang hiện tại
        List<UserSearchResponse.UserSearchResult> pagedResults =
                startIndex < totalElements ? allResults.subList(startIndex, endIndex) : new ArrayList<>();

        // Tạo response
        UserSearchResponse response = new UserSearchResponse();
        response.setUsers(pagedResults);
        response.setCurrentPage(page);
        response.setPageSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);

        return response;
    }

    private List<UserSearchResponse.UserSearchResult> getAllSearchResults(Long currentUserId, User currentUser, String searchTerm) {
        List<UserSearchResponse.UserSearchResult> allResults = new ArrayList<>();
        Set<Long> addedUserIds = new HashSet<>();

        // 1. Tìm kiếm trong bạn bè (ưu tiên cao nhất)
        List<User> friends = searchFriends(currentUserId, searchTerm);
        for (User friend : friends) {
            if (!addedUserIds.contains(friend.getId())) {
                allResults.add(UserSearchResponse.UserSearchResult.fromUser(friend, UserSearchResponse.SearchReason.FRIEND, 0, true));
                addedUserIds.add(friend.getId());
            }
        }

        // 2. Tìm kiếm trong bạn chung
        List<User> mutualFriends = searchMutualFriends(currentUserId, searchTerm);
        for (User user : mutualFriends) {
            if (!addedUserIds.contains(user.getId())) {
                int mutualCount = calculateMutualFriendsCount(currentUserId, user.getId());
                allResults.add(UserSearchResponse.UserSearchResult.fromUser(user, UserSearchResponse.SearchReason.MUTUAL_FRIEND, mutualCount, false));
                addedUserIds.add(user.getId());
            }
        }

        // 3. Tìm kiếm theo địa chỉ (cùng tỉnh)
        if (currentUser.getAddress() != null) {
            String province = extractProvince(currentUser.getAddress());
            if (province != null) {
                List<User> sameProvinceUsers = searchUsersByProvince(currentUserId, province, searchTerm);
                for (User user : sameProvinceUsers) {
                    if (!addedUserIds.contains(user.getId())) {
                        allResults.add(UserSearchResponse.UserSearchResult.fromUser(user, UserSearchResponse.SearchReason.SAME_PROVINCE, 0, false));
                        addedUserIds.add(user.getId());
                    }
                }
            }
        }

        // 4. Tìm kiếm ngẫu nhiên
        List<User> randomUsers = searchRandomUsers(currentUserId, searchTerm);
        for (User user : randomUsers) {
            if (!addedUserIds.contains(user.getId())) {
                allResults.add(UserSearchResponse.UserSearchResult.fromUser(user, UserSearchResponse.SearchReason.RANDOM, 0, false));
                addedUserIds.add(user.getId());
            }
        }

        return allResults;
    }

    private List<User> searchFriends(Long currentUserId, String searchTerm) {
        List<User> allFriends = new ArrayList<>();
        int pageSize = 50;
        int currentPage = 0;

        while (true) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.searchFriends(currentUserId, searchTerm, pageable);

            allFriends.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }
            currentPage++;
        }

        return allFriends;
    }

    private List<User> searchMutualFriends(Long currentUserId, String searchTerm) {
        List<User> allMutualFriends = new ArrayList<>();
        int pageSize = 50;
        int currentPage = 0;

        while (true) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.searchMutualFriends(currentUserId, searchTerm, pageable);

            allMutualFriends.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }
            currentPage++;
        }

        return allMutualFriends;
    }

    private List<User> searchUsersByProvince(Long currentUserId, String province, String searchTerm) {
        List<User> allUsers = new ArrayList<>();
        int pageSize = 50;
        int currentPage = 0;

        while (true) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.searchUsersByProvince(currentUserId, province, searchTerm, pageable);

            allUsers.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }
            currentPage++;
        }

        return allUsers;
    }

    private List<User> searchRandomUsers(Long currentUserId, String searchTerm) {
        List<User> allUsers = new ArrayList<>();
        int pageSize = 50;
        int currentPage = 0;
        int maxPages = 4; // Giới hạn để tránh load quá nhiều

        while (currentPage < maxPages) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.searchRandomUsers(currentUserId, searchTerm, pageable);

            allUsers.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }
            currentPage++;
        }

        return allUsers;
    }

    private String extractProvince(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        String[] parts = address.split(",");
        if (parts.length >= 2) {
            String province = parts[parts.length - 1].trim();

            if (province.startsWith("Thành phố ")) {
                province = province.substring("Thành phố ".length());
            } else if (province.startsWith("Tỉnh ")) {
                province = province.substring("Tỉnh ".length());
            }

            return province;
        }
        return address.trim();
    }

    private int calculateMutualFriendsCount(Long userId1, Long userId2) {
        Long count = userRepository.countMutualFriends(userId1, userId2);
        return count != null ? count.intValue() : 0;
    }
}
