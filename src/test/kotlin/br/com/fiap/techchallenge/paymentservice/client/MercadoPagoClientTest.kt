package br.com.fiap.techchallenge.paymentservice.client

import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderConfigResponse
import br.com.fiap.techchallenge.paymentservice.dto.MercadoPagoOrderResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import org.hamcrest.Matchers.*

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

class MercadoPagoClientTest {

    private lateinit var mercadoPagoClient: MercadoPagoClient
    private lateinit var mockServer: MockRestServiceServer
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder()
            .messageConverters { converters ->
                converters.clear()
                converters.add(MappingJackson2HttpMessageConverter(objectMapper))
            }

        // Create mock server BEFORE creating the client
        val mockServerBuilder = MockRestServiceServer.bindTo(builder)
        mockServer = mockServerBuilder.build()

        // Now create the client with the mocked builder
        mercadoPagoClient = MercadoPagoClient("test_token", builder)
    }

    @Test
    fun `createQrCodeOrder should return order response when successful`() {
        val orderId = "123"
        val amount = BigDecimal("100.00")
        val expectedResponse = MercadoPagoOrderResponse(
            id = "mp_123",
            status = "opened",
            externalReference = orderId,
            totalAmount = amount.toDouble(),
            config = MercadoPagoOrderConfigResponse(qrData = "qr_data_string")
        )

        mockServer.expect(requestTo("https://api.mercadopago.com/v1/orders"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer test_token"))
            .andExpect(header("X-Idempotency-Key", notNullValue()))
            .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON))

        val result = mercadoPagoClient.createQrCodeOrder(orderId, amount)

        assertNotNull(result)
        assertEquals(expectedResponse.id, result?.id)
        assertEquals(expectedResponse.config?.qrData, result?.config?.qrData)
        mockServer.verify()
    }

    @Test
    fun `createQrCodeOrder should return null when API call fails`() {
        val orderId = "123"
        val amount = BigDecimal("100.00")

        mockServer.expect(requestTo("https://api.mercadopago.com/v1/orders"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer test_token"))
            .andRespond(withUnauthorizedRequest().body("{\"code\":\"unauthorized\",\"message\":\"authorization value not present\"}"))

        val result = mercadoPagoClient.createQrCodeOrder(orderId, amount)

        assertNull(result)
        mockServer.verify()
    }
}
