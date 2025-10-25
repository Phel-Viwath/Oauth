package vw.viwath.oauth.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class ApiResponse <T>(
    val success: Boolean,
    val message: String,
    val code: Int,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
){
    companion object{
        // Success responses
        fun <T> success(data: T? = null, message: String = "Success!"): ApiResponse<T>{
            return ApiResponse(
                success = true,
                message = message,
                code = 200,
                data = data
            )
        }

        fun <T> created(data: T? = null, message: String = "Created"): ApiResponse<T>{
            return ApiResponse(
                success = true,
                message = message,
                code = 201,
                data = data
            )
        }

        // Client error responses
        fun <T> badRequest(message: String = "Bad request!"): ApiResponse<T>{
            return ApiResponse(
                success = false,
                message = message,
                code = 400,
                data = null
            )
        }

        fun <T> conflict(message: String = "Resource conflict."): ApiResponse<T>{
            return ApiResponse(
                success = false,
                message = message,
                code = 409,
                data = null
            )
        }

        fun <T> notFound(message: String = "Resource not found."): ApiResponse<T>{
            return ApiResponse(
                success = false,
                message = message,
                code = 404,
                data = null
            )
        }

        // Server error responses
        fun <T> internalServerError(message: String = "Internal server error"): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                code = 500,
                data = null
            )
        }

        // Generic error response
        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                code = code,
                data = null
            )
        }
    }

}

fun <T> ApiResponse<T>.toResponseEntity(): ResponseEntity<ApiResponse<T>>{
    val httpStatus = when(this.code){
        200 -> HttpStatus.OK
        201 -> HttpStatus.CREATED
        400 -> HttpStatus.BAD_REQUEST
        404 -> HttpStatus.NOT_FOUND
        409 -> HttpStatus.CONFLICT
        500 -> HttpStatus.INTERNAL_SERVER_ERROR
        else -> HttpStatus.valueOf(this.code)
    }
    return ResponseEntity.status(httpStatus).body(this)
}
