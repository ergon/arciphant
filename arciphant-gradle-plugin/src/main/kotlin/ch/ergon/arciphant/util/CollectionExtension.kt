package ch.ergon.arciphant.util

fun <K, V> List<Map<K, V>>.merge(): Map<K, V> {
    return reduce { mergedMap, map -> mergedMap + map }
}
