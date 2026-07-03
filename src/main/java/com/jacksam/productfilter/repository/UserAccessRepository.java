package com.jacksam.productfilter.repository;

import com.jacksam.productfilter.entity.UserAccess;
import com.jacksam.productfilter.enums.AccessLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccessRepository extends JpaRepository<UserAccess, Long> {

    List<UserAccess> findByUserId(Long userId);

    Optional<UserAccess> findByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT ua.productId FROM UserAccess ua WHERE ua.userId = :userId AND ua.accessLevel IN :levels")
    List<Long> findProductIdsByUserIdAndLevels(
            @Param("userId") Long userId,
            @Param("levels") List<AccessLevel> levels);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
