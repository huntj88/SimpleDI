import org.junit.Assert.*
import org.junit.Test

class LazyDIGraphTest {

    @Test
    fun test() {
        LazyDIGraph().apply {
            register<Heater> { ElectricHeater() }
            register<Pump> { Thermosiphon(ref()) }
            register<Filter> { PaperFilter() }
            register { CoffeeMachine(ref(), ref()) }

            println(ref<CoffeeMachine>())
        }
    }
}

interface Pump
data class Thermosiphon(val heater: Heater): Pump

interface Filter
class PaperFilter: Filter
class CarbonFilter: Filter

interface Heater
class ElectricHeater: Heater

data class CoffeeMachine(val filter: Filter, val pump: Pump)
