package bg.dalexiev.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as ToDoApplication).appContainer

        setContent {
            App(appContainer) {
                moveTaskToBack(false)
            }
        }
    }
}