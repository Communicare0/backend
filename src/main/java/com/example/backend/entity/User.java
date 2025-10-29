package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 테이블명 users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer age;

    public User() {}

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getAge() { return age; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(Integer age) { this.age = age; }
}
