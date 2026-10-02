package pl.huntly.v

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class MainActivity : AppCompatActivity() {

    private val darkGreen = Color.rgb(23, 58, 45)
    private val cream = Color.rgb(247, 243, 234)
    private val warmWhite = Color.rgb(255, 253, 248)
    private val gold = Color.rgb(181, 139, 58)
    private val muted = Color.rgb(108, 108, 98)
    private val line = Color.rgb(225, 219, 207)

    private lateinit var searchInput: EditText
    private lateinit var sizeSpinner: Spinner
    private lateinit var colorSpinner: Spinner

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Some providers do not support persistable permissions.
            }
            saveInspiration(it.toString())
            showSharedContent(Intent())
        }
    }

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
        root.addView(searchBox(), marginParams(top = 24, bottom = 10))
        root.addView(filterRow(), marginParams(bottom = 14))
        root.addView(showItemButton(), marginParams(bottom = 10))
        root.addView(addPhotoButton(), marginParams(bottom = 28))

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

    private fun filterRow(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        sizeSpinner = filterSpinner(
            listOf("Rozmiar", "XS", "S", "M", "L", "XL", "XXL", "34", "36", "38", "40", "42", "44")
        )

        colorSpinner = filterSpinner(
            listOf(
                "Kolor",
                "czarny",
                "biały",
                "szary",
                "beż / cappuccino",
                "camel",
                "taupe / greige",
                "brązowy",
                "granatowy",
                "niebieski",
                "zielony",
                "khaki / oliwkowy",
                "czerwony",
                "różowy",
                "fioletowy",
                "pomarańczowy",
                "żółty",
                "wielokolorowy"
            )
        )

        row.addView(sizeSpinner, LinearLayout.LayoutParams(0, dp(46), 1f))
        row.addView(colorSpinner, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            setMargins(dp(8), 0, 0, 0)
        })

        return row
    }

    private fun filterSpinner(values: List<String>): Spinner {
        val spinner = Spinner(this).apply {
            background = rounded(warmWhite, 16f, line)
            setPadding(dp(12), 0, dp(8), 0)
        }

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            values
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.textSize = 14f
                view.setTextColor(if (position == 0) muted else darkGreen)
                view.setPadding(dp(10), 0, dp(4), 0)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        return spinner
    }

    private fun addPhotoButton(): View {
        return Button(this).apply {
            text = "＋  DODAJ ZDJĘCIE INSPIRACJI"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            isAllCaps = false
            background = rounded(warmWhite, 18f, line)
            setOnClickListener {
                pickImage.launch(arrayOf("image/*"))
            }
        }
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
        val size = sizeSpinner.selectedItem?.toString().orEmpty()
        val color = colorSpinner.selectedItem?.toString().orEmpty()

        if (query.isBlank()) {
            Toast.makeText(
                this,
                "Najpierw wpisz, czego szukasz.",
                Toast.LENGTH_SHORT
            ).show()
            searchInput.requestFocus()
            return
        }

        val filters = buildList {
            if (size.isNotBlank() && size != "Rozmiar") add("rozmiar $size")
            if (color.isNotBlank() && color != "Kolor") {
                val colorTerms = when (color) {
                    "beż / cappuccino" -> "beżowy cappuccino nude"
                    "camel" -> "camel karmelowy jasny brąz"
                    "taupe / greige" -> "taupe greige szarobeżowy"
                    "khaki / oliwkowy" -> "khaki oliwkowy olive"
                    "wielokolorowy" -> "wielokolorowe"
                    else -> color
                }
                add("kolor $colorTerms")
            }
        }

        val finalQuery = listOf(query, *filters.toTypedArray()).joinToString(" ")

        val searchUri = Uri.Builder()
            .scheme("https")
            .authority("www.vinted.pl")
            .path("catalog")
            .appendQueryParameter("search_text", finalQuery)
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

        val image = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_menu_camera)
            setColorFilter(Color.rgb(155, 148, 134))
        }

        visual.addView(image, LinearLayout.LayoutParams(-1, dp(250)))
        loadInspirationImage(url, image)

        val isPhoto = url.startsWith("content://")

        val meta = TextView(this).apply {
            text = "INSPIRACJA  " + String.format("%02d", index + 1) + "   •   " + if (isPhoto) "ZDJĘCIE" else "VINTED"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(gold)
            setPadding(dp(4), dp(16), dp(4), dp(5))
        }

        val name = TextView(this).apply {
            text = if (isPhoto) "Zdjęcie inspiracji" else prettyTitle(url)
            textSize = 20f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(darkGreen)
            setPadding(dp(4), 0, dp(4), dp(5))
            maxLines = 2
        }

        val hint = TextView(this).apply {
            text = if (isPhoto) "Pokaż Huntly, co Ci się podoba — później poszukamy czegoś podobnego" else "Zapamiętane jako punkt odniesienia dla Twojego stylu"
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

        if (isPhoto) {
            open.text = "OTWÓRZ ZDJĘCIE"
        }

        actions.addView(open, LinearLayout.LayoutParams(0, dp(48), 1f))
        actions.addView(delete, LinearLayout.LayoutParams(dp(86), dp(48)).apply {
            setMargins(dp(8), 0, 0, 0)
        })

        card.addView(visual)
        card.addView(meta)
        card.addView(name)
        card.addView(hint)

        if (isPhoto) {
            val analysis = TextView(this).apply {
                text = "Analizuję inspirację…"
                textSize = 13f
                setTextColor(muted)
                setPadding(dp(4), 0, dp(4), dp(12))
            }
            card.addView(analysis)
            analyzePhotoInspiration(url, analysis, searchInput)
        }

        card.addView(actions)

        return card
    }

    private fun analyzePhotoInspiration(source: String, resultView: TextView, searchField: EditText) {
        if (!source.startsWith("content://")) return

        Thread {
            try {
                val bitmap = contentResolver.openInputStream(Uri.parse(source)).use {
                    BitmapFactory.decodeStream(it)
                } ?: return@Thread

                val image = InputImage.fromBitmap(bitmap, 0)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        val useful = labels
                            .sortedByDescending { it.confidence }
                            .map { it.text.lowercase() }
                            .filter { it.length > 2 }
                            .take(6)

                        val color = dominantColorName(bitmap)
                        val fashionTerms = useful.mapNotNull { label ->
                            when {
                                "trouser" in label || "pants" in label -> "spodnie"
                                "suit" in label -> "garnitur komplet"
                                "jacket" in label || "blazer" in label -> "marynarka"
                                "shirt" in label -> "koszula"
                                "dress" in label -> "sukienka"
                                "coat" in label -> "płaszcz"
                                "clothing" in label || "apparel" in label -> "ubranie"
                                "fashion" in label -> "moda"
                                else -> null
                            }
                        }.distinct()

                        val baseQuery = (fashionTerms + listOfNotNull(color)).distinct().joinToString(" ")
                        val shown = if (baseQuery.isBlank()) {
                            "Zdjęcie zapisane. Dodaj cechy fasonu, a Huntly zbuduje lepsze wyszukiwanie."
                        } else {
                            "Rozpoznano: $baseQuery"
                        }

                        runOnUiThread {
                            resultView.text = shown
                            if (baseQuery.isNotBlank()) {
                                searchField.setText(baseQuery)
                            }
                            addFashionRefinementChips(
                                resultView,
                                searchField,
                                baseQuery,
                                color
                            )
                        }
                        labeler.close()
                    }
                    .addOnFailureListener {
                        runOnUiThread {
                            resultView.text = "Zdjęcie zapisane. Analiza nie była dostępna."
                        }
                        labeler.close()
                    }
            } catch (_: Exception) {
                runOnUiThread {
                    resultView.text = "Zdjęcie zapisane. Analiza nie była dostępna."
                }
            }
        }.start()
    }

    private fun addFashionRefinementChips(
        resultView: TextView,
        searchField: EditText,
        baseQuery: String,
        detectedColor: String?
    ) {
        val parent = resultView.parent as? LinearLayout ?: return

        val title = TextView(this).apply {
            text = "Dopasuj fason (1–3 kliknięcia)"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkGreen)
            setPadding(dp(4), 0, dp(4), dp(8))
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val options = listOf(
            "szerokie nogawki",
            "wysoki stan",
            "luźny krój",
            "prosta nogawka",
            "garniturowe",
            "w paski"
        )

        options.forEach { option ->
            val chip = Button(this).apply {
                text = option
                textSize = 11f
                isAllCaps = false
                setTextColor(darkGreen)
                background = rounded(warmWhite, 50f, line)
                setPadding(dp(10), 0, dp(10), 0)
            }

            chip.setOnClickListener {
                val current = searchField.text?.toString()?.trim().orEmpty()
                if (!current.split(" ").contains(option)) {
                    searchField.setText(
                        listOf(current, option)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                    )
                }
                chip.background = rounded(Color.rgb(232, 239, 231), 50f, darkGreen)
                chip.setTextColor(darkGreen)
                resultView.text = "Zapytanie dopasowane: " + searchField.text.toString()
            }

            row.addView(
                chip,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(38)
                ).apply {
                    setMargins(0, 0, dp(7), 0)
                }
            )
        }

        val scroll = ScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(row)
        }

        val index = parent.indexOfChild(resultView)
        parent.addView(title, index + 1)
        parent.addView(scroll, index + 2)

        // Keep this conservative: the generic ML model can detect clothing
        // and broad categories, but shape attributes are offered as explicit
        // visual refinements instead of being guessed.
        if (!detectedColor.isNullOrBlank() && baseQuery.isBlank()) {
            searchField.setText(detectedColor)
        }
    }

    private fun dominantColorName(bitmap: android.graphics.Bitmap): String? {
        val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 1, 1, true)
        val pixel = scaled.getPixel(0, 0)
        scaled.recycle()
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val avg = (r + g + b) / 3

        return when {
            avg < 45 -> "czarny"
            avg > 235 && max - min < 18 -> "biały"
            max - min < 22 && avg < 105 -> "szary ciemny"
            max - min < 22 -> "szary"
            r > 145 && g > 110 && b < 100 && r - b > 55 -> "camel beż brąz"
            r > g + 35 && r > b + 35 -> "czerwony"
            g > r + 25 && g > b + 15 -> "zielony"
            b > r + 25 && b > g + 10 -> "niebieski"
            r > 170 && g > 120 && b > 80 -> "beżowy"
            else -> null
        }
    }

    private fun loadInspirationImage(source: String, imageView: ImageView) {
        if (source.startsWith("content://")) {
            Thread {
                try {
                    val bitmap = contentResolver.openInputStream(Uri.parse(source)).use {
                        BitmapFactory.decodeStream(it)
                    }
                    if (bitmap != null) {
                        runOnUiThread {
                            imageView.clearColorFilter()
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                } catch (_: Exception) {
                    // Keep the placeholder if the local image is unavailable.
                }
            }.start()
            return
        }

        Thread {
            try {
                val connection = (URL(source).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    instanceFollowRedirects = true
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36"
                    )
                    setRequestProperty("Accept-Language", "pl-PL,pl;q=0.9,en;q=0.8")
                }

                val html = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val imageUrl = extractOgImage(html) ?: return@Thread

                val imageConnection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    instanceFollowRedirects = true
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36"
                    )
                }

                val bitmap = imageConnection.inputStream.use { BitmapFactory.decodeStream(it) }
                imageConnection.disconnect()

                if (bitmap != null) {
                    runOnUiThread {
                        imageView.clearColorFilter()
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (_: Exception) {
                // Keep the placeholder if Vinted blocks the request.
            }
        }.start()
    }

    private fun extractOgImage(html: String): String? {
        val patterns = listOf(
            Regex("""<meta[^>]+property=["']og:image["'][^>]+content=["']([^"']+)["'][^>]*>""", RegexOption.IGNORE_CASE),
            Regex("""<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image["'][^>]*>""", RegexOption.IGNORE_CASE)
        )

        val raw = patterns.firstNotNullOfOrNull { it.find(html)?.groupValues?.getOrNull(1) }
            ?: return null

        return raw
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#x2F;", "/")
    }

    private fun prettyTitle(url: String): String {
        val slug = url.substringAfterLast("/").substringBefore("?")
        if (slug.isBlank()) return "Zapisana rzecz"

        val words = slug
            .replace(Regex("""^\d+-"""), "")
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
