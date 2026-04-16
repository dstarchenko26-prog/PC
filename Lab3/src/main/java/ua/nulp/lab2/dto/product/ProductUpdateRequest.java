package ua.nulp.lab2.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    @NotBlank(message = "Назва товару не може бути порожньою")
    private String name;

    private String description;

    @NotNull(message = "Ціна є обов'язковою")
    @DecimalMin(value = "0.01", message = "Ціна повинна бути більшою за 0")
    private BigDecimal price;

    @NotNull(message = "Кількість на складі є обов'язковою")
    @Min(value = 0, message = "Кількість не може бути від'ємною")
    private Integer stockQuantity;
}