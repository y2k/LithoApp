package y2k.example.litho.common

sealed class Result<out T, out E>
class Ok<out T>(val value: T) : Result<T, Nothing>()
class Error<out E>(val error: E) : Result<Nothing, E>()

fun <T, E> T?.toResult(defError: E): Result<T, E> =
    this?.let(::Ok) ?: Error(defError)

inline fun <T, E, R> Result<T, E>.map(f: (T) -> R): Result<R, E> =
    when (this) {
        is Ok -> Ok(f(value))
        is Error -> this
    }

inline fun <T, E, R> Result<T, E>.mapOption(f: (T) -> R?, defError: E): Result<R, E> =
    when (this) {
        is Ok -> f(value).toResult(defError)
        is Error -> this
    }

inline fun <T, E, R> Result<T, E>.bind(f: (T) -> Result<R, E>): Result<R, E> =
    when (this) {
        is Ok -> f(value)
        is Error -> this
    }