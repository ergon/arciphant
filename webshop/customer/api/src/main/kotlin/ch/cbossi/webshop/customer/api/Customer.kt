package ch.cbossi.webshop.customer.api

data class Customer(val firstname: String, val lastname: String) {

    val name = "$firstname $lastname"

}
