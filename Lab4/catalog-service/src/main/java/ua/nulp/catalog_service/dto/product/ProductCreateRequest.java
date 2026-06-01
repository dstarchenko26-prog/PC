package ua.nulp.catalog_service.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {

    @NotNull(message = "ID продавця є обов'язковим")
    private Long sellerId;

    @NotBlank(message = "Назва товару не може бути порожньою")
    private String name;

    private String description;

    @NotNull(message = "Ціна є обов'язковою")
    @Positive(message = "Ціна має бути більшою за нуль")
    private BigDecimal price;

    @NotNull(message = "Кількість товару є обов'язковою")
    @Min(value = 0, message = "Кількість не може бути від'ємною")
    private Integer stockQuantity;
}