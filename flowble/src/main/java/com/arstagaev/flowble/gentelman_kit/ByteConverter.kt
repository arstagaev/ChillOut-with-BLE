package com.arstagaev.flowble.gentelman_kit

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

fun bytesToHex(bytes: ByteArray): String {
    if (bytes == null)
        return "null"
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v: Int = (bytes[j].toInt() and 0xFF)
        hexChars[j * 2] = HEX_ARRAY.get(v ushr 4)
        hexChars[j * 2 + 1] = HEX_ARRAY.get(v and 0x0F)
    }
    return String(hexChars)
}

fun hexToBytes(hexRepresentation: String): ByteArray {
    require(hexRepresentation.length % 2 != 1) { "hexToBytes requires an even-length String parameter" }
    val len = hexRepresentation.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] = ((Character.digit(hexRepresentation[i], 16) shl 4)
                + Character.digit(
            hexRepresentation[i + 1],
            16
        )).toByte()
        i += 2
    }
    return data
}

fun searchCompareElements(arrayList1 : ArrayList<Int>, arrayList2 : ArrayList<Int>): Set<Int> {

    var output = arrayListOf<Int>()

    for (i in arrayList1) {

        for (z in arrayList2) {
            if (i == z) {
                output.add(i)
            }
        }

    }
    val foo: Set<Int> = HashSet<Int>(output)


    println("> ${foo.joinToString()}")
    return foo
}