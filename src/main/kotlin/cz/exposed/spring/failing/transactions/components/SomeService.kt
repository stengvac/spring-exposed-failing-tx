package cz.exposed.spring.failing.transactions.components

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class SomeService(
    private val someRepo: SomeRepo
) {

    @Transactional
    fun insertWithRequireNewTransaction(someId: UUID) {
        val existInDbBeforeInsert = someRepo.findById(someId) != null
        if (existInDbBeforeInsert) throw IllegalStateException("Should not be in db")
        //require new transaction -> suspend current one and execute other then resume previous one
        someRepo.insertRequireNewTransaction(someId)
        //previous transaction is not resumed
        //will throw no tx in context
        val afterInsertShouldExist = someRepo.findById(someId) != null
        if (!afterInsertShouldExist) throw IllegalStateException("Should be in DB after insert")
    }

    @Transactional
    fun insertWithNestedTx(someId: UUID): UUID {
        val shouldBeInDBefore = someRepo.findById(someId) != null
        if (!shouldBeInDBefore) throw IllegalStateException("Should not be in db $someId")
        //for some reason try insert in nested tx -> will fail with ex, but it should not abort outer tx
        return try {
            //either insert and return inserted data
            someRepo.insertRequireNested(someId)
            someId
        } catch (e: ExposedSQLException) {
            //ex -> duplicate key
            e.printStackTrace()
            println("${TransactionManager.current().statementCount} should be 1")
            //this fetch should not throw ex with org.postgresql.util.PSQLException: ERROR: current transaction is aborted, commands ignored until end of transaction block
            someRepo.findById(someId)!!
        }
    }

    @Transactional
    fun findInTransaction(someId: UUID): UUID? {
        return someRepo.findById(someId)
    }
}