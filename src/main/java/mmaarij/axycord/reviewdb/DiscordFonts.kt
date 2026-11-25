package mmaarij.axycord.reviewdb

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.discord.R

/** Resolve host resource IDs with host resources, never the plugin's package context. */
internal fun View.useDiscordFonts(host: Context) {
    fun font(attribute: Int, bundled: Int): Typeface {
        val value = TypedValue()
        if (host.theme.resolveAttribute(attribute, value, true) && value.resourceId != 0) {
            runCatching { ResourcesCompat.getFont(host, value.resourceId) }.getOrNull()?.let { return it }
        }
        return requireNotNull(ResourcesCompat.getFont(host, bundled)) { "Axycord's Discord font is unavailable" }
    }
    applyDiscordTypefaces(
        this,
        font(R.attr.font_primary_normal, R.font.whitney_medium),
        font(R.attr.font_primary_bold, R.font.whitney_bold),
        font(R.attr.font_primary_semibold, R.font.whitney_semibold),
    )
}

internal fun applyDiscordTypefaces(root: View, normal: Typeface, bold: Typeface, semibold: Typeface) {
    if (root is TextView) {
        val style = root.typeface?.style ?: Typeface.NORMAL
        val face = when {
            root is Button -> semibold
            style and Typeface.BOLD != 0 -> bold
            else -> normal
        }
        root.typeface = if (style and Typeface.ITALIC != 0) Typeface.create(face, Typeface.ITALIC) else face
    }
    if (root is ViewGroup) {
        for (index in 0 until root.childCount) applyDiscordTypefaces(root.getChildAt(index), normal, bold, semibold)
    }
}
