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
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentServiceTest {

    private val repository = mockk<OrderPaymentRepository>()
    private val mercadoPagoClient = mockk<MercadoPagoClient>()
    private val publisher = mockk<PaymentStatusPublisher>()
    private val service = PaymentService(repository, mercadoPagoClient, publisher)

    @Test
    fun `createPayment should persist and return QR data`() {
        val request = OrderPaymentRequest(orderId = "order-123", amount = BigDecimal("10.50"))

        every { repository.save(any()) } answers { firstArg() }
        every { mercadoPagoClient.createQrCodeOrder(request.orderId, request.amount) } returns
                MercadoPagoOrderResponse(
                    id = "mp-1",
                    status = "created",
                    externalReference = request.orderId,
                    totalAmount = 10.50,
                    typeResponse = MercadoPagoOrderConfigResponse(qrData = "qr-data")
                )

        val payment = service.createPayment(request)

        assertEquals("order-123", payment.orderId)
        assertEquals("mp-1", payment.mercadoPagoId)
        assertEquals("qr-data", payment.qrCode)
        verify(exactly = 2) { repository.save(any()) } // pré e pós resposta do MP
    }

    @Test
    fun `createPayment should fail when MP does not return a response`() {
        val request = OrderPaymentRequest(orderId = "order-404", amount = BigDecimal("20.00"))

        every { repository.save(any()) } answers { firstArg() }
        every { mercadoPagoClient.createQrCodeOrder(any(), any()) } returns null

        assertThrows(RuntimeException::class.java) {
            service.createPayment(request)
        }
    }

    @Test
    @DisplayName("Given valid webhook When status changes Then update and publish - BDD")
    fun `atualiza status e publica quando webhook muda estado`() {
        val payload = MercadoPagoWebhookPayload(
            action = "status.updated",
            type = "order",
            data = MercadoPagoOrderData(id = "mp-1", orderId = "order-123", status = null)
        )

        val existing = OrderPayment(
            id = "db-1",
            orderId = "order-123",
            amount = BigDecimal("15.00"),
            status = "created",
            mercadoPagoId = "mp-1",
            qrCode = "qr-old",
            createdAt = LocalDateTime.now().minusMinutes(10),
            updatedAt = LocalDateTime.now().minusMinutes(10)
        )

        val mpData = MercadoPagoOrderData(
            id = "mp-1",
            orderId = "order-123",
            status = "paid"
        )

        every { mercadoPagoClient.fetchOrder("mp-1") } returns mpData
        every { repository.findByMercadoPagoId("mp-1") } returns existing
        every { repository.save(any()) } answers { firstArg() }
        justRun { publisher.notifyOrder(any(), any()) }

        service.processWebhook(payload)

        assertEquals("paid", existing.status)
        assertNotNull(existing.updatedAt)
        verify { publisher.notifyOrder("order-123", "paid") }
        verify { repository.save(existing) }
    }

    @Test
    fun `ignore webhook when status does not change`() {
        val payload = MercadoPagoWebhookPayload(
            action = "status.updated",
            type = "order",
            data = MercadoPagoOrderData(id = "mp-1", orderId = "order-123", status = null)
        )

        val existing = OrderPayment(
            id = "db-1",
            orderId = "order-123",
            amount = BigDecimal("15.00"),
            status = "paid",
            mercadoPagoId = "mp-1",
            qrCode = "qr-old",
            createdAt = LocalDateTime.now().minusMinutes(10),
            updatedAt = LocalDateTime.now().minusMinutes(10)
        )

        val mpData = MercadoPagoOrderData(
            id = "mp-1",
            orderId = "order-123",
            status = "paid"
        )

        every { mercadoPagoClient.fetchOrder("mp-1") } returns mpData
        every { repository.findByMercadoPagoId("mp-1") } returns existing

        service.processWebhook(payload)

        verify(exactly = 0) { publisher.notifyOrder(any(), any()) }
        verify(exactly = 0) { repository.save(any()) }
    }
}