package ua.nulp.lab2.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long productId;
    private Long buyerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
