package com.example.backend.entity;

import com.example.backend.entity.enums.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "post_translated", schema = "communicare",
    uniqueConstraints = {
        @UniqueConstraint(name="uq_post_lang", columnNames = {"post_id", "language"})
    },
    indexes = {
        @Index(name="ix_post_translated_post", columnList = "post_id")
    }
)
public class PostTranslated {
    @Id
    @Column(name = "postTranslated_id", columnDefinition = "uuid")
    private UUID translationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="post_id", nullable=false, foreignKey=@ForeignKey(name="fk_translated_post"))
    private Post post;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "language",
        nullable = false,
        columnDefinition = "communicare.language"
    )
    private Language language;

    @Column(columnDefinition = "text")
    private String translatedTitle;

    @Column(columnDefinition = "text")
    private String translatedContent;

    @Column(nullable = false)
    private OffsetDateTime translatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (translationId == null) {
            translationId = UUID.randomUUID();
        }
        if (translatedAt == null) {
            translatedAt = now;
        }
    }
}
