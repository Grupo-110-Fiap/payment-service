package br.com.fiap.techchallenge.paymentservice.controller

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class HealthControllerTest {

    private val controller = HealthController()
    private val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `should return UP status`() {
        mockMvc.get("/health")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value("UP") }
            }
    }
}
