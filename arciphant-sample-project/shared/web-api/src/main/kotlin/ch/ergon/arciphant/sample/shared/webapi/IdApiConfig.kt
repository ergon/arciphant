package ch.ergon.arciphant.sample.shared.webapi

import ch.ergon.arciphant.sample.shared.base.CustomerId
import ch.ergon.arciphant.sample.shared.base.OrderId
import org.springframework.context.annotation.Configuration

@Configuration
internal class CustomerIdApiType : UuidApiType<CustomerId>(CustomerId::class.java, { CustomerId(it) })

@Configuration
internal class OrderIdApiConfig : UuidApiType<OrderId>(OrderId::class.java, { OrderId(it) })
