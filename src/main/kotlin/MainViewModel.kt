import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var kSerial: KSerial? = null

    var ports = mutableStateListOf<String>()
    var logs = mutableStateListOf<String>()
    var commands = mutableStateListOf<Command>()
    var selectedPort by mutableStateOf("")

    init {
        refreshPorts()
    }

    fun refreshPorts() {
        scope.launch {
            withLoading {
                ports.clear()
                KSerial
                    .getPorts()
                    .forEach {
                        ports.add(it)
                    }
            }
        }
    }

    fun connect() = scope.launch {
        withLoading {
            if (selectedPort.isEmpty())
                error("Please select serial port!")
            kSerial = KSerial.builder(
                selectedPort
            ) {
                logs.add(it)
            }
                .enableAutoReconnect()
                .build()
            kSerial?.start()
        }
    }

    fun disconnect() = scope.launch {
        logs.add("closing the ports please wait!")
        kSerial?.stop()
        kSerial = null
    }

    fun sendCommand(data: String) = scope.launch {
        commands.add(
            Command(
                Command.Type.REQUEST,
                data
            )
        )
        val response = kSerial?.sendReceive(data) ?: ""
        commands.add(
            Command(
                Command.Type.RESPONSE,
                response
            )
        )
    }

    data class Command(
        val type: Type,
        val data: String
    ) {
        enum class Type {
            REQUEST,
            RESPONSE
        }
    }

    private suspend fun withLoading(
        block: suspend () -> Unit
    ) {
        try {
            block()
        } catch (e: Exception) {
            logs.add(e.message ?: e.stackTraceToString())
        }
    }

}