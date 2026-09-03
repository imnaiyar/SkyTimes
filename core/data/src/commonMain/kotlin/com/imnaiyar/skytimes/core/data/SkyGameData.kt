package com.imnaiyar.skytimes.core.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface IGuid {
    val guid: String
}

interface IPeriod {
    val date: LocalDate;
    val endDate: LocalDate
}

interface IWiki {
    val href: String?
}

data class Wiki(override val href: String?) : IWiki
data class MapData(
    val position: List<Double>? = null,
    val zoom: Double? = null,
    val boundary: List<List<Double>>? = null,
    val boundaryLabelAlign: String? = null,
    val boundaryColor: String? = null,
    val videoUrl: String? = null,
)

data class Cost(
    val c: Int? = null, val h: Int? = null, val sc: Int? = null,
    val sh: Int? = null, val ac: Int? = null, val ec: Int? = null,
)

open class SkyEntity(override val guid: String) : IGuid
class Area(
    guid: String,
    val name: String,
    val imageUrl: String?,
    val imagePosition: String?,
    val mapData: MapData?
) : SkyEntity(guid) {
    var realm: Realm? = null;
    val spirits = mutableListOf<Spirit>();
    val wingedLights = mutableListOf<WingedLight>()
    val specialVisits = mutableListOf<SpecialVisit>();
    val connections = mutableListOf<Area>();
    val mapShrines = mutableListOf<MapShrine>()
}

class Realm(
    guid: String,
    val name: String,
    val shortName: String,
    val imageUrl: String?,
    val imagePosition: String?,
    val hidden: Boolean?,
    val mapData: MapData?
) : SkyEntity(guid) {
    val areas = mutableListOf<Area>();
    var constellation: RealmConstellation? = null;
    var elder: Spirit? = null
}

class RealmConstellation(
    guid: String,
    val imageUrl: String,
    val icons: MutableList<RealmConstellationIcon>
) : SkyEntity(guid)

class RealmConstellationIcon(
    val imageUrl: String,
    val position: List<Double>,
    val flag: Boolean?,
    var spirit: Spirit?
)

class WingedLight(
    guid: String,
    val order: Int,
    val name: String?,
    val description: String?,
    val mapData: MapData?
) : SkyEntity(guid) {
    var area: Area? = null;
    var unlocked = false
}

class MapShrine(
    guid: String,
    val description: String?,
    val imageUrl: String?,
    val mapData: MapData?
) : SkyEntity(guid) {
    var area: Area? = null
}

class Season(
    guid: String,
    val name: String,
    val shortName: String,
    override var date: LocalDate,
    override var endDate: LocalDate,
    val year: Int,
    val iconUrl: String?,
    val imageUrl: String?,
    val imagePosition: String?,
    val draft: Boolean?
) : SkyEntity(guid), IPeriod {
    var number = 0;
    val spirits = mutableListOf<Spirit>();
    val shops = mutableListOf<Shop>();
    val includedTrees = mutableListOf<SpiritTree>()
}

class Event(
    guid: String,
    val name: String,
    val shortName: String?,
    val imageUrl: String?,
    val imagePosition: String?,
    val recurring: Boolean?
) : SkyEntity(guid) {
    val instances = mutableListOf<EventInstance>()
}

class EventInstance(
    guid: String,
    val name: String?,
    val shortName: String?,
    override var date: LocalDate,
    override var endDate: LocalDate,
    val draft: Boolean?
) : SkyEntity(guid), IPeriod {
    var number = 0;
    var event: Event? = null;
    val shops = mutableListOf<Shop>();
    val spirits = mutableListOf<EventInstanceSpirit>()
}

class EventInstanceSpirit(guid: String, val name: String?) : SkyEntity(guid) {
    var spirit: Spirit? = null;
    var tree: SpiritTree? = null;
    var eventInstance: EventInstance? = null
}

class SpecialVisit(
    guid: String,
    val name: String?,
    override var date: LocalDate,
    override var endDate: LocalDate,
    val draft: Boolean?
) : SkyEntity(guid), IPeriod {
    var area: Area? = null;
    val spirits = mutableListOf<SpecialVisitSpirit>()
}

class SpecialVisitSpirit(guid: String) : SkyEntity(guid) {
    var visit: SpecialVisit? = null;
    var spirit: Spirit? = null;
    var tree: SpiritTree? = null
}

class TravelingSpirit(
    guid: String,
    override var date: LocalDate,
    override var endDate: LocalDate,
    val visit: Int
) : SkyEntity(guid), IPeriod {
    var number = 0;
    var spirit: Spirit? = null;
    var tree: SpiritTree? = null
}

enum class ItemType { HairAccessory, HeadAccessory, Hair, Mask, FaceAccessory, Necklace, Outfit, Shoes, OutfitShoes, Cape, Held, Furniture, Prop, Emote, Stance, Call, Spell, Music, Quest, WingBuff, Special }
class Item(
    guid: String,
    val id: Int?,
    val type: String,
    val subtype: String?,
    val group: String?,
    val name: String,
    val icon: String?,
    val previewUrl: String?,
    var order: Int?,
    val level: Int?,
    val sheet: String?,
    val quantity: Int?,
    val closetHide: Boolean?
) : SkyEntity(guid) {
    val nodes = mutableListOf<Node>();
    val hiddenNodes = mutableListOf<Node>();
    val listNodes = mutableListOf<ItemListNode>();
    val iaps = mutableListOf<Iap>()
    var season: Season? = null;
    var unlocked = false;
    var autoUnlocked = false;
    var favourited = false
}

class Iap(
    guid: String,
    val price: Double?,
    val name: String?,
    val returning: Boolean?,
    val c: Int?,
    val sc: Int?,
    val sp: Int?
) : SkyEntity(guid) {
    val items = mutableListOf<Item>();
    var shop: Shop? = null;
    var bought = false;
    var gifted = false
}

class ItemList(guid: String, val description: String?) : SkyEntity(guid) {
    val items = mutableListOf<ItemListNode>();
    var shop: Shop? = null
}

class ItemListNode(guid: String, val cost: Cost, val quantity: Int?) : SkyEntity(guid) {
    var item: Item? = null;
    var itemList: ItemList? = null;
    var unlocked = false
}

class Shop(guid: String, val type: String, val name: String?, val permanent: String?) :
    SkyEntity(guid) {
    val iaps = mutableListOf<Iap>();
    var itemList: ItemList? = null;
    var event: EventInstance? = null;
    var spirit: Spirit? = null;
    var season: Season? = null
}

class Node(guid: String, val cost: Cost) : SkyEntity(guid) {
    var item: Item? = null;
    val hiddenItems = mutableListOf<Item>();
    var tree: SpiritTree? = null;
    var nw: Node? = null;
    var ne: Node? = null;
    var n: Node? = null;
    var prev: Node? = null;
    var root: Node? = null;
    var unlocked = false
}

class SpiritTree(guid: String, val name: String?, val permanent: String?, val draft: Boolean?) :
    SkyEntity(guid) {
    var node: Node? = null;
    var tier: SpiritTreeTier? = null;
    var spirit: Spirit? = null;
    var travelingSpirit: TravelingSpirit? = null;
    var specialVisitSpirit: SpecialVisitSpirit? = null;
    var eventInstanceSpirit: EventInstanceSpirit? = null
}

class SpiritTreeTier(guid: String, val rows: MutableList<MutableList<Node?>>) : SkyEntity(guid) {
    var tree: SpiritTree? = null;
    var prev: SpiritTreeTier? = null;
    var next: SpiritTreeTier? = null;
    var root: SpiritTreeTier? = null
}

class Spirit(guid: String, val name: String, val type: String, val imageUrl: String?) :
    SkyEntity(guid) {
    var tree: SpiritTree? = null;
    val treeRevisions = mutableListOf<SpiritTree>();
    var area: Area? = null;
    var season: Season? = null;
    val travelingSpirits = mutableListOf<TravelingSpirit>();
    val specialVisitSpirits = mutableListOf<SpecialVisitSpirit>();
    val eventInstanceSpirits = mutableListOf<EventInstanceSpirit>();
    val shops = mutableListOf<Shop>();
    var relived = false;
    var index = 0
}

data class CalculatorData(
    val dailyCurrencyAmount: Int? = null,
    val timedCurrency: List<TimedCurrency> = emptyList()
)

data class TimedCurrency(
    override val guid: String,
    val description: String?,
    override val date: LocalDate,
    override val endDate: LocalDate,
    val amount: Int
) : IGuid, IPeriod

data class DataConfig<T>(val items: List<T>)
data class SkyData(
    val realms: DataConfig<Realm>,
    val areas: DataConfig<Area>,
    val constellations: DataConfig<RealmConstellation>,
    val wingedLights: DataConfig<WingedLight>,
    val mapShrines: DataConfig<MapShrine>,
    val seasons: DataConfig<Season>,
    val events: DataConfig<Event>,
    val eventInstances: DataConfig<EventInstance>,
    val eventInstanceSpirits: DataConfig<EventInstanceSpirit>,
    val spirits: DataConfig<Spirit>,
    val spiritTrees: DataConfig<SpiritTree>,
    val spiritTreeTiers: DataConfig<SpiritTreeTier>,
    val nodes: DataConfig<Node>,
    val travelingSpirits: DataConfig<TravelingSpirit>,
    val specialVisits: DataConfig<SpecialVisit>,
    val specialVisitSpirits: DataConfig<SpecialVisitSpirit>,
    val items: DataConfig<Item>,
    val itemLists: DataConfig<ItemList>,
    val shops: DataConfig<Shop>,
    val iaps: DataConfig<Iap>,
    val candles: DataConfig<SkyEntity> = DataConfig(emptyList()),
) {
    val guids: Map<String, IGuid> by lazy { all.associateBy { it.guid } }
    val itemIds: Map<Int, Item> by lazy {
        items.items.mapNotNull { it.id?.let { id -> id to it } }.toMap()
    }
    val all: List<SkyEntity>
        get() = listOf(
            realms.items,
            areas.items,
            constellations.items,
            wingedLights.items,
            mapShrines.items,
            seasons.items,
            events.items,
            eventInstances.items,
            eventInstanceSpirits.items,
            spirits.items,
            spiritTrees.items,
            spiritTreeTiers.items,
            nodes.items,
            travelingSpirits.items,
            specialVisits.items,
            specialVisitSpirits.items,
            items.items,
            itemLists.items,
            shops.items,
            iaps.items,
            candles.items
        ).flatten()
}

private fun JsonObject.s(key: String) = this[key]?.jsonPrimitive?.contentOrNull
private fun JsonObject.i(key: String) = this[key]?.jsonPrimitive?.intOrNull
private fun JsonObject.b(key: String) = this[key]?.jsonPrimitive?.booleanOrNull
private fun JsonObject.d(key: String) = this[key]?.jsonPrimitive?.doubleOrNull
private fun JsonObject.ref(key: String) = s(key)
private fun JsonObject.refs(key: String) =
    this[key]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

private fun JsonObject.date(key: String) = s(key)?.let { LocalDate.parse(it) }
private fun JsonObject.mapData(key: String): MapData? = this[key]?.jsonObject?.let { o ->
    MapData(
        o["position"]?.jsonArray?.mapNotNull { it.jsonPrimitive.doubleOrNull },
        o.d("zoom"),
        o["boundary"]?.jsonArray?.map { it.jsonArray.mapNotNull { n -> n.jsonPrimitive.doubleOrNull } },
        o.s("boundaryLabelAlign"),
        o.s("boundaryColor"),
        o.s("videoUrl")
    )
}

private fun JsonObject.config(key: String) =
    this[key]?.jsonObject?.get("items")?.jsonArray ?: JsonArray(emptyList())

class SkyDataResolver {
    companion object {
        fun resolve(json: String): SkyData = SkyDataResolver().resolve(json)
    }

    fun resolve(json: String): SkyData = resolve(Json.parseToJsonElement(json).jsonObject)
    fun resolve(root: JsonObject): SkyData {
        fun <T> arr(key: String, factory: (JsonObject) -> T) =
            root.config(key).map { factory(it.jsonObject) }

        val realms = arr("realms") { o ->
            Realm(
                o.s("guid")!!,
                o.s("name") ?: "",
                o.s("shortName") ?: "",
                o.s("imageUrl"),
                o.s("imagePosition"),
                o.b("hidden"),
                o.mapData("mapData")
            )
        }
        val areas = arr("areas") { o ->
            Area(
                o.s("guid")!!,
                o.s("name") ?: "",
                o.s("imageUrl"),
                o.s("imagePosition"),
                o.mapData("mapData")
            )
        }
        val constellations = arr("constellations") { o ->
            RealmConstellation(
                o.s("guid")!!,
                o.s("imageUrl") ?: "",
                mutableListOf()
            )
        }
        val winged = arr("wingedLights") { o ->
            WingedLight(
                o.s("guid")!!,
                o.i("order") ?: 0,
                o.s("name"),
                o.s("description"),
                o.mapData("mapData")
            )
        }
        val shrines = arr("mapShrines") { o ->
            MapShrine(
                o.s("guid")!!,
                o.s("description"),
                o.s("imageUrl"),
                o.mapData("mapData")
            )
        }
        val seasons = arr("seasons") { o ->
            Season(
                o.s("guid")!!,
                o.s("name") ?: "",
                o.s("shortName") ?: "",
                o.date("date") ?: LocalDate(1970, 1, 1),
                o.date("endDate") ?: LocalDate(1970, 1, 1),
                o.i("year") ?: 0,
                o.s("iconUrl"),
                o.s("imageUrl"),
                o.s("imagePosition"),
                o.b("draft")
            )
        }
        val events = arr("events") { o ->
            Event(
                o.s("guid")!!,
                o.s("name") ?: "",
                o.s("shortName"),
                o.s("imageUrl"),
                o.s("imagePosition"),
                o.b("recurring")
            )
        }
        val eventInstances = arr("eventInstances") { o ->
            EventInstance(
                o.s("guid")!!,
                o.s("name"),
                o.s("shortName"),
                o.date("date") ?: LocalDate(1970, 1, 1),
                o.date("endDate") ?: o.date("date") ?: LocalDate(1970, 1, 1),
                o.b("draft")
            )
        }
        val eventSpirits =
            arr("eventInstanceSpirits") { o -> EventInstanceSpirit(o.s("guid")!!, o.s("name")) }
        val spirits = arr("spirits") { o ->
            Spirit(
                o.s("guid")!!,
                o.s("name") ?: "",
                o.s("type") ?: "Regular",
                o.s("imageUrl")
            )
        }
        val trees = arr("spiritTrees") { o ->
            SpiritTree(
                o.s("guid")!!,
                o.s("name"),
                o.s("permanent"),
                o.b("draft")
            )
        }
        val tiers = arr("spiritTreeTiers") { o ->
            SpiritTreeTier(
                o.s("guid")!!,
                o["rows"]?.jsonArray?.map { row ->
                    row.jsonArray.map { null as Node? }.toMutableList()
                }?.toMutableList() ?: mutableListOf<MutableList<Node?>>()
            )
        }
        val nodes = arr("nodes") { o ->
            Node(
                o.s("guid")!!,
                Cost(o.i("c"), o.i("h"), o.i("sc"), o.i("sh"), o.i("ac"), o.i("ec"))
            )
        }
        val traveling = arr("travelingSpirits") { o ->
            TravelingSpirit(
                o.s("guid")!!,
                o.date("date") ?: LocalDate(1970, 1, 1),
                o.date("endDate") ?: o.date("date") ?: LocalDate(1970, 1, 1),
                o.i("visit") ?: 0
            )
        }
        val visits = arr("specialVisits") { o ->
            SpecialVisit(
                o.s("guid")!!,
                o.s("name"),
                o.date("date") ?: LocalDate(1970, 1, 1),
                o.date("endDate") ?: o.date("date") ?: LocalDate(1970, 1, 1),
                o.b("draft")
            )
        }
        val visitSpirits = arr("specialVisitSpirits") { o -> SpecialVisitSpirit(o.s("guid")!!) }
        val items = arr("items") { o ->
            Item(
                o.s("guid")!!,
                o.i("id"),
                o.s("type") ?: "Special",
                o.s("subtype"),
                o.s("group"),
                o.s("name") ?: "",
                o.s("icon"),
                o.s("previewUrl"),
                o.i("order"),
                o.i("level"),
                o.s("sheet"),
                o.i("quantity"),
                o.b("closetHide")
            )
        }
        val lists = arr("itemLists") { o -> ItemList(o.s("guid")!!, o.s("description")) }
        val shops = arr("shops") { o ->
            Shop(
                o.s("guid")!!,
                o.s("type") ?: "Store",
                o.s("name"),
                o.s("permanent")
            )
        }
        val iaps = arr("iaps") { o ->
            Iap(
                o.s("guid")!!,
                o.d("price"),
                o.s("name"),
                o.b("returning"),
                o.i("c"),
                o.i("sc"),
                o.i("sp")
            )
        }
        val data = SkyData(
            DataConfig(realms),
            DataConfig(areas),
            DataConfig(constellations),
            DataConfig(winged),
            DataConfig(shrines),
            DataConfig(seasons),
            DataConfig(events),
            DataConfig(eventInstances),
            DataConfig(eventSpirits),
            DataConfig(spirits),
            DataConfig(trees),
            DataConfig(tiers),
            DataConfig(nodes),
            DataConfig(traveling),
            DataConfig(visits),
            DataConfig(visitSpirits),
            DataConfig(items),
            DataConfig(lists),
            DataConfig(shops),
            DataConfig(iaps)
        )
        link(root, data)
        return data
    }

    private inline fun <reified T : SkyEntity> get(by: Map<String, IGuid>, ref: String?): T? =
        by[ref] as? T

    private fun link(root: JsonObject, d: SkyData) {
        val by = d.guids
        d.realms.items.forEachIndexed { i, r ->
            val o = root.config("realms")[i].jsonObject; o.refs(
            "areas"
        ).mapNotNull { get<Area>(by, it) }
            .forEach { r.areas += it; it.realm = r }; r.constellation =
            get(by, o.ref("constellation")); r.elder = get(by, o.ref("elder"))
        }
        d.areas.items.forEachIndexed { i, a ->
            val o = root.config("areas")[i].jsonObject; o.refs("spirits")
            .mapNotNull { get<Spirit>(by, it) }
            .forEach { a.spirits += it; it.area = a }; o.refs("wingedLights")
            .mapNotNull { get<WingedLight>(by, it) }
            .forEach { a.wingedLights += it; it.area = a }; o.refs("mapShrines")
            .mapNotNull { get<MapShrine>(by, it) }.forEach {
                a.mapShrines += it; it.area = a
            }; (o["connections"]?.jsonArray?.mapNotNull { it.jsonObject.ref("area") }
            ?: emptyList()).mapNotNull { get<Area>(by, it) }.forEach { a.connections += it }
        }
        d.seasons.items.forEachIndexed { i, s ->
            val o = root.config("seasons")[i].jsonObject; s.number = i + 1; o.refs("spirits")
            .mapNotNull { get<Spirit>(by, it) }
            .forEach { s.spirits += it; it.season = s }; o.refs("shops")
            .mapNotNull { get<Shop>(by, it) }
            .forEach { s.shops += it; it.season = s }; o.refs("includedTrees")
            .mapNotNull { get<SpiritTree>(by, it) }.forEach { s.includedTrees += it }
        }
        d.events.items.forEachIndexed { i, e ->
            val o = root.config("events")[i].jsonObject; o.refs(
            "instances"
        ).mapNotNull { get<EventInstance>(by, it) }
            .forEach { it.event = e; it.number = e.instances.size + 1; e.instances += it }
        }
        d.spirits.items.forEachIndexed { i, s ->
            s.index = i;
            val o = root.config("spirits")[i].jsonObject; s.tree =
            get(by, o.ref("tree")); s.tree?.spirit = s
        }
        d.travelingSpirits.items.forEachIndexed { i, t ->
            val o = root.config("travelingSpirits")[i].jsonObject; t.number = i + 1; t.spirit =
            get(by, o.ref("spirit")); t.tree =
            get(by, o.ref("tree")); t.spirit?.travelingSpirits?.add(t); t.tree?.travelingSpirit = t
        }
        d.shops.items.forEachIndexed { i, s ->
            val o = root.config("shops")[i].jsonObject; s.spirit =
            get(by, o.ref("spirit")); s.spirit?.shops?.add(s); o.refs("iaps")
            .mapNotNull { get<Iap>(by, it) }.forEach { s.iaps += it; it.shop = s }; s.itemList =
            get(by, o.ref("itemList")); s.itemList?.shop = s
        }
        d.iaps.items.forEachIndexed { i, p ->
            val o = root.config("iaps")[i].jsonObject; o.refs("items")
            .mapNotNull { get<Item>(by, it) }
            .forEach { p.items += it; it.iaps += p; if (p.bought) it.unlocked = true }
        }
        d.itemLists.items.forEachIndexed { i, l ->
            val o =
                root.config("itemLists")[i].jsonObject; (o["items"]?.jsonArray?.mapNotNull { it.jsonObject }
            ?: emptyList()).forEach { n ->
            val node = ItemListNode(
                n.s("guid") ?: "",
                Cost(n.i("c"), n.i("h"), n.i("sc"), n.i("sh"), n.i("ac"), n.i("ec")),
                n.i("quantity")
            ); node.item = get(by, n.ref("item")); node.itemList =
            l; node.item?.listNodes?.add(node); l.items += node
        }
        }
        d.nodes.items.forEachIndexed { i, n ->
            val o = root.config("nodes")[i].jsonObject; n.item =
            get(by, o.ref("item")); n.item?.nodes?.add(n); n.hiddenItems += o.refs("hiddenItems")
            .mapNotNull { get<Item>(by, it) }; n.hiddenItems.forEach { it.hiddenNodes += n }; n.n =
            get(by, o.ref("n")); n.ne = get(by, o.ref("ne")); n.nw = get(by, o.ref("nw"))
        }
        d.spiritTreeTiers.items.forEachIndexed { i, t ->
            val o = root.config("spiritTreeTiers")[i].jsonObject; t.tree =
            get(by, o.ref("tree")); t.next = get(by, o.ref("next")); t.next?.prev = t; t.root =
            t.root ?: t
        }
        d.spiritTrees.items.forEachIndexed { i, t ->
            val o = root.config("spiritTrees")[i].jsonObject; t.node =
            get(by, o.ref("node")); t.tier = get(by, o.ref("tier")); t.tier?.tree =
            t; t.node?.tree = t
        }
    }
}
