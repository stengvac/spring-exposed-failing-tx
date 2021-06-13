package cz.exposed.spring.failing.transactions.nested

import cz.exposed.spring.failing.transactions.components.SomeRepo
import cz.exposed.spring.failing.transactions.components.SomeService
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.*
import javax.annotation.PostConstruct
import javax.sql.DataSource


@Configuration
class NestedTransactionManagerConfiguration {

    @Bean
    fun txManager(dataSource: DataSource): SpringTransactionManager {
        val stm = SpringTransactionManager(dataSource)
        val db = stm::class.java.declaredFields.first { it.name == "db" }.apply {
            isAccessible = true
        }.get(stm) as Database
        db.useNestedTransactions = stm.isNestedTransactionAllowed

        return stm
    }
}


@EnableTransactionManagement
@SpringBootApplication(
    scanBasePackages = ["cz.exposed.spring.failing.transactions.components"],
    exclude = [ExposedAutoConfiguration::class]
)
@Import(NestedTransactionManagerConfiguration::class)
class NestedTransactionApp(
    private val someService: SomeService,
    private val someRepo: SomeRepo,
    //force initialization
    txManager: SpringTransactionManager
) {
    /**
     * Flow
     *
     * 1, AbstractPlatformTransactionManager#handleExistingTransaction
     * 2, Decision is made by useSavepointForNestedTransaction() by default return true
     * If i understand correctly then for expose purposes this func should return false.
     * Then `doBegin` is called and exposed Spring Manager can react.
     *
     * S
     */
    @PostConstruct
    fun `nested transaction was not created`() {
        //nested with exposed dsl
        val exposedId = UUID.randomUUID()
        transaction {
            println("useNestedTransactions allowed ${db.useNestedTransactions}")
            someRepo.insertWithoutTransaction(exposedId)

            //if i understand correctly this code should be repeated 3x and fails should not abort outer tx
            transaction {
                try {
                    someRepo.insertWithoutTransaction(exposedId)
                } catch (e: ExposedSQLException) {
                    e.printStackTrace()
                }
            }
        }

        val r = transaction {
            someRepo.findById(exposedId)
        }
        println("insert with exposed tx present ${r != null}")

        //nested with JTA
        val someId = UUID.randomUUID()
        someRepo.insertRequireNewTransaction(someId)
        try {
            someService.insertWithNestedTx(someId)
            //no ex -> ok
        } catch (e: ExposedSQLException) {
            e.printStackTrace()
        }

        //works fine bcs in new transaction
        val entityFromDb = someService.findInTransaction(someId)
        println("entity inserted with JTA in DB: ${entityFromDb != null}")
    }
}

fun main() {
    runApplication<NestedTransactionApp>()
}
