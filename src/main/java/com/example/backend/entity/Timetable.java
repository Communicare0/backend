package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "timetables", schema = "communicare",
  indexes = {
    @Index(name="ix_timetable_user", columnList = "user_id,day_of_week")
  })
public class Timetable {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="timetable_id")
  private Long timetableId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="user_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_timetable_user"))
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name="day_of_week", nullable = false)
  private java.time.DayOfWeek dayOfWeek;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  @Column(length = 100)
  private String subjectName;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;
  // getters/setters ...
}
