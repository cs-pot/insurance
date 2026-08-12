package com.cspot.insurahub.notification.renderer;

import com.cspot.insurahub.notification.exception.EmailTemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PlainTextEmailRenderer {

    private final ResourceLoader resourceLoader;

    public String render(String templateName, Map<String, Object> values) {
        Resource resource = resourceLoader.getResource(
                "classpath:email/" + templateName + ".txt"
        );

        try (InputStream inputStream = resource.getInputStream()) {
            String template = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String result = template;

            for (var entry : values.entrySet()) {
                result = result.replace(
                        "{{" + entry.getKey() + "}}",
                        entry.getValue().toString()
                );
            }

            return result;
        } catch (IOException e) {
            throw new EmailTemplateException(
                    "Failed to load email template: " + templateName,
                    e
            );
        }
    }
}
