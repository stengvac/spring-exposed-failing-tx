package cz.exposed.spring.failing.transactions.newtx

import cz.exposed.spring.failing.transactions.components.SomeService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication(scanBasePackages = ["cz.exposed.spring.failing.transactions.components"])
class RequireNewTransactionFailingApp(
    private val someService: SomeService
) {
    /**
     * SuspendedResourcesHolder
     * Seems AbstractPlatformTransactionManager#doResume needs to be impl
     */
    @PostConstruct
    fun `no context for Propagation-REQUIRES_NEW`() {
        val someId = UUID.randomUUID()
        someService.insertWithRequireNewTransaction(someId)
        //no ex -> ok

        //works fine bcs in new transaction
        val entityFromDb = someService.findInTransaction(someId)
        println("entity found in DB: ${entityFromDb != null}")
    }
}

fun main() {
    runApplication<RequireNewTransactionFailingApp>()
}
