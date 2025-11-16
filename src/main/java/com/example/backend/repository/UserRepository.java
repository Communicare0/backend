package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 찾기
    Optional<User> findByNickname(String nickname);

    // 친구 코드로 사용자 찾기
    Optional<User> findByFriendCode(String friendCode);

    // 이메일로 존재 여부 확인
    boolean existsByEmail(String email);

    // 닉네임으로 존재 여부 확인
    boolean existsByNickname(String nickname);

    // 친구 코드로 존재 여부 확인
    boolean existsByFriendCode(String friendCode);

    // 계정 상태로 사용자 목록 찾기
    List<User> findByStatus(AccountStatus status);

    // 학과로 사용자 목록 찾기
    List<User> findByDepartment(String department);

    // 키워드로 사용자 검색 (닉네임 또는 이메일)
    @Query("SELECT u FROM User u WHERE (u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%) AND u.status = :status")
    List<User> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") AccountStatus status);

    // Soft delete된 사용자 제외하고 모든 활성 사용자 찾기
    @Query("SELECT u FROM User u WHERE u.status != 'DELETED'")
    List<User> findAllActiveUsers();
}
