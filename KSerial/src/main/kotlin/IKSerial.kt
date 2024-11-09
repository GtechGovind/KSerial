import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for serial communication functionality in a structured, asynchronous manner.
 * Provides methods to start and stop communication, read and write data, and handle
 * bidirectional data exchange with serial devices.
 */
interface IKSerial {

    /**
     * Initiates the serial connection and opens the specified port.
     * This method should be called to begin communication with the target serial device.
     * Non-blocking operation without exceptions.
     */
    fun start()

    /**
     * Terminates the serial connection and releases the associated port.
     * Should be called to properly close and clean up resources when communication is no longer needed.
     * Non-blocking operation without exceptions.
     */
    fun stop()

    /**
     * Writes a specified byte array to the connected serial port.
     * Sends raw data directly, which can be used to interface with devices
     * expecting byte-level communication.
     *
     * @param bytes The byte array to send to the serial port.
     * @return Boolean indicating if the write operation was successful.
     */
    suspend fun write(bytes: ByteArray): Boolean

    /**
     * Writes a specified string to the connected serial port.
     * Useful for sending ASCII or UTF-encoded data to the device in text form.
     *
     * @param data The string to send to the serial port.
     * @return Boolean indicating if the write operation was successful.
     */
    suspend fun write(data: String): Boolean

    /**
     * Reads a string from the connected serial port.
     * Primarily used for receiving ASCII or UTF-encoded text from the serial device.
     *
     * @return The string data read from the serial port.
     */
    suspend fun readString(): String?

    /**
     * Reads a byte array from the connected serial port.
     * Enables reception of raw data from the device, particularly useful when
     * handling non-text-based protocols.
     *
     * @return The byte array data read from the serial port.
     */
    suspend fun readBytes(): ByteArray?

    /**
     * Sends a string request to the serial device and awaits a string response.
     * Combines write and read operations, allowing for a request-response pattern.
     *
     * @param request The string data to send to the serial port.
     * @return The response from the device as a string, or null if the operation fails.
     */
    suspend fun sendReceive(request: String): String?

    /**
     * Sends a byte array request to the serial device and awaits a byte array response.
     * Useful for raw data exchanges that require a request-response communication pattern.
     *
     * @param request The byte array data to send to the serial port.
     * @return The response from the device as a byte array, or null if the operation fails.
     */
    suspend fun sendReceive(request: ByteArray): ByteArray?

    /**
     * Exposes the connection status as a StateFlow to observe status changes.
     * @return A StateFlow<Boolean> representing the connection status.
     */
    fun getConnectionStatusFlow(): StateFlow<Boolean>

}
