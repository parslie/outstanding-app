package com.tavro.parslie.outstanding.util

data class Resource<T>(
    val status: Status,
    val data: T?  // TODO: implement as non-nullable instead
)