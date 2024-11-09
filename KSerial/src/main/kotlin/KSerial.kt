import jssc.SerialPort
import jssc.SerialPortList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * KSerial - A Kotlin-based serial communication wrapper with support for auto-reconnection,
 * concurrency-safe data transmission, and configurable retry handling.
 */
class KSerial private constructor(
    private val port: String,
    private val baudRate: Int,
    private val dataBits: Int,
    private val stopBits: Int,
    private val parity: Int,
    private val retryDelay: Long,
    private val readDelay: Long,
    private val maxFailureCount: Int,
    private val logger: (message: String) -> Unit,
    private val isAutoReconnect: Boolean
) : IKSerial {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isConnected = MutableStateFlow(false) // Represents the connection status flow
    private var serial: SerialPort? = null
    private val communication = Mutex() // Ensures thread safety during data transmission
    private var responseFailureCounter = 0 // Counts consecutive read/write failures

    /**
     * Initializes the connection process. If auto-reconnect is enabled, it triggers autoConnect().
     */
    override fun start() {
        scope.launch {
            if (isAutoReconnect) autoConnect() else connect()
        }
    }

    /**
     * Terminates the serial connection and stops auto-reconnect attempts.
     */
    override fun stop() {
        scope.launch { disconnect() }
    }

    /**
     * Returns a StateFlow representing the current connection status.
     */
    override fun getConnectionStatusFlow(): StateFlow<Boolean> = isConnected

    /**
     * Sends a byte array over the serial connection.
     * @return True if the data was written successfully, false otherwise.
     */
    override suspend fun write(bytes: ByteArray): Boolean {
        return communication.withLock {
            try {
                ensureConnected()
                serial?.writeBytes(bytes) ?: false
            } catch (e: Exception) {
                handleFailure(e)
                false
            }
        }
    }

    /**
     * Sends a string over the serial connection.
     * @return True if the data was written successfully, false otherwise.
     */
    override suspend fun write(data: String): Boolean {
        return communication.withLock {
            try {
                ensureConnected()
                serial?.writeString(data) ?: false
            } catch (e: Exception) {
                handleFailure(e)
                false
            }
        }
    }

    /**
     * Reads data from the serial connection as a string.
     * @return The data read from the connection, or null if an error occurred.
     */
    override suspend fun readString(): String? {
        return communication.withLock {
            try {
                ensureConnected()
                serial?.readString() ?: throw Exception("Failed to read from $port")
            } catch (e: Exception) {
                handleFailure(e)
                null
            }
        }
    }

    /**
     * Reads data from the serial connection as a byte array.
     * @return The byte array read from the connection, or null if an error occurred.
     */
    override suspend fun readBytes(): ByteArray? {
        return communication.withLock {
            try {
                ensureConnected()
                serial?.readBytes() ?: throw Exception("Failed to read from $port")
            } catch (e: Exception) {
                handleFailure(e)
                null
            }
        }
    }

    /**
     * Sends a byte array request and waits for a byte array response.
     * @return The response byte array, or null if the operation failed.
     */
    override suspend fun sendReceive(request: ByteArray): ByteArray? {
        ensureConnected()
        write(request)
        delay(readDelay) // Allows the receiver time to respond
        return readBytes()
    }

    /**
     * Sends a string request and waits for a string response.
     * @return The response string, or null if the operation failed.
     */
    override suspend fun sendReceive(request: String): String? {
        ensureConnected()
        write(request)
        delay(readDelay) // Allows the receiver time to respond
        return readString()
    }

    /**
     * Ensures the connection is active. Attempts reconnection if the connection is lost
     * or if failure threshold is exceeded.
     */
    private suspend fun ensureConnected() = communication.withLock {
        if (serial == null || serial?.isOpened == false || responseFailureCounter >= maxFailureCount) {
            logger("Connection not active, attempting to reconnect to $port.")
            disconnect()
            connect()
        }
    }

    /**
     * Continuously attempts reconnection when the connection is lost, if auto-reconnect is enabled.
     * Runs on a background coroutine until the connection is successfully re-established.
     */
    private suspend fun autoConnect() = coroutineScope {
        while (isActive) {
            if (serial == null || !serial!!.isOpened) {
                logger("Auto-reconnect in progress for $port.")
                connect()
            }
            delay(retryDelay)
        }
    }

    /**
     * Establishes a connection to the serial port with the specified parameters.
     */
    private suspend fun connect() = communication.withLock {
        runCatching {
            if (isConnected.value) throw IllegalStateException("Already connected to $port.")
            SerialPort(port).apply {
                if (!openPort()) throw IllegalStateException("Failed to open port: $port")
                if (!setParams(baudRate, dataBits, stopBits, parity)) throw IllegalStateException("Failed to set port parameters.")
                serial = this
                isConnected.value = true
                logger("Connected to $port.")
                responseFailureCounter = 0
            }
        }.onFailure { e ->
            logger("Failed to connect to $port: ${e.message}")
            isConnected.value = false
        }
    }

    /**
     * Closes the current serial connection and resets connection state.
     */
    private suspend fun disconnect() = communication.withLock {
        runCatching {
            serial?.closePort()
        }.onFailure { e ->
            logger("Failed to close port $port: ${e.message}")
        }.also {
            serial = null
            isConnected.value = false
            responseFailureCounter = 0
        }
    }

    /**
     * Handles failures by logging the error, incrementing the failure counter, and
     * disconnecting if the maximum failure count is reached.
     */
    private suspend fun handleFailure(e: Exception) {
        responseFailureCounter++
        if (responseFailureCounter >= maxFailureCount) {
            logger("Maximum failure count reached for $port. Resetting connection.")
            disconnect()
        }
        logger(e.message ?: e.stackTraceToString())
    }

    /**
     * Builder for creating a customized instance of KSerial with various configuration options.
     */
    class Builder(private val port: String, private val logger: (String) -> Unit) {
        private var baudRate: Int = SerialPort.BAUDRATE_115200
        private var dataBits: Int = SerialPort.DATABITS_8
        private var stopBits: Int = SerialPort.STOPBITS_1
        private var parity: Int = SerialPort.PARITY_NONE
        private var retryDelay: Long = 3000L
        private var readDelay: Long = 100L
        private var isAutoReconnect: Boolean = true
        private var maxFailureCount: Int = 3

        fun baudRate(baudRate: Int) = apply { this.baudRate = baudRate }
        fun dataBits(dataBits: Int) = apply { this.dataBits = dataBits }
        fun stopBits(stopBits: Int) = apply { this.stopBits = stopBits }
        fun parity(parity: Int) = apply { this.parity = parity }
        fun retryDelay(timeoutMillis: Long) = apply { this.retryDelay = timeoutMillis }
        fun readDelay(timeoutMillis: Long) = apply { this.readDelay = timeoutMillis }
        fun enableAutoReconnect() = apply { this.isAutoReconnect = true }
        fun maxFailureCount(count: Int) = apply { this.maxFailureCount = count }

        fun build(): KSerial {
            return KSerial(
                port, baudRate, dataBits, stopBits, parity,
                retryDelay, readDelay, maxFailureCount, logger, isAutoReconnect
            )
        }
    }

    companion object {
        /**
         * Creates a new KSerial Builder instance for the specified port.
         */
        fun builder(port: String, logger: (String) -> Unit) = Builder(port, logger)

        /**
         * Lists all available serial ports.
         */
        fun getPorts(): Array<String> = SerialPortList.getPortNames()
    }
}
