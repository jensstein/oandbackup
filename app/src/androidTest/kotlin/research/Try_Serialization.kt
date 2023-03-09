package research

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test

//@Serializable
//data class AnyType(var any: @Contextual Any): Any() {
//    constructor(value: String) : this(value as Any)
//    constructor(value: Int) : this(value as Any)
//    constructor(value: Boolean) : this(value as Any)
//}

typealias AnyType = @Polymorphic Any
typealias MapAny = Map<String, AnyType>

class MapAnySerializer

class Try_Serialization {

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    //val context = Utils.getDeContext(Utils.getContext())
    //val context = Utils.getContext()

    fun <K, T> mapAnyOf(vararg args: Pair<K, T>) = mapOf<K, T>(*args)  //.mapValues { AnyType(it) }

    val aMap : MapAny = mapAnyOf(
        "int" to 123,
        "boolean" to false,
        "string" to "abc",
    )

    @Serializable
    data class aClass(val int: Int = 123, val flag: Boolean = false, val str: String = "abc")

    val aObj = aClass(int = 456, flag = true, str = "str")


    val serializersMod: SerializersModule = SerializersModule {
        //contextual(Boolean.serializer())
        //contextual(Int.serializer())
        //contextual(String.serializer())
        polymorphic(Any::class) {
            subclass(Boolean.serializer())
            subclass(Int.serializer())
            subclass(String.serializer())
        }
        //polymorphic(Any::class) {
        //    subclass(Boolean::class)
        //    subclass(Int::class)
        //    subclass(String::class)
        //}
        //polymorphic(Any::class) {
        //    subclass(Boolean::class, Boolean.serializer())
        //    subclass(Int::class, Int.serializer())
        //    subclass(String::class, String.serializer())
        //}
        //polymorphic(Any::class, Boolean::class, Boolean.serializer())
        //polymorphic(Any::class, Int::class, Int.serializer())
        //polymorphic(Any::class, String::class, String.serializer())
    }

    val json = Json {
        serializersModule = serializersMod
        prettyPrint = true
    }

    val serializer = json

    @Test
    fun test_map() {
        val ser = serializer.encodeToString(aMap)
        println("ser: '\n$ser\n'")
        val obj: MapAny = serializer.decodeFromString(ser)
        assertEquals(aMap, obj)
    }

    @Test
    fun test_obj() {
        val ser = serializer.encodeToString(aObj)
        println("ser: '\n$ser\n'")
        val obj : aClass = serializer.decodeFromString(ser)
        assertEquals(aObj, obj)
    }

    @Test
    fun test_jsonElement() {
        val ele = json.encodeToJsonElement(aMap)
        println("ele: '\n$ele\n'")
        val prim = ele.jsonObject.mapValues { it.value.jsonPrimitive }
        val ser = prim.toString()
        val obj: MapAny = serializer.decodeFromString(ser)
        assertEquals(aMap, obj)
    }
}
