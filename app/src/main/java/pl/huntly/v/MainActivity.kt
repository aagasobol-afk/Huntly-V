package pl.huntly.v

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val darkGreen = Color.rgb(23, 58, 45)
    private val cream = Color.rgb(247, 243, 234)
    private val gold = Color.rgb(181, 139, 58)

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

        val scroll = ScrollView(this).apply {
            setBackgroundColor(cream)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 56, 32, 40)
        }

        val title = TextView(this).apply {
            text = "Huntly V"
            textSize = 32f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Ty pokazujesz. Huntly szuka."
            textSize = 17f
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 32)
        }

        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(subtitle, LinearLayout.LayoutParams(-1, -2))

        if (!sharedText.isNullOrBlank()) {
            val saved = TextView(this).apply {
                text = "✓  Inspiracja zapisana"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(darkGreen)
                setPadding(4, 8, 4, 14)
            }

            val latestCard = makeLatestCard(sharedText)
            root.addView(saved, LinearLayout.LayoutParams(-1, -2))
            root.addView(latestCard, marginParams(bottom = 22))
        } else if (savedInspirations().isEmpty()) {
            val empty = TextView(this).apply {
                text = "Udostępnij ofertę z Vinted do Huntly V.\n\nZ czasem zbierzemy tutaj rzeczy, które Ci się podobają."
                textSize = 17f
                setTextColor(darkGreen)
                setBackgroundColor(Color.WHITE)
                setPadding(24, 24, 24, 24)
            }
            root.addView(empty, marginParams(bottom = 22))
        }

        val inspirations = savedInspirations()

        val section = TextView(this).apply {
            text = "Twoje inspiracje  •  " + inspirations.size
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            setPadding(4, 8, 4, 16)
        }
        root.addView(section, LinearLayout.LayoutParams(-1, -2))

        inspirations.forEachIndexed { index, url ->
            root.addView(makeInspirationCard(index, url), marginParams(bottom = 14))
        }

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun makeLatestCard(url: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(24, 22, 24, 22)
        }

        val label = TextView(this).apply {
            text = "NOWA INSPIRACJA"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
        }

        val name = TextView(this).apply {
            text = prettyTitle(url)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            setPadding(0, 8, 0, 6)
        }

        val small = TextView(this).apply {
            text = "Vinted  •  " + shortUrl(url)
            textSize = 13f
            setTextColor(Color.DKGRAY)
        }

        val open = Button(this).apply {
            text = "OTWÓRZ OFERTĘ"
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        card.addView(label)
        card.addView(name)
        card.addView(small)
        card.addView(open, marginParams(top = 14))

        return card
    }

    private fun makeInspirationCard(index: Int, url: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(22, 20, 22, 20)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val number = TextView(this).apply {
            text = String.format("%02d", index + 1)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
        }

        val name = TextView(this).apply {
            text = prettyTitle(url)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            setPadding(12, 0, 0, 0)
        }

        top.addView(number, LinearLayout.LayoutParams(34, -2))
        top.addView(name, LinearLayout.LayoutParams(0, -2, 1f))

        val source = TextView(this).apply {
            text = "Vinted  •  " + shortUrl(url)
            textSize = 12f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 12)
        }

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val open = Button(this).apply {
            text = "OTWÓRZ"
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        val delete = Button(this).apply {
            text = "USUŃ"
            setOnClickListener {
                deleteInspiration(url)
                showSharedContent(Intent())
            }
        }

        actions.addView(open, LinearLayout.LayoutParams(0, -2, 1f))
        actions.addView(delete, LinearLayout.LayoutParams(0, -2, 1f))

        card.addView(top)
        card.addView(source)
        card.addView(actions)

        return card
    }

    private fun prettyTitle(url: String): String {
        val slug = url.substringAfterLast("/").substringBefore("?")
        if (slug.isBlank()) return "Zapisana rzecz"

        val words = slug
            .replace(Regex("^\\d+-"), "")
            .replace("-", " ")
            .replace("_", " ")
            .trim()

        if (words.isBlank()) return "Zapisana rzecz"

        return words
            .split(" ")
            .joinToString(" ") { word ->
                if (word.length <= 2) word.uppercase()
                else word.replaceFirstChar { it.uppercase() }
            }
            .take(70)
    }

    private fun shortUrl(url: String): String {
        val slug = url.substringAfterLast("/").substringBefore("?")
        return if (slug.length > 42) slug.take(42) + "…" else slug
    }

    private fun marginParams(
        top: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, top, 0, bottom)
        }
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
