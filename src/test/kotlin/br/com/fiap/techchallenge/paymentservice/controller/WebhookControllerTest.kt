package br.com.fiap.techchallenge.paymentservice.controller

import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoWebhookPayload
import br.com.fiap.techchallenge.paymentservice.service.PaymentService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class WebhookControllerTest {

    private val service = mockk<PaymentService>()
    private val controller = WebhookController(service)
    private val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `should receive notification and process webhook`() {
        every { service.processWebhook(any()) } returns Unit

        mockMvc.post("/api/payments/webhook") {
            contentType = MediaType.APPLICATION_JSON
            content = "{\"type\": \"order\", \"data\": {\"id\": \"mp-123\"}}"
        }.andExpect {
            status { isOk() }
        }

        verify { service.processWebhook(any()) }
    }
}
