package mmaarij.axycord.reviewdb

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import mmaarij.axycord.plugins.api.AxycordPlugin
import mmaarij.axycord.plugins.api.PluginContextMenuSurface
import mmaarij.axycord.plugins.api.PluginScope
import mmaarij.axycord.plugins.api.PluginSettingsPage
import mmaarij.axycord.plugins.api.pluginContextMenuItem
import mmaarij.axycord.plugins.api.pluginProfileTabContribution
import mmaarij.axycord.plugins.api.pluginSettings
import mmaarij.axycord.reviewdb.databinding.ReviewdbPageBinding
import mmaarij.axycord.reviewdb.databinding.ReviewdbSettingsBinding
import rx.Completable

class ReviewDbPlugin : AxycordPlugin {
    private lateinit var scope: PluginScope
    private lateinit var client: ReviewDbClient

    override val settingsSchema = pluginSettings {
        section("ReviewDB")
        text("token", "ReviewDB token", "Paste a token only if browser authorization cannot complete.")
        toggle("disableAds", "Disable ReviewDB ads", default = false)
        toggle("disableWarnings", "Disable ReviewDB warnings", default = false)
        action("authorize", "Authorize ReviewDB") { authorize(scope, client) }
    }

    override val settingsPage = PluginSettingsPage { context, _ ->
        val binding = ReviewdbSettingsBinding.inflate(requireNotNull(scope.resources.inflater))
        binding.root.useDiscordFonts(context)
        val palette = ReviewDbPalette.from(context)
        binding.reviewdbAuthStatus.setTextColor(palette.primary)
        binding.reviewdbBlocks.setTextColor(palette.muted)
        binding.reviewdbAuthStatus.text = if (scope.settings.getString(TOKEN_KEY).isBlank()) "Not authorized" else "Authorized"
        binding.reviewdbAuthorize.setOnClickListener { authorize(scope, client) }
        client.execute {
            val blocks = client.blockedUsers()
            binding.root.post {
                binding.reviewdbBlocks.text = if (blocks.isEmpty()) {
                    "No blocked ReviewDB users"
                } else {
                    "Blocked: " + blocks.joinToString { it.username }
                }
            }
        }
        binding.root
    }

    override fun start(scope: PluginScope): Completable = Completable.fromAction {
        this.scope = scope
        client = ReviewDbClient(
            token = { scope.settings.getString(TOKEN_KEY) },
            disableAds = { scope.settings.getBoolean("disableAds") },
            disableWarnings = { scope.settings.getBoolean("disableWarnings") },
        )
        val inflater = requireNotNull(scope.resources.inflater)

        scope.ui.registerProfileTab(
            pluginProfileTabContribution("reviewdb_tab", "ReviewDB", { ReviewdbPageBinding.inflate(inflater).root }) {
                priority = 50
                bind = { view, context ->
                    val binding = ReviewdbPageBinding.bind(view)
                    val page = (binding.root.tag as? ReviewDbPage)
                        ?.takeIf { it.targetId == context.userId }
                        ?: ReviewDbPage(scope, client, context.userId, "User Reviews").also {
                            binding.root.tag = it
                        }
                    page.bind(
                        binding,
                        ReviewDbPalette(
                            surface = context.surfaceColor,
                            border = context.borderColor,
                            primary = context.primaryTextColor,
                            muted = context.secondaryTextColor,
                            interactive = context.interactiveColor,
                        ),
                    )
                }
            },
        )

        scope.ui.registerContextMenuItem(
            pluginContextMenuItem("reviewdb_user", "View ReviewDB", PluginContextMenuSurface.USER, onClick = { context ->
                context.userId?.let { openReviews(it, "User Reviews") }
            }) { priority = 20 },
        )
        scope.ui.registerContextMenuItem(
            pluginContextMenuItem("reviewdb_block_user", "Block on ReviewDB", PluginContextMenuSurface.USER, onClick = { context ->
                val userId = context.userId ?: return@pluginContextMenuItem
                if (scope.settings.getString(TOKEN_KEY).isBlank()) {
                    authorize(scope, client)
                } else {
                    client.execute {
                        val result = client.block(userId, true)
                        scope.runOnMainThread { scope.dialogs.alert("ReviewDB", result.message.ifBlank { "User blocked" }) }
                    }
                }
            }) { priority = 10 },
        )
        scope.ui.registerContextMenuItem(
            pluginContextMenuItem("reviewdb_guild", "View Server Reviews", PluginContextMenuSurface.GUILD, onClick = { context ->
                context.targetId?.let { openReviews(it, "Server Reviews") }
            }) { priority = 20 },
        )
    }

    private fun openReviews(targetId: Long, title: String) {
        scope.dialogs.page(title, "ReviewDB") { context -> ReviewDbPage(scope, client, targetId, title).create(context) }
    }

    override fun stop(): Completable = Completable.fromAction { if (::client.isInitialized) client.close() }

    companion object {
        const val TOKEN_KEY = "token"

        fun authorize(scope: PluginScope, client: ReviewDbClient) {
            scope.oauth.authorize(client.authorizeUrl()) { redirect ->
                client.execute {
                    client.tokenFromRedirect(redirect)?.let { token ->
                        scope.settings.putString(TOKEN_KEY, token)
                    } ?: scope.runOnMainThread { scope.dialogs.alert("ReviewDB", "Authorization failed") }
                }
            }
        }
    }
}

class ReviewDbDiscoveryService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
