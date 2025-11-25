package com.example.backend.dto.request;

import com.example.backend.entity.enums.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    private String title;
    private String content;
//    private PostCategory category;
}
