package com.kampplus.hava.ui

sealed interface LoadState<out T> {

    data object Loading : LoadState<Nothing>

    data class Success<T>(val data: T) : LoadState<T>

    data object Empty : LoadState<Nothing>

    /** [message] kullanıcıya gösterilecek, insan dilinde yazılmış metindir. */
    data class Error(val message: String) : LoadState<Nothing>
}
