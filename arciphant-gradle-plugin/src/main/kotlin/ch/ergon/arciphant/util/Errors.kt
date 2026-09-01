package ch.ergon.arciphant.util

private const val ERROR_MSG_PREFIX = "Arciphant error:"

fun arciphantPrecondition(condition: Boolean, message: () -> String) {
    if (!condition) {
        arciphantPreconditionError(message())
    }
}

fun arciphantPreconditionError(message: String, originalException: Throwable? = null): Nothing {
    throw IllegalArgumentException("$ERROR_MSG_PREFIX $message", originalException)
}

fun arciphantError(message: String, e: Throwable? = null): Nothing {
    throw IllegalStateException("$ERROR_MSG_PREFIX $message", e)
}