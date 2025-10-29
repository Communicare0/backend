package com.example.v1.controller;

import com.example.v1.entity.User;
import com.example.v1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 전체 조회
    @GetMapping
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 단건 조회
    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    // 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User req) {
        return userRepository.save(req);
    }

    // 수정 (전체 필드 교체 느낌)
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setName(req.getName());
        user.setAge(req.getAge());
        return userRepository.save(user);
    }

    // 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    // 이름으로 검색 (예: /users/search?name=홍길동)
    @GetMapping("/search")
    public List<User> searchByName(@RequestParam String name) {
        return userRepository.findByName(name);
    }
}
