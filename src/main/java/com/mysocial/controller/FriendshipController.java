package com.mysocial.controller;

import com.mysocial.dto.user.response.FriendSuggestionResponse;
import com.mysocial.model.Friendship;
import com.mysocial.model.User;
import com.mysocial.model.Notification;
import com.mysocial.service.FriendshipService;
import com.mysocial.service.UserService;
import com.mysocial.service.NotificationService;
import com.mysocial.ws.NotificationWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {
    @Autowired
    private FriendshipService friendshipService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationWebSocketController notificationWebSocketController;

    @PostMapping("/request/{userId}")
    public Friendship sendFriendRequest(@RequestHeader("Authorization") String jwt, @PathVariable Long userId) {
        User requester = userService.findUserProfileByJwt(jwt);
        User addressee = userService.findUserById(userId);
        Friendship friendship = friendshipService.sendFriendRequest(requester, addressee);

        Notification notification = new Notification();
        notification.setUser(addressee);
        notification.setType("FRIEND_REQUEST");
        notification.setMessage(requester.getFirstName() + " " + requester.getLastName() + " đã gửi lời mời kết bạn cho bạn!");
        notification.setRelatedUserId(requester.getId());
        notification.setIsRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setReferenceId(friendship.getId());
        notificationService.createNotification(notification);
        notificationWebSocketController.sendNotificationToUser(notification);
        return friendship;
    }

    @PostMapping("/accept/{friendshipId}")
    public Friendship acceptFriendRequest(@PathVariable Long friendshipId) {
        Friendship friendship = friendshipService.acceptFriendRequest(friendshipId);

        User requester = friendship.getRequester();
        User addressee = friendship.getAddressee();
        Notification notification = new Notification();
        notification.setUser(requester);
        notification.setType("FRIEND_ACCEPT");
        notification.setMessage(addressee.getFirstName() + " " + addressee.getLastName() + " đã chấp nhận lời mời kết bạn của bạn!");
        notification.setRelatedUserId(addressee.getId());
        notification.setIsRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setReferenceId(friendship.getId());
        notificationService.createNotification(notification);
        notificationWebSocketController.sendNotificationToUser(notification);
        return friendship;
    }

    @GetMapping("/list")
    public List<User> getFriends(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserProfileByJwt(jwt);
        return friendshipService.getFriends(user);
    }



    @GetMapping("/requests")
    public List<Friendship> getPendingRequests(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserProfileByJwt(jwt);
        return friendshipService.getPendingRequests(user);
    }

    @PostMapping("/decline/{friendshipId}")
    public void declineFriendRequest(@PathVariable Long friendshipId) {
        friendshipService.declineFriendRequest(friendshipId);
    }

    @PostMapping("/unfriend/{userId}")
    public void unfriend(@RequestHeader("Authorization") String jwt, @PathVariable Long userId) {
        User user = userService.findUserProfileByJwt(jwt);
        User friend = userService.findUserById(userId);
        friendshipService.unfriend(user, friend);
    }

    @GetMapping("/search")
    public List<User> searchFriends(
        @RequestHeader("Authorization") String jwt,
        @RequestParam("q") String keyword
    ) {
        User user = userService.findUserProfileByJwt(jwt);
        List<User> friends = friendshipService.getFriends(user);
        return friends.stream()
            .filter(u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(keyword.toLowerCase()))
            .toList();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFriendsByUser(@PathVariable Long userId){
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(friendshipService.getFriendsProfile(user));
    }

    @GetMapping("/relationship/{userId}")
    public ResponseEntity<?> getRelationship(@RequestHeader("Authorization") String jwt, @PathVariable Long userId){
        User user = userService.findUserProfileByJwt(jwt);
        return new ResponseEntity<>(friendshipService.getRelationship(user, userId), HttpStatus.OK);
    }
    @PostMapping("/accept/profile/{userId}")
    public Friendship acceptFriendRequestProfile(@PathVariable Long userId, @RequestHeader("Authorization") String jwt) {
        User user = userService.findUserProfileByJwt(jwt);
        Friendship friendship = friendshipService.acceptFriendRequestProfile(user.getId(), userId);

        User requester = friendship.getRequester();
        User addressee = friendship.getAddressee();
        Notification notification = new Notification();
        notification.setUser(requester);
        notification.setType("FRIEND_ACCEPT");
        notification.setMessage(addressee.getFirstName() + " " + addressee.getLastName() + " đã chấp nhận lời mời kết bạn của bạn!");
        notification.setRelatedUserId(addressee.getId());
        notification.setIsRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setReferenceId(friendship.getId());
        notificationService.createNotification(notification);
        notificationWebSocketController.sendNotificationToUser(notification);
        return friendship;
    }

    @PostMapping("/decline/profile/{userId}")
    public void declineFriendRequestProfile(@PathVariable Long userId, @RequestHeader("Authorization") String jwt) {
        User user = userService.findUserProfileByJwt(jwt);
        friendshipService.declineFriendRequestProfile(user.getId(), userId);
    }
    @GetMapping("/mutual/{userId}")
    public ResponseEntity<?> getMutualFriends(@RequestHeader("Authorization") String jwt, @PathVariable Long userId){
        User user = userService.findUserProfileByJwt(jwt);
        return ResponseEntity.ok(friendshipService.getMutualFriends(user.getId(), userId));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<FriendSuggestionResponse> getFriendSuggestions(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.findUserProfileByJwt(jwt);

        try {
            FriendSuggestionResponse response = friendshipService
                    .getFriendSuggestions(user.getId(), page, size);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group/friend/{groupId}")
    public List<User> getFriendForGroup(@RequestHeader("Authorization") String jwt,
                                        @PathVariable("groupId") Long groupId){
        User user = userService.findUserProfileByJwt(jwt);

        return friendshipService.getFriendsForGroup(user, groupId);
    }
} 