package com.example.usermanagement.repository;

import com.example.usermanagement.entity.LoginAttempt;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, java.util.UUID> {

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username " +
           "AND la.attemptStatus = 'FAILED' AND la.createdAt > :since")
    long countFailedAttemptsByUsernameSince(@Param("username") String username,
                                            @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress " +
           "AND la.attemptStatus = 'FAILED' AND la.createdAt > :since")
    long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress,
                                      @Param("since") LocalDateTime since);

    List<LoginAttempt> findByUsernameAndCreatedAtAfterOrderByCreatedAtDesc(String username,
                                                                           LocalDateTime since);

    @Query("SELECT la FROM LoginAttempt la WHERE la.createdAt < :before")
    List<LoginAttempt> findByCreatedAtBefore(@Param("before") LocalDateTime before);
}
