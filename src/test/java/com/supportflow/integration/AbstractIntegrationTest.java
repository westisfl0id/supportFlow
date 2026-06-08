package com.supportflow.integration;

import com.supportflow.TicketSystemApplication;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TicketSystemApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
}