package com.mysocial.ws;

import com.mysocial.model.Friendship;
import com.mysocial.model.User;
import com.mysocial.service.FriendshipService;
import com.mysocial.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FriendWebSocketController {
    @Autowired
    private FriendshipService friendshipService;
    @Autowired
    private UserService userService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Khi gửi lời mời kết bạn, gửi thông báo real-time đến user nhận
    @MessageMapping("/friends/request/{addresseeId}")
    public void sendFriendRequest(
            @Header("Authorization") String jwt,
            @DestinationVariable Long addresseeId
    ) {
        User requester = userService.findUserProfileByJwt(jwt);
        User addressee = userService.findUserById(addresseeId);
        Friendship friendship = friendshipService.sendFriendRequest(requester, addressee);
        // Gửi thông báo đến user nhận
        messagingTemplate.convertAndSend("/topic/friends/" + addresseeId, friendship);
    }

    // Chấp nhận lời mời kết bạn (B accept A)
    @MessageMapping("/friends/accept/{friendshipId}")
    public void acceptFriendRequest(
            @Header("Authorization") String jwt,
            @DestinationVariable Long friendshipId
    ) {
        Friendship friendship = friendshipService.acceptFriendRequest(friendshipId);
        // Gửi thông báo cho cả requester và addressee
        messagingTemplate.convertAndSend("/topic/friends/" + friendship.getRequester().getId(), friendship);
        messagingTemplate.convertAndSend("/topic/friends/" + friendship.getAddressee().getId(), friendship);
    }

    // Từ chối lời mời kết bạn (B decline A)

} 