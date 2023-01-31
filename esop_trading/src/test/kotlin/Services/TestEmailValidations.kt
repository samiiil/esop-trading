package Services

import models.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import services.Validations

class TestEmailValidations {
    @Test
    fun `check if email is not valid`{
        //Arrange
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        //Act
        val errorList=Validations.validateEmailIds(user.emailId )
        //Assert
        assertEquals("Invalid email address",errorList.)

    }

}