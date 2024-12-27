package ch.cbossi.webshop.customer.api

import ch.cbossi.webshop.shared.api.CustomerId

data class Customer(val id: CustomerId, val firstname: String, val lastname: String) {

    val name = "$firstname $lastname"

}
