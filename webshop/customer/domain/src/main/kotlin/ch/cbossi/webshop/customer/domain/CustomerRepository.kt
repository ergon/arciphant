package ch.cbossi.webshop.customer.domain

import ch.cbossi.webshop.customer.api.Customer

interface CustomerRepository {

    fun loadCustomer(): Customer

}