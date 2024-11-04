# KSerial

KSerial is a Kotlin-based, coroutine-friendly library built on top of [JSSC](https://github.com/scream3r/java-simple-serial-connector) (Java Simple Serial Connector) for efficient serial communication. Designed to handle robust data exchanges through features like auto-reconnect, configurable retries, and coroutine-based non-blocking IO, KSerial is ideal for applications requiring reliable serial communication over varied network conditions.

## Features

- **Automatic Reconnection**: Keeps the connection alive with retry mechanisms.
- **Flexible Configuration**: Highly customizable, allowing tailored data handling.
- **Coroutine-Friendly**: Optimized for non-blocking IO with Kotlin coroutines.
- **Support for String and Hex Data**: Handles multiple data formats with ease.

## Configuration Options

| Option             | Type            | Default Value          | Description |
|--------------------|-----------------|------------------------|-------------|
| **Port Name**      | `String`        | **Required**           | Serial port to connect (e.g., `"COM3"`). |
| **Baud Rate**      | `Int`           | 115200                 | Data transmission speed (bps). |
| **Data Bits**      | `Int`           | 8                      | Number of data bits. Options: 5, 6, 7, 8. |
| **Stop Bits**      | `Int`           | 1                      | Number of stop bits. Options: 1, 1.5, 2. |
| **Parity**         | `Int`           | `SerialPort.PARITY_NONE` | Parity mode. Options: NONE, EVEN, ODD, MARK, SPACE. |
| **Retry Delay**    | `Long` (ms)     | 3000                   | Time delay between retries. |
| **Read Delay**     | `Long` (ms)     | 100                    | Delay after writing to read data. |
| **Auto Reconnect** | `Boolean`       | `false`                | Reconnects on disconnect if enabled. |
| **Max Failure Count** | `Int`       | 3                      | Max retry attempts before reset. |
| **Logger**         | `(String) -> Unit` | `{ println(it) }`  | Custom logger for internal messaging. |

## Available Functions

### 1. `builder(port: String, logger: (String) -> Unit): KSerial.Builder`
- **Description**: Initiates the builder pattern to configure a new `KSerial` instance.
- **Parameters**:
  - `port`: The serial port to connect.
  - `logger`: A lambda function for logging messages.
- **Usage**:
  ```kotlin
  val serial = KSerial.builder("COM3") { message -> println(message) }.build()
  ```

### 2. `baudRate(rate: Int): Builder`
- **Description**: Sets the baud rate for the serial communication.
- **Parameters**:
  - `rate`: The baud rate (bps).
- **Usage**:
  ```kotlin
  serial.baudRate(9600)
  ```

### 3. `dataBits(bits: Int): Builder`
- **Description**: Configures the number of data bits.
- **Parameters**:
  - `bits`: Number of data bits (5, 6, 7, 8).
- **Usage**:
  ```kotlin
  serial.dataBits(8)
  ```

### 4. `stopBits(bits: Int): Builder`
- **Description**: Sets the number of stop bits.
- **Parameters**:
  - `bits`: Number of stop bits (1, 1.5, 2).
- **Usage**:
  ```kotlin
  serial.stopBits(1)
  ```

### 5. `parity(parity: Int): Builder`
- **Description**: Configures the parity mode.
- **Parameters**:
  - `parity`: Options include `PARITY_NONE`, `PARITY_EVEN`, `PARITY_ODD`, etc.
- **Usage**:
  ```kotlin
  serial.parity(SerialPort.PARITY_EVEN)
  ```

### 6. `retryDelay(delay: Long): Builder`
- **Description**: Sets the time delay between reconnection attempts.
- **Parameters**:
  - `delay`: Time in milliseconds.
- **Usage**:
  ```kotlin
  serial.retryDelay(2000L)
  ```

### 7. `readDelay(delay: Long): Builder`
- **Description**: Configures the delay after writing before reading data.
- **Parameters**:
  - `delay`: Delay time in milliseconds.
- **Usage**:
  ```kotlin
  serial.readDelay(150L)
  ```

### 8. `enableAutoReconnect(): Builder`
- **Description**: Enables automatic reconnection on disconnect.
- **Usage**:
  ```kotlin
  serial.enableAutoReconnect()
  ```

### 9. `maxFailureCount(count: Int): Builder`
- **Description**: Sets the maximum number of reconnection attempts before giving up.
- **Parameters**:
  - `count`: Max failure count.
- **Usage**:
  ```kotlin
  serial.maxFailureCount(5)
  ```

### 10. `start()`
- **Description**: Opens the serial port and starts listening for incoming data.
- **Usage**:
  ```kotlin
  serial.start()
  ```

### 11. `write(data: String)`
- **Description**: Sends a string of data through the serial port.
- **Parameters**:
  - `data`: The string data to send.
- **Usage**:
  ```kotlin
  serial.write("Hello, World!")
  ```

### 12. `write(data: ByteArray)`
- **Description**: Sends a byte array (hex) through the serial port.
- **Parameters**:
  - `data`: The byte array to send.
- **Usage**:
  ```kotlin
  serial.write(byteArrayOf(0x01, 0x02, 0x03))
  ```

### 13. `readString(): String`
- **Description**: Reads data from the serial port and returns it as a string.
- **Returns**: The received string.
- **Usage**:
  ```kotlin
  val response = serial.readString()
  ```

### 14. `readBytes(): ByteArray`
- **Description**: Reads raw bytes from the serial port.
- **Returns**: The received byte array.
- **Usage**:
  ```kotlin
  val responseBytes = serial.readBytes()
  ```

### 15. `isConnected(): Boolean`
- **Description**: Checks if the serial port is currently connected.
- **Returns**: `true` if connected, otherwise `false`.
- **Usage**:
  ```kotlin
  if (serial.isConnected()) {
      println("Port is connected.")
  }
  ```

### 16. `close()`
- **Description**: Closes the serial port connection.
- **Usage**:
  ```kotlin
  serial.close()
  ```

## Complete Example

Here's a complete example demonstrating how to set up and use the `KSerial` library to send and receive data:

```kotlin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch

fun main() = runBlocking {
    // Create a KSerial instance with desired configuration
    val serial = KSerial.builder("COM3") { message -> println("Log: $message") }
        .baudRate(9600)
        .dataBits(8)
        .stopBits(1)
        .parity(SerialPort.PARITY_NONE)
        .enableAutoReconnect()
        .maxFailureCount(5)
        .build()

    // Start the serial connection
    serial.start()

    // Sending data as a string
    serial.write("Hello Device!")

    // Reading response from the device
    val response = serial.readString()
    println("Received: $response")

    // Sending data as hex
    serial.write(byteArrayOf(0x01, 0x02, 0x03))

    // Closing the serial connection
    serial.close()
}
```

In this example, the program configures a serial connection to `"COM3"`, sends a string message to a connected device, reads the response, and also sends a byte array in hexadecimal format. The connection will automatically attempt to reconnect if disconnected.

## Contribution Guide

We welcome contributions to improve KSerial! Here's how you can help:

1. **Fork and Clone**: Fork the repo and create a new branch for your feature or bug fix.
2. **Write Tests**: Ensure changes are backed by unit tests, and existing tests are passing.
3. **Create a Pull Request**: Submit your changes for review, with details on the improvements made.

For large contributions, please open an issue for discussion.

---

With this setup, developers gain powerful, reliable serial communication, making KSerial a perfect fit for real-time embedded systems, IoT applications, and beyond.
