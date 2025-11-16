package com.example.backend.repository;

import com.example.backend.dto.UserDto;
import com.example.backend.entity.User;
import com.example.backend.entity.enums.AccountStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.backend.entity.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserDto> searchUsers(String email, String nickname, String department,
                                    AccountStatus status, int offset, int limit) {
        return queryFactory
                .selectFrom(user)
                .where(
                        emailContains(email),
                        nicknameContains(nickname),
                        departmentContains(department),
                        statusEq(status)
                )
                .orderBy(user.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findUserDetailById(UUID userId) {
        User foundUser = queryFactory
                .selectFrom(user)
                .where(user.userId.eq(userId))
                .fetchOne();

        return Optional.ofNullable(foundUser)
                .map(this::convertToDto);
    }

    @Override
    public Long countUsersByStatus(AccountStatus status) {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(statusEq(status))
                .fetchOne();
    }

    @Override
    public List<UserDto> findUsersWithPaging(int offset, int limit, String sortBy, boolean ascending) {
        var query = queryFactory
                .selectFrom(user)
                .offset(offset)
                .limit(limit);

        // 정렬 조건 추가
        switch (sortBy.toLowerCase()) {
            case "email":
                query = ascending ? query.orderBy(user.email.asc()) : query.orderBy(user.email.desc());
                break;
            case "nickname":
                query = ascending ? query.orderBy(user.nickname.asc()) : query.orderBy(user.nickname.desc());
                break;
            case "createdat":
                query = ascending ? query.orderBy(user.createdAt.asc()) : query.orderBy(user.createdAt.desc());
                break;
            default:
                query = query.orderBy(user.createdAt.desc());
                break;
        }

        return query.fetch()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countUsersByCondition(String email, String nickname, String department, AccountStatus status) {
        return queryFactory
                .select(user.count())
                .from(user)
                .where(
                        emailContains(email),
                        nicknameContains(nickname),
                        departmentContains(department),
                        statusEq(status)
                )
                .fetchOne();
    }

    // 동적 쿼리를 위한 BooleanExpression 메서드들
    private BooleanExpression emailContains(String email) {
        return email != null && !email.isEmpty() ? user.email.containsIgnoreCase(email) : null;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null && !nickname.isEmpty() ? user.nickname.containsIgnoreCase(nickname) : null;
    }

    private BooleanExpression departmentContains(String department) {
        return department != null && !department.isEmpty() ? user.department.containsIgnoreCase(department) : null;
    }

    private BooleanExpression statusEq(AccountStatus status) {
        return status != null ? user.status.eq(status) : null;
    }

    // User 엔티티를 UserDto로 변환하는 메서드
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .department(user.getDepartment())
                .studentId(user.getStudentId())
                .nationality(user.getNationality())
                .language(user.getLanguage())
                .profileImageUrl(user.getProfileImageUrl())
                .friendCode(user.getFriendCode())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}