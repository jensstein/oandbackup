/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization").version("1.6.21")
}
val compose = "1.2.0-beta01"

android {
    namespace = "com.machiav3lli.backup"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.machiav3lli.backup"
        minSdk = 26
        targetSdk = 32
        versionCode = 8017
        versionName = "8.0.3"
        buildConfigField("int", "MAJOR", "8")
        buildConfigField("int", "MINOR", "0")

        testApplicationId = "${applicationId}.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(
                    mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true"
                    )
                )
            }
        }

    }

    buildTypes {
        named("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = true
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
        }
        named("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
        }
        create("neo") {
            applicationIdSuffix = ".neo"
            versionNameSuffix = "-alpha1"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_vv"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_vv"
        }
    }
    buildFeatures {
        dataBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = compileOptions.sourceCompatibility.toString()
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packagingOptions {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")

    // Libs
    implementation("androidx.room:room-runtime:2.5.0-alpha01")
    implementation("androidx.room:room-ktx:2.5.0-alpha01")
    kapt("androidx.room:room-compiler:2.5.0-alpha01")
    implementation("androidx.work:work-runtime-ktx:2.8.0-alpha02")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha03")
    implementation("androidx.biometric:biometric:1.2.0-alpha04")
    implementation("org.apache.commons:commons-compress:1.21")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    val libsu = "3.2.1"
    implementation("com.github.topjohnwu.libsu:core:$libsu")
    implementation("com.github.topjohnwu.libsu:io:$libsu")
    //implementation("com.github.topjohnwu.libsu:busybox:$libsu")
    implementation("com.vdurmont:semver4j:3.1.0")
    //implementation("com.github.tony19:named-regexp:0.2.6") // regex named groups

    // UI
    implementation("androidx.fragment:fragment-ktx:1.5.0-rc01")
    implementation("com.google.android.material:material:1.7.0-alpha01")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0-rc01")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0-rc01")
    implementation("io.coil-kt:coil-compose:2.1.0")

    // Compose
    implementation("androidx.compose.runtime:runtime:$compose")
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-tooling:$compose")
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.runtime:runtime-livedata:$compose")
    implementation("androidx.navigation:navigation-compose:2.5.0-rc01")
    implementation("com.google.android.material:compose-theme-adapter-3:1.0.9")
    implementation("androidx.compose.material3:material3:1.0.0-alpha11")
    implementation("com.google.accompanist:accompanist-flowlayout:0.24.9-beta")

    // Testing
    implementation("androidx.test.ext:junit-ktx:1.1.3")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    val androidxTest = "1.4.0"
    implementation("androidx.test:rules:$androidxTest")
    androidTestImplementation("androidx.test:runner:$androidxTest")
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("src/main/res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile.name.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
}
tasks.preBuild.dependsOn("detectAndroidLocals")

// tells all test tasks to use Gradle's built-in JUnit 5 support
tasks.withType<Test> {
    useJUnit()
    //useTestNG()
    //useJUnitPlatform()
}
