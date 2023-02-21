package pt.up.fe.ni.website.backend.utils.listeners

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource
import org.hibernate.id.enhanced.PooledOptimizer
import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.hibernate.metamodel.model.domain.internal.MappingMetamodelImpl
import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

class DbCleanupListener : TestExecutionListener {
    companion object {
        private const val SCHEMA_NAME = "PUBLIC"
        private const val DB_NAME = "H2"
    }

    override fun beforeTestMethod(testContext: TestContext) {
        super.beforeTestMethod(testContext)
        println("Cleaning up database...")

        val dataSource = testContext.applicationContext.getBean(DataSource::class.java)
        cleanDataSource(dataSource)

        val entityManager = testContext.applicationContext.getBean(EntityManager::class.java)
        cleanEntityManager(entityManager)

        println("Done cleaning database.")
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    private fun cleanDataSource(dataSource: DataSource) {
        val connection = dataSource.connection
        val statement = connection.createStatement()
        if (connection.isH2Database()) {
            disableConstraints(statement)
            truncateTables(statement)
            resetSequences(statement)
            enableConstraints(statement)
        } else {
            print(
                "Unexpected database type: ${connection.metaData.databaseProductName}" +
                    " (expected: $DB_NAME). Skipping cleanup."
            )
        }
    }

    /**
     * Resets sequence generator to properly restart counting IDs at 1
     */
    private fun cleanEntityManager(entityManager: EntityManager) {
        val metaModel = entityManager.metamodel as MappingMetamodelImpl
        metaModel.forEachEntityDescriptor { entityDescriptor ->
            if (!entityDescriptor.hasIdentifierProperty() ||
                entityDescriptor.identifierGenerator !is SequenceStyleGenerator
            ) {
                return@forEachEntityDescriptor
            }

            val sequenceStyleGenerator = (entityDescriptor.identifierGenerator as SequenceStyleGenerator)
            val optimizer = sequenceStyleGenerator.optimizer as? PooledOptimizer
                ?: return@forEachEntityDescriptor

            optimizer::class.java.getDeclaredField("noTenantState").apply {
                isAccessible = true
                set(optimizer, null)
            }
        }
    }

    private fun enableConstraints(statement: Statement) =
        statement.executeUpdate("SET REFERENTIAL_INTEGRITY TRUE")

    private fun disableConstraints(statement: Statement) =
        statement.executeUpdate("SET REFERENTIAL_INTEGRITY FALSE")

    private fun truncateTables(statement: Statement) =
        getSchemaTables(statement).forEach { statement.executeUpdate("TRUNCATE TABLE $it RESTART IDENTITY") }

    private fun resetSequences(statement: Statement) =
        getSchemaSequences(statement).forEach {
            statement.executeUpdate("ALTER SEQUENCE $it RESTART WITH 1")
        }

    private fun getSchemaTables(statement: Statement): Set<String> =
        convertQueryToSet(
            statement,
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$SCHEMA_NAME'"
        )

    private fun getSchemaSequences(statement: Statement): Set<String> =
        convertQueryToSet(
            statement,
            "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='$SCHEMA_NAME'"
        )

    private fun convertQueryToSet(statement: Statement, sql: String): Set<String> =
        statement.executeQuery(sql).use { rs ->
            HashSet<String>().apply {
                while (rs.next()) add(rs.getString(1))
            }
        }

    private fun Connection.isH2Database() = DB_NAME == this.metaData.databaseProductName
}
