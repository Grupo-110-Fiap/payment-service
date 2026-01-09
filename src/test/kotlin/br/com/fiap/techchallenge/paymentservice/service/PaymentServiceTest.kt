package br.com.fiap.techchallenge.paymentservice.service

import br.com.fiap.techchallenge.paymentservice.client.MercadoPagoClient
import br.com.fiap.techchallenge.paymentservice.domain.OrderPayment
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderData
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderResponse
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderConfigResponse
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoWebhookPayload
import br.com.fiap.techchallenge.paymentservice.dto.OrderPaymentRequest
import br.com.fiap.techchallenge.paymentservice.messaging.PaymentStatusPublisher
import br.com.fiap.techchallenge.paymentservice.repository.OrderPaymentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentServiceTest {

    private val repository = mockk<OrderPaymentRepository>()
    private val mercadoPagoClient = mockk<MercadoPagoClient>()
    private val publisher = mockk<PaymentStatusPublisher>()
    private val service = PaymentService(repository, mercadoPagoClient, publisher)

    @Test
    fun `should create payment successfully`() {
        val request = OrderPaymentRequest(orderId = "123", amount = BigDecimal.TEN)
        val initialPayment = OrderPayment(orderId = "123", amount = BigDecimal.TEN, status = "created")
        val savedPayment = initialPayment.copy(id = "repo-id")
        
        val mpResponse = MercadoPagoOrderResponse(
            id = "456",
            status = "created",
            externalReference = "123",
            totalAmount = 10.0,
            typeResponse = MercadoPagoOrderConfigResponse(qrData = "test-qr")
        )

        every { repository.save(any<OrderPayment>()) } answers { firstArg() }
        every { mercadoPagoClient.createQrCodeOrder(any(), any()) } returns mpResponse

        val result = service.createPayment(request)

        assertNotNull(result)
        assertEquals("456", result.mercadoPagoId)
        assertEquals("test-qr", result.qrCode)
        
        verify(exactly = 2) { repository.save(any()) }
        verify { mercadoPagoClient.createQrCodeOrder("123", BigDecimal.TEN) }
    }

    @Test
    fun `should process webhook successfully when status changes`() {
        val payload = MercadoPagoWebhookPayload(
            type = "order",
            action = "payment.created",
            data = MercadoPagoOrderData(id = "mp-123", orderId = "123", status = null)
        )
        
        val orderPayment = OrderPayment(orderId = "123", amount = BigDecimal.TEN, status = "created", mercadoPagoId = "mp-123")
        val infoMp = MercadoPagoOrderData(id = "mp-123", orderId = "123", status = "paid")

        every { mercadoPagoClient.fetchOrder("mp-123") } returns infoMp
        every { repository.findByMercadoPagoId("mp-123") } returns orderPayment
        every { repository.save(any()) } returns orderPayment
        every { publisher.notifyOrder(any(), any()) } returns Unit

        service.processWebhook(payload)

        assertEquals("paid", orderPayment.status)
        verify { repository.save(any()) }
        verify { publisher.notifyOrder("123", "paid") }
    }
}
