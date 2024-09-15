package ru.gymbay.gelm.app.example.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.gymbay.gelm.app.R
import ru.gymbay.gelm.app.example.store.ExampleStore
import ru.gymbay.gelm.app.example.store.createExampleStore
import ru.gymbay.gelm.app.example.store.models.ExampleEffect
import ru.gymbay.gelm.app.example.store.models.ExampleEvent
import ru.gymbay.gelm.app.example.store.models.ExampleState


class ExampleFragment : Fragment() {

    private val store: ExampleStore by viewModels<ExampleStore> {
        Factory
    }

    private val title: TextView? by lazy { view?.findViewById(R.id.title) }
    private val textField: TextInputLayout? by lazy { view?.findViewById(R.id.textField) }
    private val recycler: RecyclerView? by lazy { view?.findViewById(R.id.items) }
    private val progress: ProgressBar? by lazy { view?.findViewById(R.id.progress) }
    private val btnReload: Button? by lazy { view?.findViewById(R.id.btnReload) }
    private val btnNext: Button? by lazy { view?.findViewById(R.id.btnNext) }

    private val adapter: Adapter by lazy { Adapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler?.adapter = adapter

        textField?.editText?.doOnTextChanged { text, _, _, _ ->
            store.sendEvent(ExampleEvent.TypeText(text?.toString() ?: ""))
        }
        btnNext?.setOnClickListener {
            store.sendEvent(ExampleEvent.Next)
        }
        btnReload?.setOnClickListener {
            store.sendEvent(ExampleEvent.Reload)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                store.state
                    .onEach(::onState)
                    .collect()
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                store.effect
                    .onEach(::onEffect)
                    .collect()
            }
        }
    }

    private fun onState(state: ExampleState) {
        recycler?.isVisible = !state.isLoading
        progress?.isVisible = state.isLoading
        title?.text = state.title

        val currentText = textField?.editText?.text?.toString()
        if (currentText != state.editField) {
            textField?.editText?.setText(state.editField)
        }

        adapter.submitList(state.items)
    }

    private fun onEffect(effect: ExampleEffect) {
        when (effect) {
            ExampleEffect.NavigateToScreen -> Toast.makeText(
                context,
                "Next screen!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private class Adapter : ListAdapter<String, ViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.example_holder, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            (holder as Holder).bind(getItem(position))
        }

        private class Holder(itemView: View) : ViewHolder(itemView) {
            private val tv: TextView = itemView.findViewById(R.id.text)

            fun bind(text: String) {
                tv.text = text
            }
        }

        companion object {
            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }

    companion object {
        fun newInstance() = ExampleFragment()

        private val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { createExampleStore(null) }
        }
    }
}