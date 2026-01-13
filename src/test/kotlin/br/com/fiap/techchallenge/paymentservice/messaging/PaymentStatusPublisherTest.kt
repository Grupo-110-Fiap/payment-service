package br.com.fiap.techchallenge.paymentservice.messaging

import io.awspring.cloud.sns.core.SnsTemplate
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PaymentStatusPublisherTest {

    @MockK(relaxed = true)
    private lateinit var snsTemplate: SnsTemplate

    private lateinit var paymentStatusPublisher: PaymentStatusPublisher
    private val topicArn = "arn:aws:sns:us-east-1:123456789012:test-topic"

    @BeforeEach
    fun setUp() {
        paymentStatusPublisher = PaymentStatusPublisher(snsTemplate, topicArn)
    }

    @Test
    fun `notifyOrder should send message to SNS topic`() {
        val orderId = "123"
        val status = "APPROVED"
        val expectedMessage = mapOf("orderId" to orderId, "status" to status)

        paymentStatusPublisher.notifyOrder(orderId, status)

        verify(exactly = 1) { snsTemplate.convertAndSend(topicArn, expectedMessage) }
        confirmVerified(snsTemplate)
    }
}
