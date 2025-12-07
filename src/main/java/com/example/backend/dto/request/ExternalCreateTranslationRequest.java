package com.example.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalCreateTranslationRequest {
    private String source_lang; // "English"
    private String target_lang; // "Korean"
    private String text; // "A: FYI, I’m heading out early today. TTYL! B: OK, g2g too. msg me later! A: LOL, that meme was so funny. IMO it’s the best one today. B: Yeah agree. post it on FaceBook ASAP!"
}
