package cn.chitanda.kmmage

import cn.chitanda.kmmage.decode.Decoder
import cn.chitanda.kmmage.fetch.Fetcher
import cn.chitanda.kmmage.fetch.SourceResult
import cn.chitanda.kmmage.intercept.Interceptor
import cn.chitanda.kmmage.key.Keyer
import cn.chitanda.kmmage.map.Mapper
import cn.chitanda.kmmage.request.Options
import cn.chitanda.kmmage.util.forEachIndices

/**
 * @author: Chen
 * @createTime: 2022/4/29 15:58
 * @description:
 **/
@Suppress("UNCHECKED_CAST")
class ComponentRegistry private constructor(
    val interceptors: List<Interceptor>,
    val mappers: List<Pair<Mapper<out Any, out Any>, Class<out Any>>>,
    val keyers: List<Pair<Keyer<out Any>, Class<out Any>>>,
    val fetcherFactories: List<Pair<Fetcher.Factory<out Any>, Class<out Any>>>,
    val decoderFactories: List<Decoder.Factory>
) {
    constructor() : this(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

    fun map(data: Any, options: Options): Any {
        var mappedData = data
        mappers.forEachIndices { (mapper, type) ->
            if (type.isAssignableFrom(mappedData::class.java)) {
                (mapper as Mapper<Any, *>).map(mappedData, options)?.let {
                    mappedData = it
                }
            }
        }

        return mappedData
    }

    fun key(data: Any, options: Options): String? {
        keyers.forEachIndices { (keyer, type) ->
            if (type.isAssignableFrom(data::class.java)) {
                (keyer as Keyer<Any>).key(data, options)?.let { return it }
            }
        }
        return null
    }

    fun newFetcher(
        data: Any,
        options: Options,
        imageLoader: ImageLoader,
        startIndex: Int = 0
    ): Pair<Fetcher, Int>? {
        for (i in startIndex..fetcherFactories.lastIndex) {
            val (factory, type) = fetcherFactories[i]
            if (type.isAssignableFrom(data::class.java)) {
                (factory as Fetcher.Factory<Any>).create(data, options, imageLoader)
                    ?.let { return it to i }
            }
        }
        return null
    }

    fun newDecoder(
        result: SourceResult, options: Options,
        imageLoader: ImageLoader,
        startIndex: Int = 0
    ): Pair<Decoder, Int>? {
        for (i in startIndex..decoderFactories.lastIndex) {
            decoderFactories[i].create(result, options, imageLoader)?.let {
                return it to i
            }
        }
        return null
    }

    fun newBuilder(): Builder = Builder(this)

    class Builder {

        internal val interceptors: MutableList<Interceptor>
        internal val mappers: MutableList<Pair<Mapper<out Any, *>, Class<out Any>>>
        internal val keyers: MutableList<Pair<Keyer<out Any>, Class<out Any>>>
        internal val fetcherFactories: MutableList<Pair<Fetcher.Factory<out Any>, Class<out Any>>>
        internal val decoderFactories: MutableList<Decoder.Factory>

        constructor() {
            interceptors = mutableListOf()
            mappers = mutableListOf()
            keyers = mutableListOf()
            fetcherFactories = mutableListOf()
            decoderFactories = mutableListOf()
        }

        constructor(registry: ComponentRegistry) {
            interceptors = registry.interceptors.toMutableList()
            mappers = registry.mappers.toMutableList()
            keyers = registry.keyers.toMutableList()
            fetcherFactories = registry.fetcherFactories.toMutableList()
            decoderFactories = registry.decoderFactories.toMutableList()
        }

        fun add(interceptor: Interceptor) = apply {
            interceptors += interceptor
        }

        inline fun <reified T : Any> add(mapper: Mapper<T, *>) = add(mapper, T::class.java)

        fun <T : Any> add(mapper: Mapper<T, *>, type: Class<T>) = apply {
            mappers += mapper to type
        }


        inline fun <reified T : Any> add(keyer: Keyer<T>) = add(keyer, T::class.java)

        fun <T : Any> add(keyer: Keyer<T>, type: Class<T>) = apply {
            keyers += keyer to type
        }

        inline fun <reified T : Any> add(fetcherFactory: Fetcher.Factory<T>) =
            add(fetcherFactory, T::class.java)

        fun <T : Any> add(fetcherFactory: Fetcher.Factory<T>, type: Class<T>) = apply {
            fetcherFactories += fetcherFactory to type
        }

        fun add(decodeFactory: Decoder.Factory) = apply {
            decoderFactories += decodeFactory
        }

        fun build(): ComponentRegistry {
            return ComponentRegistry(
                interceptors = interceptors.toList(),
                mappers = mappers.toList(),
                keyers = keyers.toList(),
                fetcherFactories = fetcherFactories.toList(),
                decoderFactories = decoderFactories.toList()
            )
        }
    }

}