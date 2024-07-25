package ru.gymbay.gelm.app.example.compose

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.gymbay.gelm.app.example.compose.store.ComposeStore
import ru.gymbay.gelm.app.example.compose.store.createComposeStore
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEffect
import ru.gymbay.gelm.app.example.compose.store.models.ComposeEvent
import ru.gymbay.gelm.app.example.compose.utils.CollectEffect
import ru.gymbay.gelm.app.ui.theme.GELMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    store: ComposeStore = createComposeStore()
) {
    val state by store.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectEffect(store.effect) { effect ->
        when (effect) {
            ComposeEffect.NavigateToScreen -> {
                Toast.makeText(context, "Button tapped!", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = state.title)
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
                    value = state.editField,
                    onValueChange = {
                        store.sendEvent(ComposeEvent.TypeText(it))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp, max = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    this@Column.AnimatedVisibility(visible = state.isLoading) {
                        CircularProgressIndicator()
                    }

                    this@Column.AnimatedVisibility(visible = !state.isLoading) {
                        LazyColumn {
                            items(state.items) {
                                Text(text = it)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        store.sendEvent(ComposeEvent.Reload)
                    }
                ) {
                    Text(text = "Reload")
                }

                Button(
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        store.sendEvent(ComposeEvent.Next)
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
fun ComposeScreenPreview() {
    GELMTheme {
        ComposeScreen()
    }
}