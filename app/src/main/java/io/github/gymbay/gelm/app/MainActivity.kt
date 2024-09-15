package io.github.gymbay.gelm.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.savedstate.SavedStateRegistry
import io.github.gymbay.gelm.app.example.store.createExampleStore
import io.github.gymbay.gelm.app.example.store.models.ExampleState
import io.github.gymbay.gelm.utils.GelmSavedStateHandler
import ru.gymbay.gelm.app.example.compose.ComposeScreen

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GELMTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val savedStateHandler = object : GelmSavedStateHandler<ExampleState> {
                        override fun saveState(state: ExampleState) {
                            val savedStateProvider = SavedStateRegistry.SavedStateProvider {
                                bundleOf(
                                    "state" to state
                                )
                            }
                            savedStateRegistry.unregisterSavedStateProvider("provider")
                            savedStateRegistry.registerSavedStateProvider(
                                "provider",
                                savedStateProvider
                            )
                        }

                        override fun restoreState(initialState: ExampleState): ExampleState? {
                            return savedStateRegistry.consumeRestoredStateForKey("provider")
                                ?.getParcelable("state")
                        }
                    }
                    val store = createExampleStore(savedStateHandler)
                    ComposeScreen(store = store)
                }
            }
        }
    }
}