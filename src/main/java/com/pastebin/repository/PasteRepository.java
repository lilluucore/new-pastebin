package com.pastebin.repository;

import com.pastebin.model.Paste;
import com.pastebin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasteRepository extends JpaRepository<Paste, Long> {
    Optional<Paste> findByUrlKey(String urlKey);
    List<Paste> findByUser(User user);
    List<Paste> findByIsPrivateFalse();

    @Query("SELECT p FROM Paste p WHERE p.isPrivate = false AND (p.expiresAt IS NULL OR p.expiresAt > :now)")
    List<Paste> findPublicAndNotExpired(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Paste p WHERE p.user = :user AND (p.expiresAt IS NULL OR p.expiresAt > :now)")
    List<Paste> findByUserAndNotExpired(@Param("user") User user, @Param("now") LocalDateTime now);

    boolean existsByUrlKey(String urlKey);
}
