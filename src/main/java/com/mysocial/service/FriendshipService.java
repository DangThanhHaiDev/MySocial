package com.mysocial.service;

import com.mysocial.model.Friendship;
import com.mysocial.model.User;
import com.mysocial.repository.FriendshipRepository;
import com.mysocial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

    public List<User> suggestFriends(User user) {
        List<User> allUsers = userRepository.findAll();
        List<User> friends = getFriends(user);
        List<Friendship> requests = friendshipRepository.findByRequesterOrAddressee(user, user);

        return allUsers.stream()
                .filter(u -> !u.equals(user))
                .filter(u -> !friends.contains(u))
                .filter(u -> requests.stream().noneMatch(f ->
                        (f.getRequester().equals(user) && f.getAddressee().equals(u)) ||
                        (f.getRequester().equals(u) && f.getAddressee().equals(user))
                ))
                .collect(Collectors.toList());
    }

    public List<Friendship> getPendingRequests(User user) {
        System.out.println(user.getId());
        return friendshipRepository.findByAddressee_IdAndStatus(user.getId(), Friendship.Status.PENDING);
    }

    public void declineFriendRequest(Long friendshipId) {
//        Friendship friendship = friendshipRepository.findById(friendshipId)
//                .orElseThrow(() -> new RuntimeException("Friend request not found"));
//        friendship.setStatus(Friendship.Status.REJECTED);
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

} 