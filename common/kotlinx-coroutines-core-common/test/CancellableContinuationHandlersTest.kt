/*
 * Copyright 2016-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import kotlin.coroutines.*
import kotlin.test.*

class CancellableContinuationHandlersTest : TestBase() {

    @Test
    fun testDoubleSubscription() = runTest({ it is IllegalStateException }) {
        suspendCancellableCoroutine<Unit> { c ->
            c.invokeOnCancellation { finish(1) }
            c.invokeOnCancellation { expectUnreached() }
        }
    }

    @Test
    fun testDoubleSubscriptionAfterCompletion() = runTest {
        suspendCancellableCoroutine<Unit> { c ->
            c.resume(Unit)
            // Nothing happened
            c.invokeOnCancellation { expectUnreached() }
            c.invokeOnCancellation { expectUnreached() }
        }
    }

    @Test
    fun testDoubleSubscriptionAfterCancellation() = runTest {
        try {
            suspendCancellableCoroutine<Unit> { c ->
                c.cancel()
                c.invokeOnCancellation {
                    assertTrue(it is CancellationException)
                    expect(1)
                }
                c.invokeOnCancellation {
                    assertTrue(it is CancellationException)
                    expect(2)
                }
            }
        } catch (e: CancellationException) {
            finish(3)
        }
    }

    @Test
    fun testDoubleSubscriptionAfterCancellationWithCause() = runTest {
        try {
            suspendCancellableCoroutine<Unit> { c ->
                c.cancel(AssertionError())
                c.invokeOnCancellation {
                    require(it is AssertionError)
                    expect(1)
                }
                c.invokeOnCancellation {
                    require(it is AssertionError)
                    expect(2)
                }
            }
        } catch (e: AssertionError) {
            finish(3)
        }
    }

    @Test
    fun testDoubleSubscriptionMixed() = runTest {
        try {
            suspendCancellableCoroutine<Unit> { c ->
                c.invokeOnCancellation {
                    require(it is IndexOutOfBoundsException)
                    expect(1)
                }

                c.cancel(IndexOutOfBoundsException())
                c.invokeOnCancellation {
                    require(it is IndexOutOfBoundsException)
                    expect(2)
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            finish(3)
        }
    }

    @Test
    fun testExceptionInHandler() = runTest({it is CompletionHandlerException}) {
        suspendCancellableCoroutine<Unit> { c ->
            c.invokeOnCancellation { throw AssertionError() }
            c.cancel()
        }
    }
}
