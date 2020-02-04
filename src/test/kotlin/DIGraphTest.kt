import org.junit.Test

class DIGraphTest {
    @Test
    fun test() {
        register<Dep1>()
        registerInterface<IDep2, Dep2>()
        registerInterface<IDep3, Dep3>()
        register<Dep4>()

        register<HttpClient>()
        registerInterface<GitHubService, GithubServiceImpl>()
        register<RestController>()

        DIGraph.build()

        println(ref<RestController>())
    }
}
class HttpClient

interface GitHubService
data class GithubServiceImpl(val client: HttpClient, val dep1: Dep1): GitHubService

data class RestController(val service: GitHubService)

data class Dep1(private val dep2: IDep2, private val dep3: IDep3)

interface IDep2
class Dep2: IDep2

interface IDep3
data class Dep3(private val dep4: Dep4): IDep3
data class Dep4(private val dep2: IDep2)
