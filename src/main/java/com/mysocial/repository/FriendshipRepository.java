package com.mysocial.repository;

import com.mysocial.model.Friendship;
import com.mysocial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findByRequesterOrAddressee(User requester, User addressee);
    boolean existsByRequesterAndAddressee(User requester, User addressee);
    @Query("SELECT f from Friendship f where f.addressee.id=:addresseeId and f.status=:status")
    List<Friendship> findByAddressee_IdAndStatus(Long addresseeId, Friendship.Status status);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.addressee.id = :userId AND f.requester.id = :targetId) OR " +
            "(f.addressee.id = :targetId AND f.requester.id = :userId)")
    Friendship findRelationship(@Param("userId") Long userId,
                                @Param("targetId") Long targetId);

    @Query("""
SELECT f.addressee FROM Friendship f 
WHERE f.requester.id = :userId AND f.status = com.mysocial.model.Friendship.Status.ACCEPTED
UNION
SELECT f.requester FROM Friendship f 
WHERE f.addressee.id = :userId AND f.status = com.mysocial.model.Friendship.Status.ACCEPTED
""")
    List<User> findFriendsOf(@Param("userId") Long userId);


}