package com.example.backend.dto.request;

import com.example.backend.entity.enums.Language;
import com.example.backend.entity.enums.Nationality;
import com.example.backend.entity.enums.PreferredFoodType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserPreferencesRequest {

    private Nationality nationality;
    private Language language;
    private PreferredFoodType preferredFoodType;
}