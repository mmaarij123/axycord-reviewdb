import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("mmaarij.axycord.plugin")
}

val releaseSigningFile = file("keystore.properties")
val releaseSigning = Properties().apply {
    if (releaseSigningFile.exists()) releaseSigningFile.inputStream().use(::load)
}

axycordPlugin {
    id.set("axycord.reviewdb")
    entrypoint.set("mmaarij.axycord.reviewdb.ReviewDbPlugin")
    description.set("Read and manage user and server reviews with ReviewDB")
    // Plugin API levels, independent of the app's versionName and SDK artifact version.
    axycordVersionMinimum.set(3)
    axycordVersionMaximumExclusive.set(4)
    usesResources.set(true)
    async()
}

android {
    namespace = "mmaarij.axycord.reviewdb"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "mmaarij.axycord.reviewdb"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (releaseSigningFile.exists()) create("release") {
            storeFile = rootProject.file(releaseSigning.getProperty("storeFile"))
            storePassword = releaseSigning.getProperty("storePassword")
            keyAlias = releaseSigning.getProperty("keyAlias")
            keyPassword = releaseSigning.getProperty("keyPassword")
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures.viewBinding = true
    testOptions.unitTests.isIncludeAndroidResources = true
    lint {
        checkOnly += setOf("NewApi")
        fatal += "NewApi"
    }
}

dependencies {
    val checkout = providers.gradleProperty("axycordCheckout").orNull
    if (checkout != null) {
        val sdk = rootProject.file(checkout).resolve("build/plugin-sdk/axycord-plugin-api-1.0.6.jar")
        require(sdk.isFile) { "Generate the local SDK first: scripts/build.sh pluginSdkJar" }
        compileOnly(files(sdk))
    } else {
        compileOnly("mmaarij.axycord:plugin-api:1.0.6")
    }
    compileOnly(kotlin("stdlib"))
    compileOnly("androidx.recyclerview:recyclerview:1.4.0")
    compileOnly("androidx.appcompat:appcompat:1.7.1")
    compileOnly("com.facebook.fresco:drawee-span:3.7.0")
    compileOnly("com.facebook.fresco:fresco:3.7.0")
    compileOnly("com.google.android.material:material:1.3.0")
    compileOnly(libs.rxjava)
    compileOnly("androidx.core:core:1.16.0") { isTransitive = false }
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("stdlib"))
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.core:core:1.16.0")
}
