package ch.ergon.arciphant.core

import ch.ergon.arciphant.util.verify
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty0

enum class ComponentLayout {
    PROJECT,
    SOURCE_SET;

    internal fun verifyConfig(value: Any?, dslFn: KFunction<*>) =
        verifyConfig(value, dslFn.name)

    internal fun <D> verifyConfig(dslProperty: KProperty0<D>) =
        verifyConfig(dslProperty.getter.call(), dslProperty.name)

    internal fun verifyConfig(value: Any?, configName: String) {
        verify(value == null) { "'$configName' cannot be configured for component layout '$this'" }
    }
}
