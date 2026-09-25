package pl.huntly.v

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSharedContent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showSharedContent(intent)
    }

    private fun showSharedContent(intent: Intent) {
        val sharedText = if (intent.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(48, 80, 48, 48)
            setBackgroundColor(Color.rgb(247, 243, 234))
        }

        val title = TextView(this).apply {
            text = "Huntly V"
            textSize = 30f
            setTextColor(Color.rgb(23, 58, 45))
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Ty pokazujesz. Huntly szuka."
            textSize = 16f
            setTextColor(Color.rgb(23, 58, 45))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 40)
        }

        val link = TextView(this).apply {
            text = sharedText ?: "Udostępnij ofertę z Vinted do Huntly V."
            textSize = 17f
            setTextColor(Color.rgb(23, 58, 45))
            setBackgroundColor(Color.WHITE)
            setPadding(28, 28, 28, 28)
        }

        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(subtitle, LinearLayout.LayoutParams(-1, -2))
        root.addView(link, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
    }
}
