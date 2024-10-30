import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
private fun ConnectionView(modifier: Modifier, viewModel: MainViewModel) {
    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = "Ports",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center
        )
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.small
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                items(viewModel.ports) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clickable {
                                viewModel.selectedPort = it
                            },
                        text = if (it == viewModel.selectedPort) {
                            "$it                ✅"
                        } else {
                            it
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.connect()
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("CONNECT")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.disconnect()
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("DISCONNECT")
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.refreshPorts()
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("REFRESH")
            }
        }
    }
}

@Composable
private fun SendCommandView(modifier: Modifier, viewModel: MainViewModel) {
    var command by remember { mutableStateOf(TextFieldValue()) }
    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = "Send Commands",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center
        )
        ElevatedCard(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.small
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                value = command,
                onValueChange = {
                    command = it
                },
                label = {
                    Text("Enter Something ...")
                },
            )
            Row {
                FilterChip(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    selected = viewModel.isDataString,
                    label = {
                        Text("HEX")
                    },
                    onClick = {
                        viewModel.isDataString = true
                    }
                )
                FilterChip(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    selected = !viewModel.isDataString,
                    label = {
                        Text("STRING")
                    },
                    onClick = {
                        viewModel.isDataString = false
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (command.text.isNotEmpty()) {
                        viewModel.sendCommand(command.text)
                    } else {
                        viewModel.logs.add("Please enter valid command!")
                    }
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("SEND")
            }
        }
    }
}

@Composable
private fun LogsView(modifier: Modifier, viewModel: MainViewModel) {
    val listState = rememberLazyListState()
    LaunchedEffect(viewModel.logs.size) {
        if (viewModel.logs.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.logs.lastIndex)
        }
    }
    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = "Logs",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.small
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                state = listState
            ) {
                items(viewModel.logs) { log ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = log
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandHistory(modifier: Modifier, viewModel: MainViewModel) {
    val listState = rememberLazyListState()
    LaunchedEffect(viewModel.commands.size) {
        if (viewModel.commands.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.commands.lastIndex)
        }
    }
    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = "History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center
        )
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.small
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                state = listState
            ) {
                items(viewModel.commands) { command ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = if (command.type == MainViewModel.Command.Type.REQUEST) {
                            "REQUEST: ${command.data}"
                        } else {
                            "RESPONSE: ${command.data}"
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "KSerial UI",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            ConnectionView(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                viewModel = viewModel
            )
            Spacer(Modifier.padding(16.dp))
            SendCommandView(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                viewModel = viewModel
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            CommandHistory(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                viewModel = viewModel
            )
            Spacer(Modifier.padding(16.dp))
            LogsView(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                viewModel = viewModel
            )
        }
    }
}