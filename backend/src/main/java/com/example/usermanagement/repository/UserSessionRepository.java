package com.example.usermanagement.repository;

import com.example.usermanagement.entity.UserSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByToken(String token);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    List<UserSession> findByUserIdAndActiveTrue(UUID userId);

    List<UserSession> findByActiveTrueAndExpiresAtBefore(LocalDateTime now);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.token = :token")
    void deactivateByToken(@Param("token") String token);

    @Modifying
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user.id = :userId")
    void deactivateAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.active = false OR s.expiresAt < :now")
    int deleteExpiredSessions(@Param("now") LocalDateTime now);

    long countByUserIdAndActiveTrue(UUID userId);
}
