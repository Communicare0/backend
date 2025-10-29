package com.example.v1.repository;

import com.example.v1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // 메서드 이름으로 쿼리 자동 생성 (예: SELECT * FROM users WHERE name = ?)
    List<User> findByName(String name);
}
