import jssc.SerialPort
import jssc.SerialPortList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * KSerial is a serial communication utility class providing features like auto-reconnect,
 * configurable retry mechanisms, and support for both string and byte array data exchanges.
 * This class is designed to be flexible, allowing configuration of parameters through its builder,
 * and it is coroutine-friendly for non-blocking IO operations.
 *
 * @property port The name of the serial port to be connected.
 * @property baudRate The baud rate for communication.
 * @property dataBits The data bits for serial configuration.
 * @property stopBits The stop bits for serial configuration.
 * @property parity The parity setting for serial configuration.
 * @property retryDelay Delay before retrying to connect, if auto-reconnect is enabled.
 * @property readDelay Delay after writing to allow the device to respond before reading.
 * @property maxFailureCount Maximum number of consecutive failures before reset.
 * @property logger A logging function for handling message logging.
 * @property isAutoReconnect Boolean to enable auto-reconnection on failure.
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
    private val isConnected = MutableStateFlow(false)
    private var serial: SerialPort? = null
    private var responseFailureCounter = 0

    /**
     * Starts the serial communication by connecting to the port.
     * If auto-reconnect is enabled, launches auto-connection in a coroutine.
     */
    override fun start() {
        scope.launch {
            if (isAutoReconnect) autoConnect()
            else connect()
        }
    }

    /**
     * Stops the serial communication and releases resources.
     * Cancels the coroutine scope and resets the failure counter.
     */
    override fun stop() {
        withError { serial?.closePort() }
        serial = null
        withError { scope.cancel() }
        responseFailureCounter = 0
        isConnected.value = false
    }

    /**
     * Writes a byte array to the serial port.
     * Ensures connection is active before sending data.
     */
    override suspend fun write(bytes: ByteArray): Boolean {
        ensureConnected()
        return serial?.writeBytes(bytes) ?: false
    }

    /**
     * Writes a string to the serial port.
     * Ensures connection is active before sending data.
     */
    override suspend fun write(data: String): Boolean {
        ensureConnected()
        return serial?.writeString(data) ?: false
    }

    /**
     * Reads data as a string from the serial port.
     * Ensures connection is active before reading.
     */
    override suspend fun readString(): String {
        ensureConnected()
        return serial?.readHexString() ?: error("Failed to read the from $port")
    }

    /**
     * Reads data as a byte array from the serial port.
     * Ensures connection is active before reading.
     */
    override suspend fun readBytes(): ByteArray {
        ensureConnected()
        return serial?.readBytes() ?: error("Failed to read the from $port")
    }

    /**
     * Sends a byte array request to the serial port and waits to receive a byte array response.
     * Delays reading to accommodate device response time.
     */
    override suspend fun sendReceive(request: ByteArray): ByteArray? {
        logger(request.contentToString())
        return try {
            ensureConnected()
            write(request)
            delay(readDelay)
            readBytes()
        } catch (e: Exception) {
            logger(e.message ?: e.stackTraceToString())
            responseFailureCounter++
            null
        }
    }

    /**
     * Sends a string request to the serial port and waits to receive a string response.
     * Delays reading to accommodate device response time.
     */
    override suspend fun sendReceive(request: String): String? {
        return try {
            ensureConnected()
            write(request)
            delay(readDelay)
            readString()
        } catch (e: Exception) {
            logger(e.message ?: e.stackTraceToString())
            responseFailureCounter++
            null
        }
    }

    /**
     * Ensures that the serial port connection is active.
     * Resets the connection if the port is not opened or failure count exceeds the maximum limit.
     */
    private suspend fun ensureConnected() = withError {
        withContext(Dispatchers.IO) {
            if (
                serial == null ||
                serial?.isOpened == false ||
                responseFailureCounter > maxFailureCount
            ) {
                logger("Failed to get $port status, resetting the connection!")
                stop()
                connect()
            }
        }
    }

    /**
     * Establishes a new connection to the serial port with the specified parameters.
     * Logs successful connections and sets connection state.
     */
    private fun connect() = withError {
        if (isConnected.value) error("Already connected to $port.")
        SerialPort(port).apply {
            if (!openPort()) error("Failed to connect to $port.")
            if (!setParams(baudRate, dataBits, stopBits, parity)) error("Failed to set parameters to $port.")
            serial = this
            isConnected.value = true
            logger("Connected to $port.")
        }
    }

    /**
     * Automatically attempts to maintain a connection to the serial port.
     * Runs on a recurring delay defined by `retryDelay`, checking and re-establishing connection as necessary.
     */
    private suspend fun autoConnect() = withError {
        withContext(Dispatchers.IO) {
            while (isActive) {
                logger("Checking if auto reconnect is required $port!")
                if (serial == null || serial?.isOpened == false || !isConnected.value) {
                    logger("Reconnecting from auto reconnect!")
                    connect()
                }
                delay(retryDelay)
            }
        }
    }

    /**
     * Helper function to handle errors during operations and log messages.
     * @param block The operation to perform with error handling.
     */
    private inline fun <T> withError(
        block: () -> T?
    ) = try {
        block()
    } catch (e: Exception) {
        logger(e.message ?: e.stackTraceToString())
        null
    }

    /**
     * Builder class for constructing KSerial instances with customizable parameters.
     * Provides fluent methods for setting connection and retry configurations.
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

        // Builder methods for setting parameters.
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
         * Returns a Builder instance for KSerial construction.
         */
        fun builder(port: String, logger: (String) -> Unit) = Builder(port, logger)

        /**
         * Returns an array of available serial ports.
         */
        fun getPorts(): Array<String> = SerialPortList.getPortNames()
    }
}
