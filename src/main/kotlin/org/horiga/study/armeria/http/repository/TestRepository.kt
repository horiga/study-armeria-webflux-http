package org.horiga.study.armeria.http.repository

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant

@Table("test")
data class TestEntity(
    @Id
    val id: String,
    @Column("name")
    val name: String,
    @Column("type")
    val type: String,
    @Column("created_at")
    val createdAt: Instant
)

@Repository
interface TestRepository : ReactiveCrudRepository<TestEntity, String> {

    @Query("SELECT * FROM test WHERE name LIKE :name")
    fun findByName(name: String): Flux<TestEntity>
}
