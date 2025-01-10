package ch.ergon.arciphant.sample.customer.api

import ch.ergon.arciphant.sample.shared.api.CustomerId

data class Customer(val id: CustomerId, val firstname: String, val lastname: String) {

    val name = "$firstname $lastname"

}
