# ReviewDB for Axycord

Maintained by **Maarij**. Native Axycord port of the Microcord plugin.

## Features and use

Read user and server reviews, manage your review, and vote or report through ReviewDB.

- Open **ReviewDB** in a user profile tab or choose **View ReviewDB** from a user menu.
- Server menu: **View Server Reviews**. User menu: **Block on ReviewDB**.
- Pages support pagination, review entry, vote toggling and reporting. Long-press your own review to delete it, or another review to report it.
- Plugin settings provide authorization, authorization status, blocked-user status and the provider's ad/warning options.
- The host emoji picker is available in review entry when the provider reports eligibility.

Authorize through the button before authenticated actions. A manual ReviewDB token
can be entered if the browser flow cannot complete. Authentication and review data
are handled by the existing ReviewDB provider (`manti.vendicated.dev`); provider
availability, OAuth acceptance and eligibility remain external dependencies.
No background new-review notification service is implemented.

## Install

Download **Axycord-ReviewDB-v1.0.0.apk** from
[Releases](https://github.com/mmaarij123/axycord-reviewdb/releases/latest), or install it through
**Axycord Settings → Plugins → Browse Plugins**. Approve installation and the
plugin signing certificate when prompted, then enable **ReviewDB**.

Requires Android 6.0+ and Axycord plugin API level 3, available in Axycord 1.0.
Plugin ID: `axycord.reviewdb`. Android package: `mmaarij.axycord.reviewdb`.
Plugin version: **1.0.0**. It uses the Axycord Plugin SDK **1.0.6**; these are separate
version numbers. No companion launcher activity is needed.

## Typography

Every native text view is assigned Axycord's active Discord typefaces explicitly,
including review rows, author/date labels, scores, titles, pagination, settings
buttons and the nested review editor. Host normal/bold/semibold attributes are used;
bundled Whitney faces are the fallback. Vote feedback is inline rather than a
system Toast. Profile palettes are retained and standalone pages read host colors.
Third-party authorization web pages retain their provider-controlled appearance.

## Axycord port fixes

The network executor belongs to the enabled plugin and shuts down on disable.
The inherited inactive notification toggle was removed rather than exposing a
setting with no effect. Reviews, votes, reports and blocks are sent only through
the corresponding user actions.

## Build and verify

Use JDK 21, Android SDK `platforms;android-37.0` and Build Tools `36.0.0`.
Set `ANDROID_HOME` or configure `sdk.dir` in an ignored `local.properties` file.
The SDK and `mmaarij.axycord.plugin` Gradle plugin resolve from Axycord's public
Maven branch without a GitHub login.

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
```

Windows: `.\gradlew.bat lintDebug testDebugUnitTest assembleDebug`.
GitHub Actions runs the same checks and saves a debug APK as an artifact. Debug
artifacts use the runner's key and are intended for verification; published
release APKs use the maintainer's stable plugin key.

For a signed release, copy `keystore.properties.example` to `keystore.properties`,
fill in the original signing-key details and run `assembleRelease`. Release output
is under `build/outputs/apk/release/`. Keep the key private and reuse it for updates.
The signing properties and key files are ignored by Git.

To compile against a local host checkout, generate `pluginSdkJar` there and pass
`-PaxycordCheckout=/path/to/axycord`. Normal builds use the public SDK.

## Catalog updates

The [Axycord Plugins collection](https://github.com/Axycord/Axycord-Plugins) contains
the corresponding source module and a pinned release entry. For updates, increase
`versionCode` and `versionName`, release with the same key, synchronize that module,
and update the catalog URL, size and SHA-256. Catalog CI verifies the APK package,
plugin metadata and signing fingerprint before publishing.

## Verification boundary

Build/lint and offline regression checks cover the port. Live account actions,
third-party service behavior and visual rendering on a device require separate
runtime verification. The plugin never needs your Discord token in its settings.

## License and credits

GPL-3.0; see [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md). Original credits
are preserved there. The Axycord adaptation is maintained by Maarij.
