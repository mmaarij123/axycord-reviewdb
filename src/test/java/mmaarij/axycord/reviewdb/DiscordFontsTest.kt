package mmaarij.axycord.reviewdb

import android.graphics.Typeface
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class DiscordFontsTest {
    // Stand-in faces test propagation; production loads only Axycord font resources.
    private val normal = Typeface.create("test-discord-normal", Typeface.NORMAL)
    private val bold = Typeface.create("test-discord-bold", Typeface.BOLD)
    private val semibold = Typeface.create("test-discord-semibold", Typeface.NORMAL)

    @Test fun `font reaches nested input fields labels and buttons`() {
        val context = RuntimeEnvironment.getApplication()
        val root = LinearLayout(context)
        val nested = FrameLayout(context)
        val input = EditText(context)
        val label = TextView(context)
        val button = Button(context)
        nested.addView(input)
        root.addView(nested)
        root.addView(label)
        root.addView(button)
        applyDiscordTypefaces(root, normal, bold, semibold)
        assertSame(normal, input.typeface)
        assertSame(normal, label.typeface)
        assertSame(semibold, button.typeface)
    }

    @Test fun `headings keep bold and italic labels keep emphasis`() {
        val context = RuntimeEnvironment.getApplication()
        val root = LinearLayout(context)
        val heading = TextView(context).apply { typeface = bold }
        val caption = TextView(context).apply { typeface = Typeface.create(normal, Typeface.ITALIC) }
        root.addView(heading)
        root.addView(caption)
        applyDiscordTypefaces(root, normal, bold, semibold)
        assertSame(bold, heading.typeface)
        assertEquals(Typeface.ITALIC, caption.typeface.style and Typeface.ITALIC)
    }
}
