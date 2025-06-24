package ch.ergon.arciphant.demo.customer.api

import ch.ergon.arciphant.demo.shared.base.CustomerId

data class Customer(val id: CustomerId, val firstname: String, val lastname: String) {

    val name = "$firstname $lastname"

}
