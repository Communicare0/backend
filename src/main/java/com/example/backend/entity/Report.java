package com.example.backend.entity;

import com.example.backend.entity.enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", schema = "communicare",
  indexes = {
    @Index(name="ix_report_reporter", columnList = "reporter_id"),
    @Index(name="ix_report_target", columnList = "target_type,target_id")
  })
public class Report {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="report_id")
  private Long reportId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="reporter_id", nullable=false,
    foreignKey=@ForeignKey(name="fk_report_reporter"))
  private User reporter;

  @Enumerated(EnumType.STRING)
  @Column(name="target_type", nullable=false)
  private ReportTargetType targetType;

  @Column(name="target_id", columnDefinition = "uuid", nullable=false)
  private UUID targetId; // Post.postId or Comment.commentId

  @Column(length = 255, nullable = false)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReportStatus status = ReportStatus.RECEIVED;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;

  private OffsetDateTime deletedAt;
  // getters/setters ...
}
