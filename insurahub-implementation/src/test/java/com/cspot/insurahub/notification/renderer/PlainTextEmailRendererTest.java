package com.cspot.insurahub.notification.renderer;

import com.cspot.insurahub.notification.exception.EmailTemplateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlainTextEmailRendererTest {

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @InjectMocks
    private PlainTextEmailRenderer renderer;

    @Test
    void shouldLoadTemplateAndReplaceValue() throws IOException {
        String template = """
                Hello {{name}},
                
                Your order {{orderId}} is ready.
                """;

        when(resourceLoader.getResource("classpath:email/order.txt"))
                .thenReturn(resource);
        when(resource.getInputStream())
                .thenReturn(inputStream(template));

        String result = renderer.render(
                "order",
                Map.of(
                        "name", "John",
                        "orderId", "12345"
                )
        );

        assertEquals("""
                Hello John,
                
                Your order 12345 is ready.
                """, result);

        verify(resourceLoader).getResource("classpath:email/order.txt");
        verify(resource).getInputStream();
    }

    @Test
    void shouldLeaveUnknownPlaceholdersUntouched() throws Exception {
        when(resourceLoader.getResource("classpath:email/welcome.txt"))
                .thenReturn(resource);
        when(resource.getInputStream())
                .thenReturn(inputStream("Hello {{name}}, your code is {{code}}."));

        String result = renderer.render(
                "welcome",
                Map.of("name", "John")
        );

        assertEquals("Hello John, your code is {{code}}.", result);
    }

    @Test
    void shouldReplaceRepeatedPlaceholders() throws Exception {
        when(resourceLoader.getResource("classpath:email/test.txt"))
                .thenReturn(resource);
        when(resource.getInputStream())
                .thenReturn(inputStream(
                        "{{name}} ordered something. Thanks, {{name}}!"
                ));

        String result = renderer.render(
                "test",
                Map.of("name", "John")
        );

        assertEquals("John ordered something. Thanks, John!", result);
    }

    @Test
    void shouldConvertValueToString() throws Exception {
        when(resourceLoader.getResource("classpath:email/test.txt"))
                .thenReturn(resource);
        when(resource.getInputStream())
                .thenReturn(inputStream("Order: {{orderId}}, total: {{total}}"));

        String result = renderer.render(
                "test",
                Map.of(
                        "orderId", 12345,
                        "total", 99.95
                )
        );

        assertEquals("Order: 12345, total: 99.95", result);
    }

    @Test
    void shouldWrapIOExceptionInEmailTemplateException() throws Exception {
        IOException cause = new IOException("Unable to read resource");

        when(resourceLoader.getResource("classpath:email/order.txt"))
                .thenReturn(resource);
        when(resource.getInputStream())
                .thenThrow(cause);

        assertThatThrownBy(() ->
                renderer.render("order", Map.of("name", "John"))
        )
                .isInstanceOf(EmailTemplateException.class)
                .hasMessage("Failed to load email template: order")
                .hasCause(cause);
    }

    private static ByteArrayInputStream inputStream(String content) {
        return new ByteArrayInputStream(
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}