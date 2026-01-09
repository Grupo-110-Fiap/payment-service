package br.com.fiap.techchallenge.paymentservice.controller

import br.com.fiap.techchallenge.paymentservice.domain.OrderPayment
import br.com.fiap.techchallenge.paymentservice.dto.OrderPaymentRequest
import br.com.fiap.techchallenge.paymentservice.service.PaymentService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal

class PaymentControllerTest {

    private val service = mockk<PaymentService>()
    private val controller = PaymentController(service)
    private val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

    @Test
    fun `should create payment via post`() {
        val request = OrderPaymentRequest(orderId = "123", amount = BigDecimal.TEN)
        val payment = OrderPayment(orderId = "123", amount = BigDecimal.TEN, status = "created")

        every { service.createPayment(any()) } returns payment

        mockMvc.post("/api/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = "{\"orderId\": \"123\", \"amount\": 10.0}"
        }.andExpect {
            status { isCreated() }
            jsonPath("$.orderId") { value("123") }
            jsonPath("$.status") { value("created") }
        }
    }
}
