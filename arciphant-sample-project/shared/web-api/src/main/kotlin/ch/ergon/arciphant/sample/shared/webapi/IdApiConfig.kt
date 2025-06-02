package ch.ergon.arciphant.sample.shared.webapi

import ch.ergon.arciphant.sample.shared.base.CustomerId
import ch.ergon.arciphant.sample.shared.base.OrderId
import org.springframework.context.annotation.Configuration

@Configuration
internal class CustomerIdApiType : UuidWebMapper<CustomerId>(CustomerId::class.java, { CustomerId(it) })

@Configuration
internal class OrderIdApiConfig : UuidWebMapper<OrderId>(OrderId::class.java, { OrderId(it) })
