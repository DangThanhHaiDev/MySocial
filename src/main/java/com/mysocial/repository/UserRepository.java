package com.mysocial.repository;

import com.mysocial.model.Friendship;
import com.mysocial.model.GroupMember;
import com.mysocial.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);


    @Query("""
        SELECT DISTINCT u FROM User u 
        WHERE u.id != :userId 
        AND u.id NOT IN (
            SELECT CASE 
                WHEN f.requester.id = :userId THEN f.addressee.id 
                ELSE f.requester.id 
            END
            FROM Friendship f 
            WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
            AND f.status IN ('ACCEPTED', 'PENDING')
        )
        AND u.id IN (
            SELECT CASE 
                WHEN f2.requester.id = friend.id THEN f2.addressee.id 
                ELSE f2.requester.id 
            END
            FROM Friendship f1
            JOIN User friend ON (
                CASE 
                    WHEN f1.requester.id = :userId THEN f1.addressee.id 
                    ELSE f1.requester.id 
                END = friend.id
            )
            JOIN Friendship f2 ON (f2.requester.id = friend.id OR f2.addressee.id = friend.id)
            WHERE (f1.requester.id = :userId OR f1.addressee.id = :userId)
            AND f1.status = 'ACCEPTED'
            AND f2.status = 'ACCEPTED'
            AND CASE 
                WHEN f2.requester.id = friend.id THEN f2.addressee.id 
                ELSE f2.requester.id 
            END != :userId
        )
        ORDER BY u.firstName, u.lastName
        """)
    Page<User> findMutualFriends(@Param("userId") Long userId, Pageable pageable);

    // Tìm người cùng tỉnh (dựa trên address)
    @Query("""
        SELECT u FROM User u 
        WHERE u.id != :userId 
        AND u.address LIKE %:province%
        AND u.id NOT IN (
            SELECT CASE 
                WHEN f.requester.id = :userId THEN f.addressee.id 
                ELSE f.requester.id 
            END
            FROM Friendship f 
            WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
            AND f.status IN ('ACCEPTED', 'PENDING')
        )
        ORDER BY u.firstName, u.lastName
        """)
    Page<User> findUsersByProvince(@Param("userId") Long userId, @Param("province") String province, Pageable pageable);

    // Lấy người dùng ngẫu nhiên (fallback)
    @Query(value = """
        SELECT * FROM user u 
        WHERE u.id != :userId 
        AND u.id NOT IN (
            SELECT CASE 
                WHEN f.requester_id = :userId THEN f.addressee_id 
                ELSE f.requester_id 
            END
            FROM friendship f 
            WHERE (f.requester_id = :userId OR f.addressee_id = :userId)
            AND f.status IN ('ACCEPTED', 'PENDING')
        )
        ORDER BY RAND()
        """, nativeQuery = true)
    Page<User> findRandomUsers(@Param("userId") Long userId, Pageable pageable);

    // Lấy danh sách ID của bạn bè và pending requests
    @Query("""
        SELECT CASE 
            WHEN f.requester.id = :userId THEN f.addressee.id 
            ELSE f.requester.id 
        END
        FROM Friendship f 
        WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
        AND f.status IN ('ACCEPTED', 'PENDING')
        """)
    List<Long> findFriendAndPendingIds(@Param("userId") Long userId);

    // Lấy danh sách ID của bạn bè (chỉ ACCEPTED)
    @Query("""
        SELECT CASE 
            WHEN f.requester.id = :userId THEN f.addressee.id 
            ELSE f.requester.id 
        END
        FROM Friendship f 
        WHERE (f.requester.id = :userId OR f.addressee.id = :userId)
        AND f.status = 'ACCEPTED'
        """)
    List<Long> getFriendIds(@Param("userId") Long userId);

    // Đếm số bạn chung giữa 2 user
    @Query("""
        SELECT COUNT(DISTINCT mutual.id) FROM User mutual
        WHERE mutual.id IN (
            SELECT CASE 
                WHEN f1.requester.id = :userId1 THEN f1.addressee.id 
                ELSE f1.requester.id 
            END
            FROM Friendship f1 
            WHERE (f1.requester.id = :userId1 OR f1.addressee.id = :userId1)
            AND f1.status = 'ACCEPTED'
        )
        AND mutual.id IN (
            SELECT CASE 
                WHEN f2.requester.id = :userId2 THEN f2.addressee.id 
                ELSE f2.requester.id 
            END
            FROM Friendship f2 
            WHERE (f2.requester.id = :userId2 OR f2.addressee.id = :userId2)
            AND f2.status = 'ACCEPTED'
        )
        """)
    Long countMutualFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    //Tìm kiếm bạn bè
    @Query("SELECT u FROM User u " +
            "JOIN Friendship f ON (f.requester.id = u.id OR f.addressee.id = u.id) " +
            "WHERE (f.requester.id = :currentUserId OR f.addressee.id = :currentUserId) " +
            "AND u.id != :currentUserId " +
            "AND f.status = 'ACCEPTED' " +
            "AND (LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY u.firstName, u.lastName")

    Page<User> searchFriends(@Param("currentUserId") Long currentUserId,
                             @Param("searchTerm") String searchTerm,
                             Pageable pageable);


    @Query("SELECT DISTINCT u FROM User u " +
            "WHERE u.id != :currentUserId " +
            "AND u.id NOT IN (" +
            "    SELECT CASE WHEN f.requester.id = :currentUserId THEN f.addressee.id ELSE f.requester.id END " +
            "    FROM Friendship f " +
            "    WHERE (f.requester.id = :currentUserId OR f.addressee.id = :currentUserId) " +
            "    AND f.status = 'ACCEPTED'" +
            ") " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Friendship f1 " +
            "    JOIN Friendship f2 ON (f1.requester.id = f2.requester.id OR f1.requester.id = f2.addressee.id OR f1.addressee.id = f2.requester.id OR f1.addressee.id = f2.addressee.id) " +
            "    WHERE (f1.requester.id = :currentUserId OR f1.addressee.id = :currentUserId) " +
            "    AND (f2.requester.id = u.id OR f2.addressee.id = u.id) " +
            "    AND f1.status = 'ACCEPTED' AND f2.status = 'ACCEPTED' " +
            "    AND f1.id != f2.id" +
            ") " +
            "AND (" +
            "     LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
            ") " +
            "ORDER BY u.firstName, u.lastName")

    Page<User> searchMutualFriends(@Param("currentUserId") Long currentUserId,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);

    // Tìm kiếm theo tỉnh
    @Query("SELECT u FROM User u " +
            "WHERE u.id != :currentUserId " +
            "AND u.id NOT IN (" +
            "    SELECT CASE WHEN f.requester.id = :currentUserId THEN f.addressee.id ELSE f.requester.id END " +
            "    FROM Friendship f " +
            "    WHERE (f.requester.id = :currentUserId OR f.addressee.id = :currentUserId) " +
            "    AND f.status = 'ACCEPTED'" +
            ") " +
            "AND u.address IS NOT NULL " +
            "AND (u.address LIKE CONCAT('%', :province, '%') " +
            "     OR u.address LIKE CONCAT('%Thành phố ', :province, '%') " +
            "     OR u.address LIKE CONCAT('%Tỉnh ', :province, '%')) " +
            "AND (" +
            "     LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
            ") " +
            "ORDER BY u.firstName, u.lastName")
    Page<User> searchUsersByProvince(@Param("currentUserId") Long currentUserId,
                                     @Param("province") String province,
                                     @Param("searchTerm") String searchTerm,
                                     Pageable pageable);
    @Query("SELECT u FROM User u " +
            "WHERE u.id != :currentUserId " +
            "AND u.id NOT IN (" +
            "    SELECT CASE WHEN f.requester.id = :currentUserId THEN f.addressee.id ELSE f.requester.id END " +
            "    FROM Friendship f " +
            "    WHERE (f.requester.id = :currentUserId OR f.addressee.id = :currentUserId) " +
            "    AND f.status = 'ACCEPTED'" +
            ") " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY RAND()")
    Page<User> searchRandomUsers(@Param("currentUserId") Long currentUserId,
                                 @Param("searchTerm") String searchTerm,
                                 Pageable pageable);

    @Query("""
SELECT u FROM User u
WHERE u.id != :currentUserId
AND (
    EXISTS (
        SELECT 1 FROM Friendship f
        WHERE f.status = :acceptedStatus
        AND (
            (f.requester.id = :currentUserId AND f.addressee.id = u.id)
            OR
            (f.addressee.id = :currentUserId AND f.requester.id = u.id)
        )
    )
)
AND NOT EXISTS (
    SELECT 1 FROM GroupMember gm
    WHERE gm.group.id = :groupId
    AND gm.status = :activeStatus
    AND gm.user.id = u.id
)
""")
    List<User> findFriendsNotInActiveGroup(
            @Param("currentUserId") Long currentUserId,
            @Param("groupId") Long groupId,
            @Param("acceptedStatus") Friendship.Status acceptedStatus,
            @Param("activeStatus") GroupMember.MemberStatus activeStatus
    );

    Page<User> findByStatus(String status, Pageable pageable);


}

