package exception

import models.ErrorResponse

class BadRequestException(val errorResponse: ErrorResponse): Exception() {

}