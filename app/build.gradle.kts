import java.util.*

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
        versionCode = 4000
        versionName = "4.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf("room.schemaLocation" to "$projectDir/schemas", "room.incremental" to "true"))
            }
        }
    }
    buildTypes {
        named("release") {
            minifyEnabled(false)
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
            // minifyEnabled = true
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

val versions: Properties = System.getProperties()
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions["kotlin"]}")
    // Libs
    implementation("androidx.room:room-runtime:${versions["room"]}")
    implementation("androidx.room:room-ktx:${versions["room"]}")
    implementation("androidx.work:work-runtime-ktx:${versions["work"]}")
    kapt("androidx.room:room-compiler:${versions["room"]}")
    implementation("com.google.code.gson:gson:${versions["gson"]}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${versions["lifecycle"]}")
    implementation("androidx.biometric:biometric:${versions["biometric"]}")
    implementation("org.apache.commons:commons-compress:${versions["commons_compress"]}")
    implementation("commons-io:commons-io:${versions["commons_io"]}")
    implementation("com.github.topjohnwu.libsu:core:${versions["libsu"]}")
    implementation("com.github.topjohnwu.libsu:io:${versions["libsu"]}")
    implementation("com.scottyab:rootbeer-lib:${versions["rootBeer"]}")
    implementation("com.jakewharton.timber:timber:${versions["timber"]}")

    // UI
    implementation("androidx.appcompat:appcompat:${versions["appcompat"]}")
    implementation("androidx.fragment:fragment-ktx:${versions["fragment"]}")
    implementation("com.google.android.material:material:${versions["material"]}")
    implementation("androidx.preference:preference-ktx:${versions["preference"]}")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:${versions["swiperefreshlayout"]}")
    implementation("androidx.navigation:navigation-fragment-ktx:${versions["navigation"]}")
    implementation("androidx.navigation:navigation-ui-ktx:${versions["navigation"]}")
    implementation("com.mikepenz:fastadapter:${versions["fastadapter"]}")
    implementation("com.mikepenz:fastadapter-extensions-diff:${versions["fastadapter"]}")
    implementation("com.mikepenz:fastadapter-extensions-binding:${versions["fastadapter"]}")

    // Tests
    implementation("androidx.test:rules:${versions["androidx_test"]}")
    testImplementation("junit:junit:${versions["junit"]}")
    androidTestImplementation("androidx.test.ext:junit:${versions["junitx"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${versions["espresso"]}")
}

