package com.example.backend.dto.response;

import com.example.backend.entity.enums.Language;
import com.example.backend.entity.enums.Nationality;
import com.example.backend.entity.enums.PreferredFoodType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPreferencesResponse {

    private Nationality nationality;
    private Language language;
    private PreferredFoodType preferredFoodType;
}