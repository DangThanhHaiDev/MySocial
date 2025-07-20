package com.mysocial.service;

import com.mysocial.dto.user.response.FriendSuggestionResponse;
import com.mysocial.model.Friendship;
import com.mysocial.model.GroupMember;
import com.mysocial.model.User;
import com.mysocial.repository.FriendshipRepository;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Autowired
    private UserRepository userRepository;

    public Friendship sendFriendRequest(User requester, User addressee) {
        if (friendshipRepository.existsByRequesterAndAddressee(requester, addressee)) {
            throw new RuntimeException("Friend request already sent or already friends");
        }
        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(Friendship.Status.PENDING);
        friendship.setCreatedAt(LocalDateTime.now());
        return friendshipRepository.save(friendship);
    }

    public Friendship acceptFriendRequest(Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    public List<User> getFriends(User user) {
        List<Friendship> friendships = friendshipRepository.findByRequesterOrAddressee(user, user);
        return friendships.stream()
                .filter(f -> f.getStatus() == Friendship.Status.ACCEPTED)
                .map(f -> f.getRequester().equals(user) ? f.getAddressee() : f.getRequester())
                .collect(Collectors.toList());
    }
    public List<User> getFriendsProfile(User user) {
        List<Friendship> friendships = friendshipRepository.findByRequesterOrAddressee(user, user);
        return friendships.stream()
                .filter(f -> f.getStatus() == Friendship.Status.ACCEPTED)
                .map(f -> f.getRequester().equals(user) ? f.getAddressee() : f.getRequester())
                .limit(6)
                .collect(Collectors.toList());
    }


    public List<Friendship> getPendingRequests(User user) {
        System.out.println(user.getId());
        return friendshipRepository.findByAddressee_IdAndStatus(user.getId(), Friendship.Status.PENDING);
    }

    public void declineFriendRequest(Long friendshipId) {
         friendshipRepository.deleteById(friendshipId);
    }

    public void unfriend(User user, User friend) {
        List<Friendship> friendships = friendshipRepository.findByRequesterOrAddressee(user, user);
        friendships.stream()
            .filter(f -> f.getStatus() == Friendship.Status.ACCEPTED &&
                (f.getRequester().equals(friend) || f.getAddressee().equals(friend)))
            .findFirst()
            .ifPresent(friendshipRepository::delete);
    }

    public String getRelationship(User user, Long targetId) {
        Friendship friendship = friendshipRepository.findRelationship(user.getId(), targetId);

        if (friendship == null) return "NONE";

        Friendship.Status status = friendship.getStatus();

        if (status == Friendship.Status.ACCEPTED) {
            return "ACCEPTED";
        }

        if (status == Friendship.Status.PENDING) {
            // Nếu user là người được nhận lời mời → có thể xác nhận
            if (user.getId().equals(friendship.getAddressee().getId())) {
                return "CONFIRM"; // user nhận lời mời → chờ xác nhận
            } else {
                return "PENDING"; // user là người đã gửi lời mời → chờ phản hồi
            }
        }

        return "NONE";
    }

    public Friendship acceptFriendRequestProfile(Long userId, Long targetId) {
        Friendship friendship = friendshipRepository.findRelationship(userId, targetId);
        friendship.setStatus(Friendship.Status.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    public void declineFriendRequestProfile(Long userId, Long targetId) {
        Friendship friendship = friendshipRepository.findRelationship(userId, targetId);
         friendshipRepository.delete(friendship);
    }

    public List<User> getMutualFriends(Long userAId, Long userBId) {
        List<User> friendsA = friendshipRepository.findFriendsOf(userAId);
        List<User> friendsB = friendshipRepository.findFriendsOf(userBId);
        return friendsA.stream()
                .filter(friendsB::contains)
                .collect(Collectors.toList());
    }

    public FriendSuggestionResponse getFriendSuggestions(Long userId, int page, int size) {
        Optional<User> currentUserOpt = userRepository.findById(userId);
        if (currentUserOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User currentUser = currentUserOpt.get();

        // Lấy tất cả suggestions trước, sau đó mới phân trang
        List<FriendSuggestionResponse.UserSuggestion> allSuggestions = getAllSuggestions(userId, currentUser);

        // Tính toán pagination
        int totalElements = allSuggestions.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        // Lấy dữ liệu cho trang hiện tại
        List<FriendSuggestionResponse.UserSuggestion> pagedSuggestions =
                startIndex < totalElements ? allSuggestions.subList(startIndex, endIndex) : new ArrayList<>();

        // Tạo response
        FriendSuggestionResponse response = new FriendSuggestionResponse();
        response.setSuggestions(pagedSuggestions);
        response.setCurrentPage(page);
        response.setPageSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setHasNext(page < totalPages - 1);
        response.setHasPrevious(page > 0);

        return response;
    }

    private List<FriendSuggestionResponse.UserSuggestion> getAllSuggestions(Long userId, User currentUser) {
        List<FriendSuggestionResponse.UserSuggestion> allSuggestions = new ArrayList<>();

        // 1. Lấy tất cả bạn chung
        List<User> mutualFriends = getAllMutualFriends(userId);
        for (User user : mutualFriends) {
            int mutualCount = calculateMutualFriendsCount(userId, user.getId());
            allSuggestions.add(FriendSuggestionResponse.UserSuggestion.fromUser(
                    user, "Bạn chung", mutualCount));
        }

        // 2. Lấy tất cả người cùng tỉnh (loại bỏ trùng lặp)
        if (currentUser.getAddress() != null) {
            String province = extractProvince(currentUser.getAddress());
            if (province != null) {
                List<User> sameProvinceUsers = getAllUsersByProvince(userId, province);
                for (User user : sameProvinceUsers) {
                    if (allSuggestions.stream().noneMatch(s -> s.getId().equals(user.getId()))) {
                        allSuggestions.add(FriendSuggestionResponse.UserSuggestion.fromUser(
                                user, "Cùng tỉnh", 0));
                    }
                }
            }
        }

        // 3. Lấy thêm user ngẫu nhiên nếu cần (loại bỏ trùng lặp)
        List<User> randomUsers = getAllRandomUsers(userId);
        for (User user : randomUsers) {
            if (allSuggestions.stream().noneMatch(s -> s.getId().equals(user.getId()))) {
                allSuggestions.add(FriendSuggestionResponse.UserSuggestion.fromUser(
                        user, "Đề xuất", 0));
            }
        }

        return allSuggestions;
    }

    private List<User> getAllMutualFriends(Long userId) {
        // Lấy nhiều trang để có đủ dữ liệu
        List<User> allUsers = new ArrayList<>();
        int pageSize = 50; // Tăng page size để giảm số lần query
        int currentPage = 0;

        while (true) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.findMutualFriends(userId, pageable);

            allUsers.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }
            currentPage++;
        }

        return allUsers;
    }

    private List<User> getAllUsersByProvince(Long userId, String province) {
        List<User> allUsers = new ArrayList<>();
        int pageSize = 50;
        int currentPage = 0;

        while (true) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.findUsersByProvince(userId, province, pageable);

            allUsers.addAll(page.getContent());

            if (!page.hasNext()) {
                break;
            }
            currentPage++;
        }

        return allUsers;
    }

    private List<User> getAllRandomUsers(Long userId) {
        List<User> allUsers = new ArrayList<>();
        int pageSize = 50;
        int currentPage = 0;
        int maxPages = 4; // Giới hạn số trang để tránh load quá nhiều

        while (currentPage < maxPages) {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<User> page = userRepository.findRandomUsers(userId, pageable);

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

        // Cải thiện logic extract province
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            String province = parts[parts.length - 1].trim();

            // Chuẩn hóa tên tỉnh
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

    public List<User> getFriendsForGroup(User user, Long groupId){
        return userRepository.findFriendsNotInActiveGroup(user.getId(), groupId, Friendship.Status.ACCEPTED, GroupMember.MemberStatus.ACTIVE);
    }
} 