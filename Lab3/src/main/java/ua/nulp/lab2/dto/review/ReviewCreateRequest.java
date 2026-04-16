package ua.nulp.lab2.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "ID товару є обов'язковим")
    private Long productId;

    @NotNull(message = "Оцінка є обов'язковою")
    @Min(value = 1, message = "Мінімальна оцінка - 1")
    @Max(value = 5, message = "Максимальна оцінка - 5")
    private Integer rating;

    @NotBlank(message = "Коментар не може бути порожнім")
    @Size(max = 1000, message = "Коментар не повинен перевищувати 1000 символів")
    private String comment;
}