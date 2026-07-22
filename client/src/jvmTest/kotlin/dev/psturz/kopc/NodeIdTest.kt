package dev.psturz.kopc

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec

class NodeIdTest : FunSpec({
    test("accepts numeric and string identifiers with an explicit namespace") {
        NodeId("ns=0;i=2258")
        NodeId("ns=2;s=Foo")
    }

    test("accepts identifiers without an explicit namespace, defaulting to ns=0") {
        NodeId("i=2258")
    }

    test("rejects a malformed value") {
        shouldThrow<IllegalArgumentException> { NodeId("not-a-node-id") }
    }
})
