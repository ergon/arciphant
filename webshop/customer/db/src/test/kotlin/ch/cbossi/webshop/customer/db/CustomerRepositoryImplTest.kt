package ch.cbossi.webshop.customer.db

class CustomerRepositoryImplTest {

    private val repository = CustomerRepositoryImpl()

    fun test() {
        repository.loadCustomer()
    }

}