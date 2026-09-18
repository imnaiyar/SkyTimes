package com.imnaiyar.skytimes.core.data

import kotlinx.datetime.LocalDate

object SkyDateHelper {
    const val skyTimeZone = "America/Los_Angeles"
    fun fromStringSky(value: String): LocalDate = LocalDate.parse(value)
}

object NodeHelper {
    fun find(node: Node?, predicate: (Node) -> Boolean): Node? {
        if (node == null) return null
        if (predicate(node)) return node
        return find(node.nw, predicate) ?: find(node.ne, predicate) ?: find(node.n, predicate)
    }

    fun all(node: Node?, nodes: MutableList<Node> = mutableListOf()): List<Node> {
        if (node == null || node in nodes) return nodes
        nodes += node
        all(node.nw, nodes); all(node.ne, nodes); all(node.n, nodes)
        return nodes
    }

    fun trace(node: Node?): List<Node> = buildList {
        var current = node
        while (current != null) { add(current); current = current.prev }
        reverse()
    }

    fun traceMany(nodes: List<Node>): Set<Node> = buildSet {
        nodes.forEach { node ->
            var current: Node? = node
            while (current != null && add(current)) current = current.prev
        }
    }

    fun getItems(node: Node?, includeHidden: Boolean = false): List<Item> =
        all(node).flatMap { buildList { it.item?.let(::add); if (includeHidden) addAll(it.hiddenItems) } }.distinct()
}

object SpiritTreeHelper {
    fun getNodes(tree: SpiritTree?): List<Node> = tree?.node?.let(NodeHelper::all) ?: tree?.tier?.let { tier ->
        generateSequence(tier) { it.next }
            .flatMap { it.rows.asSequence().flatMap { row -> row.asSequence().filterNotNull() } }
            .toList()
    } ?: emptyList()

    fun getTiers(tree: SpiritTree?): List<SpiritTreeTier> =
        generateSequence(tree?.tier) { it.next }.toList()

    fun getItems(tree: SpiritTree?, includeHidden: Boolean = false): List<Item> =
        getNodes(tree).flatMap { NodeHelper.getItems(it, includeHidden) }.distinct()
}
