package br.com.fiap.techchallenge.paymentservice.bdd

import br.com.fiap.techchallenge.paymentservice.client.MercadoPagoClient
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderConfigResponse
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderResponse
import io.cucumber.java.en.Given
import org.springframework.test.context.TestPropertySource
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.math.BigDecimal

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = [
    "spring.cloud.aws.parameterstore.enabled=false",
    "spring.cloud.aws.sns.enabled=false",
    "spring.cloud.aws.sqs.enabled=false",
    "cloud.aws.stack.auto=false",
    "cloud.aws.region.static=us-east-1",
    "cloud.aws.credentials.access-key=test",
    "cloud.aws.credentials.secret-key=test"
])
class StepDefinitions {

    @org.springframework.boot.test.mock.mockito.MockBean
    private lateinit var snsTemplate: io.awspring.cloud.sns.core.SnsTemplate

    @TestConfiguration
    class Config {
        @Bean
        @Primary
        fun mercadoPagoClient(): MercadoPagoClient = mockk(relaxed = true)
    }

    @Autowired
    private lateinit var mercadoPagoClient: MercadoPagoClient

    private var response: MercadoPagoOrderResponse? = null

    @Given("the external payment service is available")
    fun the_external_payment_service_is_available() {
        val mockResponse = MercadoPagoOrderResponse(
            id = "mp_123",
            status = "opened",
            externalReference = "ORDER-123",
            totalAmount = 150.00,
            config = MercadoPagoOrderConfigResponse(qrData = "valid_qr_code_data")
        )
        every { mercadoPagoClient.createQrCodeOrder(any(), any()) } returns mockResponse
    }

    @When("I request a QR Code for order {string} with amount {double}")
    fun i_request_a_qr_code_for_order_with_amount(orderId: String, amount: Double) {
        response = mercadoPagoClient.createQrCodeOrder(orderId, BigDecimal.valueOf(amount))
    }

    @Then("I should receive a successful payment response")
    fun i_should_receive_a_successful_payment_response() {
        assertNotNull(response)
        assertEquals("mp_123", response?.id)
    }

    @Then("the response should contain a valid QR Code data")
    fun the_response_should_contain_a_valid_qr_code_data() {
        assertNotNull(response?.config?.qrData)
        assertEquals("valid_qr_code_data", response?.config?.qrData)
    }
}
