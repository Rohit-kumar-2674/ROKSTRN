import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

val javaTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())

val localProperties = gradleLocalProperties(rootDir, project.providers)
val forkVersion = "${libs.versions.versionName.get()}-rokstrn.1"

fun forkSetting(environmentName: String, propertyName: String): String =
    providers.environmentVariable(environmentName).orNull
        ?: localProperties.getProperty(propertyName).orEmpty()

val forkRepository = forkSetting("ROKSTRN_GITHUB_REPOSITORY", "rokstrn.github.repository").trim()
require(forkRepository.isEmpty() ||
    (forkRepository.matches(Regex("[A-Za-z0-9][A-Za-z0-9-]*/[A-Za-z0-9_.-]+")) &&
        forkRepository.substringAfter('/') !in listOf(".", "..") &&
        !forkRepository.equals("recloudstream/cloudstream", ignoreCase = true))) {
    "Configure the owner/repository of the independent ROKSTRN fork."
}
val communityUrl = forkSetting("ROKSTRN_COMMUNITY_URL", "rokstrn.community.url").trim()
require(communityUrl.isEmpty() || URI(communityUrl).let {
    it.scheme == "https" && it.host != null && it.userInfo == null
}) { "The community URL must use HTTPS." }
val updatesEnabled = forkSetting("ROKSTRN_ENABLE_UPDATES", "rokstrn.updates.enabled") == "true"
require(!updatesEnabled || forkRepository.isNotEmpty()) { "Updates require a ROKSTRN repository." }

val signingFile = forkSetting("ROKSTRN_KEYSTORE_FILE", "rokstrn.signing.storeFile").trim()
val signingPassword = forkSetting("ROKSTRN_KEYSTORE_PASSWORD", "rokstrn.signing.storePassword")
val signingAlias = forkSetting("ROKSTRN_KEY_ALIAS", "rokstrn.signing.keyAlias").trim()
val signingKeyPassword = forkSetting("ROKSTRN_KEY_PASSWORD", "rokstrn.signing.keyPassword")
val signingValues = listOf(signingFile, signingPassword, signingAlias, signingKeyPassword)
val hasReleaseSigning = signingValues.any { it.isNotEmpty() }
require(!hasReleaseSigning || signingValues.all { it.isNotEmpty() }) {
    "Set all four ROKSTRN signing properties or leave all four unset."
}
val releaseKey = if (hasReleaseSigning) rootProject.file(signingFile) else null
require(releaseKey == null || releaseKey.isFile) { "The ROKSTRN keystore file is unavailable." }

abstract class GenerateGitHashTask : DefaultTask() {
    @get:Input
    abstract val revision: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val output = outputDir.file("git-hash.txt").get().asFile
        output.parentFile.mkdirs()
        output.writeText(revision.get().take(7))
    }
}

val hasGitCheckout = rootProject.file(".git").exists()
val sourceRevision = if (hasGitCheckout) {
    providers.exec {
        workingDir(rootDir)
        commandLine("git", "rev-parse", "--short=7", "HEAD")
    }.standardOutput.asText.map { it.trim() }
} else {
    providers.environmentVariable("ROKSTRN_SOURCE_REVISION").orElse("")
}

val generateGitHash = tasks.register<GenerateGitHashTask>("generateGitHash") {
    revision.set(sourceRevision)
    outputDir.set(layout.buildDirectory.dir("generated/git"))
}

val sourceEpoch = providers.environmentVariable("SOURCE_DATE_EPOCH").orElse(
    if (hasGitCheckout) providers.exec {
        workingDir(rootDir)
        commandLine("git", "show", "-s", "--format=%ct", "HEAD")
    }.standardOutput.asText.map { it.trim() } else providers.provider { "0" }
).get().toLong()

android {
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    // Looks like google likes to add metadata only they can read https://gitlab.com/IzzyOnDroid/repo/-/work_items/491
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    androidComponents {
        onVariants { variant ->
            variant.sources.assets?.addGeneratedSourceDirectory(
                generateGitHash,
                GenerateGitHashTask::outputDir
            )
        }
    }

    signingConfigs {
        if (releaseKey != null) {
            create("rokstrnRelease") {
                storeFile = releaseKey
                storePassword = signingPassword
                keyAlias = signingAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.rokstrn.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 4090101
        versionName = forkVersion

        manifestPlaceholders["target_sdk_version"] = libs.versions.targetSdk.get()

        buildConfigField("String", "ROKSTRN_REPOSITORY", "\"$forkRepository\"")
        buildConfigField("String", "ROKSTRN_COMMUNITY_URL", "\"$communityUrl\"")
        buildConfigField("boolean", "ROKSTRN_UPDATES_ENABLED", updatesEnabled.toString())
        buildConfigField("long", "BUILD_DATE", "${sourceEpoch * 1000}L")
        buildConfigField(
            "String",
            "MAL_KEY",
            "\"" + (System.getenv("MAL_KEY") ?: localProperties["mal.key"]) + "\""
        )
        buildConfigField(
            "String",
            "ANILIST_KEY",
            "\"" + (System.getenv("ANILIST_KEY") ?: localProperties["anilist.key"]) + "\""
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isDebuggable = false
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("rokstrnRelease")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions.add("state")
    productFlavors {
        create("stable") {
            dimension = "state"
        }
        create("prerelease") {
            dimension = "state"
            applicationIdSuffix = ".prerelease"
            versionNameSuffix = "-preview"
            versionCode = (System.currentTimeMillis() / 60000).toInt()
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.toVersion(javaTarget.target)
        targetCompatibility = JavaVersion.toVersion(javaTarget.target)
    }

    java {
        // Use Java 17 toolchain even if a higher JDK runs the build.
        // We still use Java 8 for now which higher JDKs have deprecated.
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(libs.versions.jdkToolchain.get()))
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        jniLibs {
            // Enables legacy JNI packaging to reduce APK size (similar to builds before minSdk 23).
            // Note: This may increase app startup time slightly.
            useLegacyPackaging = true
        }
    }

    // Keep the generated resource and extension-facing JVM namespace.
    namespace = "com.lagradost.cloudstream3"
}

dependencies {
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.core)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.instancio.core)
    androidTestImplementation(libs.junit.ktx)
    androidTestImplementation(libs.kotlin.test)

    // Android Core & Lifecycle
    implementation(libs.core.ktx)
    implementation(libs.activity.ktx)
    implementation(libs.annotation)
    implementation(libs.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json) // JSON Parser

    // Design & UI
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Coil Image Loading
    implementation(libs.bundles.coil)

    // Media 3 (ExoPlayer)
    implementation(libs.bundles.media3)
    implementation(libs.video)

    // FFmpeg Decoding
    implementation(libs.bundles.nextlib)

    // Anime-db for filler
    implementation(libs.anime.db)

    // PlayBack
    implementation(libs.colorpicker) // Subtitle Color Picker
    implementation(libs.newpipeextractor) // For Trailers
    implementation(libs.juniversalchardet) // Subtitle Decoding

    // UI Stuff
    implementation(libs.shimmer) // Shimmering Effect (Loading Skeleton)
    implementation(libs.palette.ktx) // Palette for Images -> Colors
    implementation(libs.tvprovider)
    implementation(libs.overlappingpanels) // Gestures
    implementation(libs.biometric) // Fingerprint Authentication
    implementation(libs.previewseekbar.media3) // SeekBar Preview
    implementation(libs.qrcode.kotlin) // QR Code for PIN Auth on TV

    // Extensions & Other Libs
    implementation(libs.jsoup) // HTML Parser
    implementation(libs.ksoup) // HTML Parser
    implementation(libs.rhino) // Run JavaScript
    implementation(libs.safefile) // To Prevent the URI File Fu*kery
    coreLibraryDesugaring(libs.desugar.jdk.libs.nio) // NIO Flavor Needed for NewPipeExtractor
    implementation(libs.conscrypt.android) // To Fix SSL Fu*kery on Android 9
    implementation(libs.jackson.module.kotlin) // JSON Parser
    implementation(libs.zipline)

    // Temp/deprecated; will be removed once extensions have time to migrate from using it
    implementation("com.google.code.gson:gson:2.11.0")
    // Deprecated; will be removed once extensions have time to migrate from using it
    implementation("me.xdrop:fuzzywuzzy:1.4.0")

    // Torrent Support
    implementation(libs.torrentserver)

    // Downloading & Networking
    implementation(libs.work.runtime.ktx)
    implementation(libs.nicehttp) // HTTP Lib

    implementation(libs.bundles.compose)
    implementation(libs.activity.compose)
    implementation(libs.kotlinx.io.core) // Logcat parser

    implementation(project(":library"))
    implementation(project(":shared"))
}

tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.directories) // Full Sources
}

tasks.register<Copy>("copyJar") {
    dependsOn("build", ":library:jvmJar")
    from(
        "build/intermediates/compile_app_classes_jar/prereleaseDebug/bundlePrereleaseDebugClassesToCompileJar",
        "../library/build/libs"
    )
    into("build/app-classes")
    include("classes.jar", "library-jvm*.jar")
    // Remove the version
    rename("library-jvm.*.jar", "library-jvm.jar")
}

// Merge the app classes and the library classes into classes.jar
tasks.register<Jar>("makeJar") {
    // Duplicates cause hard to catch errors, better to fail at compile time.
    duplicatesStrategy = DuplicatesStrategy.FAIL
    dependsOn(tasks.getByName("copyJar"))
    from(
        zipTree("build/app-classes/classes.jar"),
        zipTree("build/app-classes/library-jvm.jar")
    )
    destinationDirectory.set(layout.buildDirectory)
    archiveBaseName = "classes"
}

tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(javaTarget)
        jvmDefault.set(JvmDefaultMode.ENABLE)
        optIn.addAll(
            "com.lagradost.cloudstream3.InternalAPI",
            "com.lagradost.cloudstream3.Prerelease",
        )
    }
}

dokka {
    moduleName = "App"
    dokkaSourceSets {
        configureEach {
            suppress = name != "prereleaseDebug"
            analysisPlatform = KotlinPlatform.JVM
            displayName = "JVM"
            documentedVisibilities(
                VisibilityModifier.Public,
                VisibilityModifier.Protected
            )

            sourceLink {
                localDirectory = file("..")
                remoteUrl("https://github.com/recloudstream/cloudstream/tree/master")
                remoteLineSuffix = "#L"
            }
        }
    }
}
