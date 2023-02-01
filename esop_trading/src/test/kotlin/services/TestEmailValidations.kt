package services
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class TestEmailValidations {
    @Test
    fun `should not accept email without domain`(){
        //Act
          val errorList=Validations.validateEmailIds("plaintextaddress")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should have atleast one character in username part `(){
        //Act
        val errorList=Validations.validateEmailIds("@#@@##@%^%#\$@#\$@#.com\n")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail without username`(){
        //Act
        val errorList=Validations.validateEmailIds("@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept special characters in domain`(){
        //Act
        val errorList=Validations.validateEmailIds("John Doe <example@email.com>")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail without domain`(){
        //Act
        val errorList=Validations.validateEmailIds("example.email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail with @ in username`(){
        //Act
        val errorList=Validations.validateEmailIds("example@example@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }
    @Test
    fun `should not accept mail starting with dot`(){
        //Act
        val errorList=Validations.validateEmailIds(".example@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail ending with dot`(){
        //Act
        val errorList=Validations.validateEmailIds("example.@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail with continuous dot`(){
        //Act
        val errorList=Validations.validateEmailIds("example…example@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail with non-english characters`(){
        //Act
        val errorList=Validations.validateEmailIds("おえあいう@example.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail with special characters in domain name`(){
        //Act
        val errorList=Validations.validateEmailIds("example@email.com (John Doe)")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail without subdomain`(){
        //Act
        val errorList=Validations.validateEmailIds("example@email")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not start with hyphens`(){
        //Act
        val errorList=Validations.validateEmailIds("example@-email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail with continuous dots in domain`(){
        //Act
        val errorList=Validations.validateEmailIds("example@email…com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }
    @Test
    fun `should not accept mail with continuous dots`(){
        //Act
        val errorList=Validations.validateEmailIds("CAT…123@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

    @Test
    fun `should not accept mail with special characters without quotes`(){
        //Act
        val errorList=Validations.validateEmailIds("\"(),:;<>[\\]@email.com")
        //Assert
        assertEquals("Invalid Email address",errorList.elementAt(0))
    }

}