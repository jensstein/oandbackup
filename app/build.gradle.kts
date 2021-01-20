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
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.machiav3lli.backup"
        minSdkVersion(26)
        targetSdkVersion(29)
        versionCode = 5000
        versionName = "5.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("room.schemaLocation" to "$projectDir/schemas", "room.incremental" to "true"))
            }
        }
    }
    buildTypes {
        named("release") {
            minifyEnabled(true)
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        named("debug") {
            applicationIdSuffix = ".debug"
        }
        create("neo") {
            applicationIdSuffix = ".neo"
            versionNameSuffix = "-neo"
            minifyEnabled(false)
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    lintOptions {
        isAbortOnError = false
    }
}

val versions: java.util.Properties = System.getProperties()
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions["kotlin"]}")
    // Libs
    implementation("androidx.room:room-runtime:2.2.6")
    implementation("androidx.room:room-ktx:2.2.6")
    implementation("androidx.work:work-runtime-ktx:2.4.0")
    kapt("androidx.room:room-compiler:2.2.6")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.biometric:biometric:1.0.1")
    implementation("org.apache.commons:commons-compress:1.20")
    implementation("commons-io:commons-io:${versions["commons_io"]}")
    implementation("com.github.topjohnwu.libsu:core:${versions["libsu"]}")
    implementation("com.github.topjohnwu.libsu:io:${versions["libsu"]}")
    implementation("com.scottyab:rootbeer-lib:0.0.8")
    implementation("com.jakewharton.timber:timber:4.7.1")

    // UI
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("com.google.android.material:material:1.3.0-rc01")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.2")
    implementation("com.mikepenz:fastadapter:${versions["fastadapter"]}")
    implementation("com.mikepenz:fastadapter-extensions-diff:${versions["fastadapter"]}")
    implementation("com.mikepenz:fastadapter-extensions-binding:${versions["fastadapter"]}")

    // Tests
    implementation("androidx.test:rules:1.3.0")
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

