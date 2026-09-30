package pl.huntly.v

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val darkGreen = Color.rgb(23, 58, 45)
    private val cream = Color.rgb(247, 243, 234)
    private val warmWhite = Color.rgb(255, 253, 248)
    private val gold = Color.rgb(181, 139, 58)
    private val muted = Color.rgb(108, 108, 98)
    private val line = Color.rgb(225, 219, 207)

    private lateinit var searchInput: EditText

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
        } else null

        if (!sharedText.isNullOrBlank()) saveInspiration(sharedText)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(cream)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(34), dp(22), dp(42))
        }

        root.addView(header())
        root.addView(searchBox(), marginParams(top = 24, bottom = 14))
        root.addView(showItemButton(), marginParams(bottom = 28))

        val inspirations = savedInspirations()

        if (!sharedText.isNullOrBlank()) {
            root.addView(
                statusPill("✓  Inspiracja dodana do Twojej kolekcji"),
                marginParams(bottom = 20)
            )
        }

        val sectionRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val section = TextView(this).apply {
            text = "Twoje inspiracje"
            textSize = 22f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(darkGreen)
        }

        val count = TextView(this).apply {
            text = inspirations.size.toString()
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
            background = rounded(gold, 50f)
            setPadding(dp(10), dp(4), dp(10), dp(4))
        }

        sectionRow.addView(section, LinearLayout.LayoutParams(0, -2, 1f))
        sectionRow.addView(count)
        root.addView(sectionRow, marginParams(bottom = 14))

        if (inspirations.isEmpty()) {
            root.addView(emptyState())
        } else {
            inspirations.forEachIndexed { index, url ->
                root.addView(
                    makeInspirationCard(index, url),
                    marginParams(bottom = 16)
                )
            }
        }

        val footer = TextView(this).apply {
            text = "Ty pokazujesz. Huntly zapamiętuje.\nZ czasem nauczy się Twojego stylu."
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(22), dp(8), 0)
        }
        root.addView(footer)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun header(): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Huntly V"
            textSize = 38f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Ty pokazujesz. Huntly szuka."
            textSize = 16f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, 0)
        }

        box.addView(title)
        box.addView(subtitle)
        return box
    }

    private fun searchBox(): View {
        searchInput = EditText(this).apply {
            hint = "Czego szukasz?"
            textSize = 16f
            setTextColor(darkGreen)
            setHintTextColor(Color.rgb(150, 146, 137))
            setSingleLine(true)
            setPadding(dp(18), 0, dp(18), 0)
            background = rounded(warmWhite, 18f, line)
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
        }
        return searchInput
    }

    private fun showItemButton(): View {
        return Button(this).apply {
            text = "＋  POKAŻ MI RZECZ"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(warmWhite)
            isAllCaps = false
            background = rounded(darkGreen, 18f)
            setOnClickListener {
                searchVinted()
            }
        }
    }

    private fun searchVinted() {
        val query = searchInput.text?.toString()?.trim().orEmpty()

        if (query.isBlank()) {
            Toast.makeText(
                this,
                "Najpierw wpisz, czego szukasz.",
                Toast.LENGTH_SHORT
            ).show()
            searchInput.requestFocus()
            return
        }

        val searchUri = Uri.Builder()
            .scheme("https")
            .authority("www.vinted.pl")
            .path("catalog")
            .appendQueryParameter("search_text", query)
            .build()

        startActivity(Intent(Intent.ACTION_VIEW, searchUri))
    }

    private fun statusPill(text: String): View {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            background = rounded(Color.rgb(232, 239, 231), 50f)
            setPadding(dp(14), dp(9), dp(14), dp(9))
        }
    }

    private fun emptyState(): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(34), dp(24), dp(34))
            background = rounded(warmWhite, 22f, line)
        }

        val icon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            setColorFilter(darkGreen)
        }

        val title = TextView(this).apply {
            text = "Zacznij od jednej rzeczy"
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(6))
        }

        val body = TextView(this).apply {
            text = "Pokaż Huntly rzecz, która Ci się podoba.\nTo będzie pierwszy element Twojego stylu."
            textSize = 14f
            setTextColor(muted)
            gravity = Gravity.CENTER
        }

        box.addView(icon, LinearLayout.LayoutParams(dp(38), dp(38)))
        box.addView(title)
        box.addView(body)
        return box
    }

    private fun makeInspirationCard(index: Int, url: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(warmWhite, 22f, line)
            setPadding(dp(12), dp(12), dp(12), dp(14))
        }

        val visual = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(241, 237, 228), 18f)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            minimumHeight = dp(190)
        }

        val camera = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            setColorFilter(Color.rgb(155, 148, 134))
        }

        val visualText = TextView(this).apply {
            text = "TU BĘDZIE ZDJĘCIE RZECZY"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(155, 148, 134))
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }

        visual.addView(camera, LinearLayout.LayoutParams(dp(48), dp(48)))
        visual.addView(visualText)

        val meta = TextView(this).apply {
            text = "INSPIRACJA  " + String.format("%02d", index + 1) + "   •   VINTED"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(dp(4), dp(16), dp(4), dp(5))
        }

        val name = TextView(this).apply {
            text = prettyTitle(url)
            textSize = 20f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(darkGreen)
            setPadding(dp(4), 0, dp(4), dp(5))
            maxLines = 2
        }

        val hint = TextView(this).apply {
            text = "Zapamiętane jako punkt odniesienia dla Twojego stylu"
            textSize = 13f
            setTextColor(muted)
            setPadding(dp(4), 0, dp(4), dp(12))
        }

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val open = Button(this).apply {
            text = "ZOBACZ OFERTĘ"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(warmWhite)
            isAllCaps = false
            background = rounded(darkGreen, 16f)
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        val delete = Button(this).apply {
            text = "Usuń"
            textSize = 12f
            setTextColor(muted)
            isAllCaps = false
            background = rounded(Color.TRANSPARENT, 16f, line)
            setOnClickListener {
                deleteInspiration(url)
                showSharedContent(Intent())
            }
        }

        actions.addView(open, LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(delete, LinearLayout.LayoutParams(dp(86), dp(48)).apply {
            setMargins(dp(8), 0, 0, 0)
        })

        card.addView(visual)
        card.addView(meta)
        card.addView(name)
        card.addView(hint)
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

        return words.split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                if (word.length <= 2) word.uppercase()
                else word.replaceFirstChar { it.uppercase() }
            }
            .take(90)
    }

    private fun rounded(fill: Int, radius: Float, stroke: Int? = null): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radius)
            if (stroke != null) setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    private fun marginParams(top: Int = 0, bottom: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(top), 0, dp(bottom))
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
