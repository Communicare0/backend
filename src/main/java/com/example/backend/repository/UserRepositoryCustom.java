package com.example.backend.repository;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.enums.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryCustom {

    // 동적 쿼리를 통한 사용자 검색 (다양한 조건 조합)
    List<UserDto> searchUsers(String email, String nickname, String department,
                              AccountStatus status, int offset, int limit);

    // 사용자 상세 정보 조회 (조인이 필요한 복잡한 쿼리)
    Optional<UserDto> findUserDetailById(UUID userId);

    // 사용자 통계 정보 조회
    Long countUsersByStatus(AccountStatus status);

    // 사용자 목록 페이징 처리
    List<UserDto> findUsersWithPaging(int offset, int limit, String sortBy, boolean ascending);

    // 특정 조건으로 사용자 수 카운트
    Long countUsersByCondition(String email, String nickname, String department, AccountStatus status);
}