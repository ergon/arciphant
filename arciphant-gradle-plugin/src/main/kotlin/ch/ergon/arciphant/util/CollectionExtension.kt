package ch.ergon.arciphant.util

fun <K, V> List<Map<K, V>>.merge(): Map<K, V> {
    return if(isNotEmpty()) reduce { mergedMap, map -> mergedMap + map } else emptyMap()
}
