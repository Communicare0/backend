package com.example.backend.dto.request;

import com.example.backend.entity.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostTranslatedRequest {
    private UUID postId;
    private Language language;
}
