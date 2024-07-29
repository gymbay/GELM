package ru.gymbay.gelm.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import ru.gymbay.gelm.app.example.view.ExampleFragment

class FragmentMainActivity : AppCompatActivity(R.layout.activity_fragment_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.commit {
            replace<ExampleFragment>(R.id.fragmentContainer)
        }
    }
}