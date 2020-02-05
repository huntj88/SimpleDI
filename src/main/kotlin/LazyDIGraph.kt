import kotlin.reflect.KClass

class LazyDIGraph {
    val providerMap = mutableMapOf<KClass<Any>, () -> Any>()
    val instanceMap = mutableMapOf<KClass<Any>, Any>()

    inline fun <reified T> register(noinline provide: () -> T) {
        providerMap[T::class as KClass<Any>] = provide as () -> Any
    }

    inline fun <reified T> ref() : T {
        val instance = instanceMap[T::class as KClass<Any>]
            ?: providerMap[T::class as KClass<Any>]
                ?.invoke()
                ?.also { instanceMap[T::class as KClass<Any>] = it } as Any

        return instance as T
    }
}
