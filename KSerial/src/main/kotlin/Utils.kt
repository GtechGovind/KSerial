@OptIn(ExperimentalStdlibApi::class)
fun String.hexToBytes() = try {
    this.hexToByteArray()
} catch (e: Exception) {
    byteArrayOf()
}

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex() = try {
    this.toHexString()
} catch (e: Exception) {
    ""
}