package pl.huntly.v

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences("huntly_v", MODE_PRIVATE)
    }

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
            intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()
        } else {
            null
        }

        if (!sharedText.isNullOrBlank()) {
            saveInspiration(sharedText)
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
            setPadding(0, 20, 0, 28)
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

        if (!sharedText.isNullOrBlank()) {
            val saved = TextView(this).apply {
                text = "✓ Oferta zapisana w Twoich inspiracjach"
                textSize = 15f
                setTextColor(Color.rgb(23, 58, 45))
                setPadding(0, 24, 0, 12)
            }

            val openButton = Button(this).apply {
                text = "Otwórz ofertę na Vinted"
                setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sharedText)))
                }
            }

            root.addView(saved, LinearLayout.LayoutParams(-1, -2))
            root.addView(openButton, LinearLayout.LayoutParams(-1, -2))
        }

        val inspirations = savedInspirations()
        val count = inspirations.size

        val history = TextView(this).apply {
            text = "Twoje inspiracje: " + count
            textSize = 16f
            setTextColor(Color.rgb(23, 58, 45))
            setPadding(0, 32, 0, 8)
        }

        root.addView(history, LinearLayout.LayoutParams(-1, -2))

        if (inspirations.isNotEmpty()) {
            val listTitle = TextView(this).apply {
                text = "Zapisane oferty"
                textSize = 18f
                setTextColor(Color.rgb(23, 58, 45))
                setPadding(0, 20, 0, 12)
            }
            root.addView(listTitle, LinearLayout.LayoutParams(-1, -2))

            inspirations.forEachIndexed { index, url ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(Color.WHITE)
                    setPadding(24, 20, 24, 20)
                }

                val number = TextView(this).apply {
                    text = "Inspiracja " + (index + 1)
                    textSize = 14f
                    setTextColor(Color.rgb(23, 58, 45))
                }

                val itemLink = TextView(this).apply {
                    text = url
                    textSize = 14f
                    setTextColor(Color.rgb(23, 58, 45))
                    setPadding(0, 8, 0, 12)
                }

                val actions = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val open = Button(this).apply {
                    text = "Otwórz"
                    setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                }

                val delete = Button(this).apply {
                    text = "Usuń"
                    setOnClickListener {
                        deleteInspiration(url)
                        showSharedContent(Intent())
                    }
                }

                actions.addView(open, LinearLayout.LayoutParams(0, -2, 1f))
                actions.addView(delete, LinearLayout.LayoutParams(0, -2, 1f))

                card.addView(number, LinearLayout.LayoutParams(-1, -2))
                card.addView(itemLink, LinearLayout.LayoutParams(-1, -2))
                card.addView(actions, LinearLayout.LayoutParams(-1, -2))

                val cardParams = LinearLayout.LayoutParams(-1, -2)
                cardParams.setMargins(0, 0, 0, 16)
                root.addView(card, cardParams)
            }
        }

        setContentView(root)
    }

    private fun saveInspiration(url: String) {
        val current = savedInspirations().toMutableList()
        current.remove(url)
        current.add(0, url)

        val limited = current.take(20)

        prefs.edit()
            .putString("inspirations_ordered", limited.joinToString("\n"))
            .putStringSet("inspirations", limited.toSet())
            .putString("latest", url)
            .apply()
    }

    private fun deleteInspiration(url: String) {
        val remaining = savedInspirations().filter { it != url }.take(20)

        prefs.edit()
            .putString("inspirations_ordered", remaining.joinToString("\n"))
            .putStringSet("inspirations", remaining.toSet())
            .putString("latest", remaining.firstOrNull())
            .apply()
    }

    private fun savedInspirations(): List<String> {
        val ordered = prefs.getString("inspirations_ordered", null)

        if (!ordered.isNullOrBlank()) {
            return ordered.lines().filter { it.isNotBlank() }
        }

        val latest = prefs.getString("latest", null)
        val set = prefs.getStringSet("inspirations", emptySet()) ?: emptySet()

        return buildList {
            if (latest != null) add(latest)
            addAll(set.filter { it != latest })
        }
    }
}
