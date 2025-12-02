import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dismesmots_client_poc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    // use your PC's IP or 10.0.2.2 for the emulator.
    private val SERVER_IP = "10.11.104.249"
    private val SERVER_PORT = 9999

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    private var writer: BufferedWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // find all our UI elements
        val questionTextView: TextView = findViewById(R.id.questionTextView)
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)
        val button4: Button = findViewById(R.id.button4)

        // Assign the click action to each button
        button1.setOnClickListener { sendResponse(button1.text.toString()) }
        button2.setOnClickListener { sendResponse(button2.text.toString()) }
        button3.setOnClickListener { sendResponse(button3.text.toString()) }
        button4.setOnClickListener { sendResponse(button4.text.toString()) }

        job = scope.launch {
            try {
                val socket = Socket(SERVER_IP, SERVER_PORT)
                val reader = socket.getInputStream().bufferedReader()
                // Store the writer so our button clicks can use it
                writer = socket.getOutputStream().bufferedWriter()

                // Loop to listen for incoming questions
                while (isActive) {
                    val questionFromServer = reader.readLine()
                    if (questionFromServer != null) {
                        withContext(Dispatchers.Main) {
                            questionTextView.text = questionFromServer
                        }
                    } else {
                        break // Server disconnected
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    questionTextView.text = "Error: ${e.message}"
                }
                e.printStackTrace()
            }
        }
    }

    // A helper function to send a message back to the server
    private fun sendResponse(message: String) {
        scope.launch {
            writer?.let {
                it.write(message)
                it.newLine()
                it.flush()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}