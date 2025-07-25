package com.mysocial.repository;

import com.mysocial.model.Post;
import com.mysocial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserId(@Param("userId") Long userId, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.isDeleted = false and p.privacy =:privacy ORDER BY p.createdAt DESC")
    List<Post> findPostsByUserIdAndRelated(@Param("userId") Long userId, @Param("privacy")Post.Privacy privacy);

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findByUserAndIsDeletedFalse(@Param("userId") Long userId);
    @Query("""
    SELECT p FROM Post p
    WHERE 
        p.isDeleted = false
        AND (
            p.user.id = :userId
            OR (
                p.privacy = com.mysocial.model.Post.Privacy.PUBLIC
                AND p.user.id IN :friendIds
            )
            OR (
                p.privacy = com.mysocial.model.Post.Privacy.FRIENDS
                AND p.user.id IN :friendIds
            )
        )
    ORDER BY p.createdAt DESC
    """)
    List<Post> findFeedPosts(@Param("userId") Long userId, @Param("friendIds") List<Long> friendIds, Pageable pageable);
}
