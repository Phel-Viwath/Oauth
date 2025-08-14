package vw.viwath.oauth.common

sealed class ResourceResult <out T> {
    data class Success<out T> (val data: T): ResourceResult<T>()
    data class Failure(val failureMsg: String): ResourceResult<Nothing>()
    data class Error(val errorMsg: String): ResourceResult<Nothing>()
}