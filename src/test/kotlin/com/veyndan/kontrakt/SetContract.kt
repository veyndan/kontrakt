package com.veyndan.kontrakt

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// Title: Legally Binding Tests in Kotlin

val contract = Contract(
    title = "Set",
    preamble = {
        println("Setup expensive operation.")
    },
    clauses = setOf(
        Clause(
            title = "adding items",
            block = { emptySet<String>() + "foo" },
            obligations = setOf(
                Obligation(
                    description = "it should have a size of 1",
                    expected = 1,
                    actual = { it.size }
                ),
                Obligation(
                    description = "it should contain foo",
                    expected = true,
                    actual = { it.contains("foo") }
                )
            )
        ),
        Clause(
            title = "empty",
            block = { emptySet<String>() },
            obligations = setOf(
                Obligation(
                    description = "should have a size of 0",
                    expected = 0,
                    actual = { it.size }
                )
//                Then() {
//                    assertFailsWith(NoSuchElementException::class) {
//                        set.first()
//                    }
//                }
            )
        )
    ),
    postamble = {
        println("Teardown expensive operation.")
    }
)

object SetSpec : Spek({
    offer(contract)
})

object SetFeature : Spek({
    Feature("Set") {
        val set by memoized { mutableSetOf<String>() }

        Scenario("adding items") {
            When("adding foo") {
                set.add("foo")
            }

            Then("it should have a size of 1") {
                assertEquals(1, set.size)
            }

            Then("it should contain foo") {
                assertTrue(set.contains("foo"))
            }
        }

        Scenario("empty") {
            Then("should have a size of 0") {
                assertEquals(0, set.size)
            }

            Then("should throw when first is invoked") {
                assertFailsWith(NoSuchElementException::class) {
                    set.first()
                }
            }
        }

        Scenario("getting the first item") {
            val item = "foo"
            Given("a non-empty set") {
                set.add(item)
            }

            lateinit var result: String

            When("getting the first item") {
                result = set.first()
            }

            Then("it should return the first item") {
                assertEquals(item, result)
            }
        }
    }
})
