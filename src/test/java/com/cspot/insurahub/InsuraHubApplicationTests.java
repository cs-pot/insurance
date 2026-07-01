package com.cspot.insurahub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InsuraHubApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloWorld() throws Exception {
        mockMvc.perform(get("/api/public/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void privateHelloWorld() throws Exception {
        mockMvc.perform(get("/api/private/hello"))
                .andExpect(status().isForbidden());
    }
}
