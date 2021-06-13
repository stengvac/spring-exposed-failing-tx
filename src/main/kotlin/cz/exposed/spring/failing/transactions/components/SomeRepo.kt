package cz.exposed.spring.failing.transactions.components

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class SomeRepo {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun insertRequireNewTransaction(insertedId: UUID) {
        insertWithoutTransaction(insertedId)
    }

    @Transactional(propagation = Propagation.NESTED)
    fun insertRequireNested(insertedId: UUID) {
        insertWithoutTransaction(insertedId)
    }

    fun insertWithoutTransaction(insertedId: UUID) {
        SomeTable.insert { statement ->
            statement[id] = insertedId
        }
    }

    fun findById(id: UUID): UUID? {
        return SomeTable.select { SomeTable.id eq id }.singleOrNull()?.let { rw -> rw[SomeTable.id].value }
    }
}

object SomeTable : IdTable<UUID>("some_table") {
    override val id: Column<EntityID<UUID>> = uuid("some_id").entityId()
}