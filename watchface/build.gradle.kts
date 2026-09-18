import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Release signing configuration.
// Credentials are read from keystore.properties (git-ignored) at the project
// root, or from the MUWF_* environment variables below. The file never goes
// into version control; see keystore.properties.template.
val signingProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signingValue(key: String): String? =
    signingProps.getProperty(key) ?: System.getenv("MUWF_${key.uppercase()}")

val releaseStoreFile = signingValue("storeFile")?.let { file(it) }
val hasReleaseSigning = releaseStoreFile != null &&
    signingValue("storePassword") != null &&
    signingValue("keyAlias") != null &&
    signingValue("keyPassword") != null

if (!hasReleaseSigning) {
    logger.warn(
        "No release signing config found (keystore.properties or MUWF_* env vars). " +
            "Release builds will be signed with the debug key. " +
            "Do not upload debug-signed artifacts to Google Play."
    )
}

android {
    enableKotlin = false
    namespace = "com.nagopy.android.myultimatewatchface"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nagopy.android.myultimatewatchface"
        // Watch Face Format version 2 requires Wear OS 5 (API 34).
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = releaseStoreFile
                storePassword = signingValue("storePassword")
                keyAlias = signingValue("keyAlias")
                keyPassword = signingValue("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
        }
        release {
            isMinifyEnabled = true
            // Ensure shrink resources is false, to avoid potential for them
            // being removed.
            isShrinkResources = false

            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
}
