package Models

data class RegisterInput(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val username: String,
)