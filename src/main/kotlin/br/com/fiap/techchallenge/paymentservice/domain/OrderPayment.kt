package br.com.fiap.techchallenge.paymentservice.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDateTime

@Document(collection = "payments")
data class OrderPayment(
    @Id val id: String? = null,
    val orderId: String,
    val amount: BigDecimal,
    var status: String,
    val mercadoPagoId: String? = null,
    val qrCode: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)