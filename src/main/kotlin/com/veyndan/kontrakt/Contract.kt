package com.veyndan.kontrakt

import org.spekframework.spek2.dsl.GroupBody
import org.spekframework.spek2.style.specification.describe
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertEquals

// Sealed class is used here instead of an interface
// to forbid inheriting it without implementing Named
sealed class PersonBuilder {
    var _name: String? = null
    var age: Int? = null

    // Marker interface which indicates that this PersonBuilder has an initialized name
    interface Named

    // Now we know that each PersonBuilder implements Named
    private class Impl : PersonBuilder(), Named

    companion object {
        // This function invocation looks like constructor invocation
        operator fun invoke(): PersonBuilder = Impl()
    }
}

// Receiver object will be smart casted to <PersonBuilder & Named>
@ExperimentalContracts
fun PersonBuilder.name(name: String) {
    contract {
        returns() implies (this@name is PersonBuilder.Named)
    }
    _name = name
}

// Extension property for <PersonBuilder & Named>
val <S> S.name where
        S : PersonBuilder,
        S : PersonBuilder.Named
    get() = _name!!

// This method can be called only if the builder has been named
fun <S> S.build(): Person where
        S : PersonBuilder,
        S : PersonBuilder.Named = Person(name, age)

data class Person(val name: String, val age: Int?): PersonBuilder.Named

@ExperimentalContracts
fun <T> GroupBody.offer(contract: Contract<T>) {
    val personBuilder = PersonBuilder()
    personBuilder.name("hello")
    personBuilder.age = 10
    personBuilder.build()

    describe(contract.title) {
        beforeGroup {
            contract.preamble()
        }

        afterGroup {
            contract.postamble()
        }

        contract.clauses.forEach { clause ->
            describe(clause.title) {
                clause.obligations.forEach { obligation ->
                    it(obligation.description) {
                        assertEquals(obligation.expected, obligation.actual(clause.block()))
                    }
                }
            }
        }
    }
}

// TODO We should allow any types of clauses, so it shouldn't be bound to a type, i.e., make it Contract instead of Contract<T>
data class Contract<T>(
    val title: String,
    val preamble: () -> Unit = {},
    val clauses: Set<Clause<T>>,
    val postamble: () -> Unit = {}
)

data class Clause<T>(val title: String, val block: () -> T, val obligations: Set<Obligation<*, T>>)

data class Obligation<S, T>(val description: String, val expected: S, val actual: (T) -> S)
