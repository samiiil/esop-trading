package Services
import models.DataStorage
import models.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import services.Validations

class TestEmailValidations {
    @AfterEach
    fun tearDown(){
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
    fun `check if email is not valid`(){
        //Arrange
        val user = User(firstName = "user", lastName = "user", emailId = "plaintextaddress", phoneNumber = "+911234567890", username = "user")
        val user1 = User(firstName = "user1", lastName = "user1", emailId = "@#@@##@%^%#\$@#\$@#.com", phoneNumber = "+911234567891", username = "user1")
        val user2 = User(firstName = "user2", lastName = "user2", emailId = "@email.com", phoneNumber = "+911234567892", username = "user2")
        val user3 = User(firstName = "user3", lastName = "user3", emailId = "John Doe <example@email.com>\n", phoneNumber = "+911234567893", username = "user3")
        val user4 = User(firstName = "user4", lastName = "user4", emailId = "example.email.com", phoneNumber = "+911234567894", username = "user4")
        val user5 = User(firstName = "user5", lastName = "user5", emailId = "example@example@email.com", phoneNumber = "+911234567895", username = "user5")
        val user6 = User(firstName = "user6", lastName = "user6", emailId = ".example@email.com", phoneNumber = "+911234567896", username = "user6")
        val user7 = User(firstName = "user7", lastName = "user7", emailId = "example.@email.com", phoneNumber = "+911234567897", username = "user7")
        val user8 = User(firstName = "user8", lastName = "user8", emailId = "example…example@email.com", phoneNumber = "+911234567898", username = "user8")
        val user9 = User(firstName = "user9", lastName = "user9", emailId = "おえあいう@example.com", phoneNumber = "+911234567899", username = "user9")
        val user10 = User(firstName = "user10", lastName = "user10", emailId = "example@email.com (John Doe)", phoneNumber = "+911234567899", username = "user10")
        val user11 = User(firstName = "user11", lastName = "user11", emailId = "example@email", phoneNumber = "+911234567801", username = "user11")
        val user12 = User(firstName = "user12", lastName = "user12", emailId = "example@-email.com", phoneNumber = "+911234567802", username = "user12")
        //Act
        val errorList=Validations.validateEmailIds(user.emailId )
        val errorList1= Validations.validateEmailIds(user1.emailId)
        val errorList2=Validations.validateEmailIds(user2.emailId)
        val errorList3=Validations.validateEmailIds(user3.emailId)
        val errorList4=Validations.validateEmailIds(user4.emailId)
        val errorList5=Validations.validateEmailIds(user5.emailId)
        val errorList6=Validations.validateEmailIds(user6.emailId)
        val errorList7=Validations.validateEmailIds(user7.emailId)
        val errorList8=Validations.validateEmailIds(user8.emailId)
        val errorList9=Validations.validateEmailIds(user9.emailId)
        val errorList10=Validations.validateEmailIds(user10.emailId)
        val errorList11=Validations.validateEmailIds(user11.emailId)
        val errorList12=Validations.validateEmailIds(user12.emailId)

        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(1))
        assertEquals("Invalid Email address",errorList1.elementAt(1))
        assertEquals("Invalid Email address",errorList2.elementAt(1))
        assertEquals("Invalid Email address",errorList3.elementAt(1))
        assertEquals("Invalid Email address",errorList4.elementAt(1))
        assertEquals("Invalid Email address",errorList5.elementAt(1))
        assertEquals("Invalid Email address",errorList6.elementAt(1))
        assertEquals("Invalid Email address",errorList7.elementAt(1))
        assertEquals("Invalid Email address",errorList8.elementAt(1))
        assertEquals("Invalid Email address",errorList9.elementAt(1))
        assertEquals("Invalid Email address",errorList10.elementAt(1))
        assertEquals("Invalid Email address",errorList11.elementAt(1))
        assertEquals("Invalid Email address",errorList12.elementAt(1))

    }

}