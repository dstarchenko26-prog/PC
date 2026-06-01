package ua.nulp.order_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            String authorizationHeader = attributes.getRequest().getHeader("Authorization");
            // ДОДАЙТЕ ЦЕЙ РЯДОК:
            System.out.println("👉 Feign Interceptor зловив заголовок: " + authorizationHeader);

            if (authorizationHeader != null) {
                template.header("Authorization", authorizationHeader);
            }
        } else {
            // І ЦЕЙ РЯДОК:
            System.out.println("❌ Feign Interceptor: attributes дорівнює NULL (запит йде в іншому потоці)");
        }
    }
}
