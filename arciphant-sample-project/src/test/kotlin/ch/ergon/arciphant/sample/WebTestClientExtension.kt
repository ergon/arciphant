package ch.ergon.arciphant.sample

import org.springframework.test.web.reactive.server.EntityExchangeResult

fun <B> EntityExchangeResult<B>.body() = responseBody!!
