package ua.nulp.lab2.dto.review;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Long buyerId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
