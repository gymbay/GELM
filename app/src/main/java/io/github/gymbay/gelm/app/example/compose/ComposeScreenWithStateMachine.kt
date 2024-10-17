package io.github.gymbay.gelm.app.example.compose

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.gymbay.gelm.app.GELMTheme
import io.github.gymbay.gelm.app.example.compose.utils.CollectEffect
import io.github.gymbay.gelm.app.example.store.ExampleStateMachineStore
import io.github.gymbay.gelm.app.example.store.createExampleStateMachineStore
import io.github.gymbay.gelm.app.example.store.models.ExampleEffect
import io.github.gymbay.gelm.app.example.store.models.ExampleEvent
import io.github.gymbay.gelm.app.example.store.models.ExampleStateMachine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreenWithStateMachine(
    store: ExampleStateMachineStore = createExampleStateMachineStore()
) {
    val state by store.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEffect(store.effect) { effect ->
        when (effect) {
            ExampleEffect.NavigateToScreen -> {
                Toast.makeText(context, "Button tapped!", Toast.LENGTH_LONG).show()
            }
        }
    }

    val titleAndEditField by remember {
        derivedStateOf {
            return@derivedStateOf when (val s = state) {
                is ExampleStateMachine.Idle -> Pair(s.title, s.editField)
                is ExampleStateMachine.Fetching -> Pair(s.title, s.editField)
            }
        }
    }

    val isLoading by remember {
        derivedStateOf {
            state is ExampleStateMachine.Fetching
        }
    }

    val items by remember {
        derivedStateOf {
            return@derivedStateOf when (val s = state) {
                is ExampleStateMachine.Idle -> s.items
                is ExampleStateMachine.Fetching -> emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = titleAndEditField.first)
            })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = titleAndEditField.second,
                    onValueChange = {
                        store.sendEvent(ExampleEvent.TypeText(it))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp, max = 200.dp),
                    contentAlignment = Alignment.Center
                ) {


                    this@Column.AnimatedVisibility(visible = isLoading) {
                        CircularProgressIndicator()
                    }

                    this@Column.AnimatedVisibility(visible = !isLoading) {
                        LazyColumn {
                            items(items) {
                                Text(text = it)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        store.sendEvent(ExampleEvent.Reload)
                    }
                ) {
                    Text(text = "Reload")
                }

                Button(
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        store.sendEvent(ExampleEvent.Next)
                    }
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@Preview
@Composable
fun ComposeScreenWithStateMachinePreview() {
    GELMTheme {
        ComposeScreenWithStateMachine()
    }
}