package dev.psturz.kopc.server

import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned

/**
 * The wire format only carries write values as strings, but OPC UA writes must match the
 * node's actual data type or fail with Bad_TypeMismatch. [sample] is a freshly read value from
 * the same node, used purely to recover which type [raw] needs to be parsed into.
 */
fun coerceValue(raw: String, sample: Any?): Any = when (sample) {
    is Boolean -> raw.toBooleanStrict()
    is Byte -> raw.toByte()
    is Short -> raw.toShort()
    is Int -> raw.toInt()
    is Long -> raw.toLong()
    is Float -> raw.toFloat()
    is Double -> raw.toDouble()
    is UByte -> Unsigned.ubyte(raw.toShort())
    is UShort -> Unsigned.ushort(raw.toInt())
    is UInteger -> Unsigned.uint(raw.toLong())
    is ULong -> Unsigned.ulong(raw.toLong())
    else -> raw
}
