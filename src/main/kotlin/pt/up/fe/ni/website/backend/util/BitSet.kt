package pt.up.fe.ni.website.backend.util

import java.util.BitSet

fun BitSet.enable(bit: Int): BitSet {
    this.set(bit)
    return this
}

fun BitSet.enable(firstBit: Int, lastBit: Int): BitSet {
    this.set(firstBit, lastBit)
    return this
}

fun Long.bitSet(): BitSet {
    val bits = BitSet()
    var index = 0
    var value: Long = this

    while (value != 0L) {
        if ((value % 2L) != 0L) {
            bits.set(index)
        }
        ++index
        value = value ushr 1
    }

    return bits
}
