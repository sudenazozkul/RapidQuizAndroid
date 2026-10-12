package com.rapidquiz.data.repo

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

data class ApiError(
    val kind: Kind,
    /** Backend'in `error.code` değeri (örn. `question_mismatch`); ağ hatalarında null. */
    val code: String? = null,
    val message: String,
    val httpStatus: Int? = null,
) {
    enum class Kind { Network, Http, Unknown }
}
