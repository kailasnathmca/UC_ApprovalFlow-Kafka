package main.java.com.ipm.common.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Intercepts all @RequestBody payloads and sanitizes String fields.
 */
@ControllerAdvice
public class SanitizingRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // Apply to all request bodies
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                                MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {

        // Recursively sanitize all String fields
        sanitizeStrings(body);
        return body;
    }

    private void sanitizeStrings(Object obj) {
        if (obj == null) return;

        var fields = obj.getClass().getDeclaredFields();
        for (var field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        field.set(obj, SanitizationUtils.stripAllHtml(value.trim()));
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
    }
}