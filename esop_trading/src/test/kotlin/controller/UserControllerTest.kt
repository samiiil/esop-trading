package controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import models.DataStorage
import models.ErrorResponse
import models.RegisterInput
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


@MicronautTest
class UserControllerTest {


    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    private val mapper = ObjectMapper().registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    )

    private val registerURI = "/user/register"

    @AfterEach
    fun tearDown() {
        DataStorage.userList.clear()
        DataStorage.registeredEmails.clear()
        DataStorage.registeredPhoneNumbers.clear()
        DataStorage.buyList.clear()
        DataStorage.sellList.clear()
        DataStorage.performanceSellList.clear()
        DataStorage.orderId = 0
        DataStorage.orderExecutionId = 0
    }

    @Test
    fun shouldRegisterAValidUser() {
        val registerInput = RegisterInput(
            username = "amy_santiago",
            firstName = "Amy",
            lastName = "Santiago",
            phoneNumber = "1234567890",
            email = "amy@gmail.com"
        )
        val request = HttpRequest.POST(registerURI, registerInput)
        val responseString = client.toBlocking().retrieve(request)
        println(responseString)

        val typeRef: TypeReference<Map<String, String>> = object : TypeReference<Map<String, String>>() {}
        val response: Map<String, String> = mapper.readValue(responseString, typeRef)
        assertEquals("amy_santiago", response["username"])
        assertEquals("Amy", response["firstName"])
        assertEquals("Santiago", response["lastName"])
        assertEquals("1234567890", response["phoneNumber"])
        assertEquals("amy@gmail.com", response["email"])
    }

    @Test
    fun shouldThrowErrorsForInvalidUser() {
        val registerInput = RegisterInput()
        val request = HttpRequest.POST(registerURI, registerInput)
        val exception = assertThrows<HttpClientResponseException> {
            client.toBlocking().retrieve(request)
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.status)

        val errorResponse = mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(5, errorResponse.error.size)
        assertEquals("firstName is missing.", errorResponse.error[0])
        assertEquals("lastName is missing.", errorResponse.error[1])
        assertEquals("phoneNumber is missing.", errorResponse.error[2])
        assertEquals("email is missing.", errorResponse.error[3])
        assertEquals("userName is missing.", errorResponse.error[4])
    }
}