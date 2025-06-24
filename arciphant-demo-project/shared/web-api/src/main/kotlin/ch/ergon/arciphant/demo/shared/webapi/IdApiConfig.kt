package ch.ergon.arciphant.demo.shared.webapi

import ch.ergon.arciphant.demo.shared.base.CustomerId
import ch.ergon.arciphant.demo.shared.base.OrderId
import org.springframework.context.annotation.Configuration

@Configuration
internal class CustomerIdApiType : UuidWebMapper<CustomerId>(CustomerId::class.java, { CustomerId(it) })

@Configuration
internal class OrderIdApiConfig : UuidWebMapper<OrderId>(OrderId::class.java, { OrderId(it) })
