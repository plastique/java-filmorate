package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Genre {
    private Long id;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String name;
}
