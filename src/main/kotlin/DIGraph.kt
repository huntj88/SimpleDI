import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

object DIGraph {
    val registeredClasses: MutableList<KClass<Any>> = mutableListOf()
    val interfaceMapping: MutableMap<KClass<Any>,KClass<Any>> = mutableMapOf()
    val instances: MutableMap<KClass<Any>, Any> = mutableMapOf()

    private fun KClass<Any>.actualKlass(): KClass<Any>? = when(this.isAbstract) {
        false -> this
        true -> interfaceMapping[this]
    }

    fun build() {
        val leafs = registeredClasses
            .filter { it.actualKlass()?.primaryConstructor?.parameters?.isEmpty() == true}

        leafs
            .map { it to it.actualKlass()!!.primaryConstructor!!.call() }
            .toMap()
            .let { instances.putAll(it) }

        val remaining = registeredClasses - leafs

        remaining.buildNextActionableRemaining()
    }

    private tailrec fun List<KClass<Any>>.buildNextActionableRemaining() {
        val actionable = this.filter {
            it.actualKlass()!!.primaryConstructor!!.parameters.fold(true) { acc, next ->
                val paramKlass = next.type.jvmErasure

                if(paramKlass !in registeredClasses) {
                    throw IllegalStateException("Could not build graph. $paramKlass is not registered")
                }
                acc && paramKlass in instances.keys
            }
        }

        actionable
            .map {
                val actualKlass = it.actualKlass()!!
                val dependencies = actualKlass.primaryConstructor!!.parameters
                    .map { next -> next to instances[next.type.jvmErasure]!! }
                    .toMap()

                it to actualKlass.primaryConstructor!!.callBy(dependencies)
            }
            .toMap()
            .let { instances.putAll(it) }

        val remainingNextPass = this - actionable
        if(remainingNextPass.isEmpty()) return
        return remainingNextPass.buildNextActionableRemaining()
    }
}

inline fun <reified T : Any> register() {
    DIGraph.registeredClasses.add(T::class as KClass<Any>)
}

inline fun <reified Interface : Any, reified T: Interface> registerInterface() {
    DIGraph.registeredClasses.add(Interface::class as KClass<Any>)
    DIGraph.interfaceMapping[Interface::class as KClass<Any>] = T::class as KClass<Any>
}

inline fun <reified T : Any> ref(): T {
    return DIGraph.instances[T::class as KClass<Any>] as T
}
