package ua.nulp.order_service.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.nulp.order_service.config.CustomFeignErrorDecoder;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        // Реєструємо ваш кастомний декодер як Бін
        return new CustomFeignErrorDecoder();
    }
}
