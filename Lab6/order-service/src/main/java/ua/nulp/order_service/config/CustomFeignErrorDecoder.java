package ua.nulp.order_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.codec.ErrorDecoder;
import ua.nulp.order_service.exception.ApiErrorResponse;
import ua.nulp.order_service.exception.ExternalServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

public class CustomFeignErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;

    public CustomFeignErrorDecoder() {
        this.objectMapper = new ObjectMapper();
        // Обов'язково додаємо модуль для підтримки LocalDateTime
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        System.out.println("❌ Feign отримав помилку зі статусом: " + response.status() + " від методу: " + methodKey);
        // 1. Перевіряємо, чи є взагалі тіло у відповіді
        if (response.body() == null) {
            // Якщо тіла немає, створюємо помилку на основі статус-коду
            ApiErrorResponse fallbackError = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(response.status())
                    .error("External Service Error")
                    .message("Сервіс повернув помилку без опису: " + response.reason())
                    .path("") // Шлях невідомий
                    .build();
            return new ExternalServiceException(response.status(), fallbackError);
        }

        // 2. Якщо тіло є, намагаємося його розпарсити
        try (InputStream bodyIs = response.body().asInputStream()) {
            ApiErrorResponse error = objectMapper.readValue(bodyIs, ApiErrorResponse.class);
            return new ExternalServiceException(response.status(), error);
        } catch (IOException e) {
            // Якщо JSON битий або не відповідає структурі
            ApiErrorResponse parseError = ApiErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(response.status())
                    .error("Proxy Error")
                    .message("Не вдалося розпізнати формат помилки: " + response.reason())
                    .build();
            return new ExternalServiceException(response.status(), parseError);
        }
    }
}