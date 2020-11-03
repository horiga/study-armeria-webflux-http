package org.horiga.study.armeria.http.repository

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Table("member")
data class MemberEntity(
    @Column("name")
    val name: String,
    @Column("type")
    val type: String,
    @Column("created_at")
    val createdAt: DateTime,
    @Id
    val id: Long? = null
)

@Repository
interface MemberRepository : ReactiveCrudRepository<MemberEntity, String> {

    @Query("SELECT * FROM member WHERE name LIKE :name")
    fun findByName(name: String): Flux<MemberEntity>
}
