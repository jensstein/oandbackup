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
    kotlin("plugin.serialization").version("1.9.20")
    id("com.google.devtools.ksp") version ("1.9.20-1.0.14")
}

val vActivity = "1.8.1"
val vAccompanist = "0.33.2-alpha"
val vBiometric = "1.2.0-alpha05"
val vCoil = "2.5.0"
val vCommonsCompress = "1.25.0"
val vCommonsIO = "2.14.0"
val vComposeCompiler = "1.5.4"
val vCompose = "1.6.0-alpha07"
val vComposeM3 = "1.1.2" // does NOT crash in context menu "Put"
val vDatastore = "1.0.0"
val vKAML = "0.55.0"
val vKotlin = "1.9.20"
val vKSP = "1.0.14"
val vLibsu = "5.2.1"
val vLifecycle = "2.6.2"
val vMaterial = "1.10.0"
val vNavigation = "2.7.5"
val vPreference = "1.2.1"
val vRoom = "2.6.0"
val vSecurity = "1.1.0-alpha06"
val vSemVer = "4.1.0"
val vSerialization = "1.6.1"
val vTimber = "5.0.1"
val vWork = "2.9.0-rc01"

val vTest = "1.5.0"
val vTestRules = "1.5.0"
val vTestExt = "1.1.5"

android {
    namespace = "com.machiav3lli.backup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.machiav3lli.backup"
        minSdk = 26
        targetSdk = 33
        versionCode = 8314
        versionName = "8.3.5-alpha01"
        buildConfigField("int", "MAJOR", "8")
        buildConfigField("int", "MINOR", "3")

        testApplicationId = "${applicationId}.tests"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                    arg("room.generateKotlin", "true")
                }
            }
        }

    }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Backup_${variant.name}_${variant.versionName}.apk"
        }
        true
    }

    buildTypes {
        named("release") {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //versionNameSuffix = "-alpha01"
            isMinifyEnabled = true
        }
        named("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        create("neo") {
            applicationIdSuffix = ".neo"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        dataBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
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
    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }
}

dependencies {
    implementation(kotlin("stdlib", vKotlin))
    implementation(kotlin("reflect", vKotlin))
    implementation("com.google.devtools.ksp:symbol-processing-api:$vKotlin-$vKSP")

    // Libs
    implementation("androidx.activity:activity-compose:$vActivity")
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    ksp("androidx.room:room-compiler:$vRoom")
    // TODO use the new WorkInfo.stopReason (report stopReason), WorkManager.getWorkInfosFlow (Flow instead of LiveData), setNextScheduleTimeOverride (Precise scheduling), Configuration.Builder.setContentUriTriggerWorkersLimit (limit for content uri workers)
    implementation("androidx.work:work-runtime:$vWork")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$vSerialization")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$vSerialization")
    implementation("com.charleskorn.kaml:kaml:$vKAML")
    implementation("androidx.datastore:datastore-preferences:$vDatastore")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$vLifecycle")
    implementation("androidx.security:security-crypto-ktx:$vSecurity")
    implementation("androidx.biometric:biometric:$vBiometric")
    implementation("org.apache.commons:commons-compress:$vCommonsCompress")
    implementation("commons-io:commons-io:$vCommonsIO")      // attention, there is an old 20030203.000550 version, that looks higher
    implementation("com.jakewharton.timber:timber:$vTimber")
    implementation("com.github.topjohnwu.libsu:core:$vLibsu")
    implementation("com.github.topjohnwu.libsu:io:$vLibsu")
    implementation("de.voize:semver4k:$vSemVer")

    // UI
    implementation("com.google.android.material:material:$vMaterial")
    implementation("androidx.preference:preference-ktx:$vPreference")

    // Compose
    implementation("androidx.compose.runtime:runtime:$vCompose")
    implementation("androidx.compose.ui:ui:$vCompose")
    implementation("androidx.compose.ui:ui-tooling:$vCompose")
    implementation("androidx.compose.foundation:foundation:$vCompose")
    implementation("androidx.compose.runtime:runtime-livedata:$vCompose")
    implementation("androidx.navigation:navigation-compose:$vNavigation")
    implementation("io.coil-kt:coil-compose:$vCoil")
    implementation("androidx.compose.material3:material3:$vComposeM3")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$vAccompanist")
    implementation("com.google.accompanist:accompanist-permissions:$vAccompanist")

    // Testing
    androidTestImplementation("androidx.test:runner:$vTest")
    implementation("androidx.test:rules:$vTestRules")
    implementation("androidx.test.ext:junit-ktx:$vTestExt")

    // compose testing
    // Test rules and transitive dependencies:
    androidTestImplementation("androidx.compose.ui:ui-test:$vCompose")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$vCompose")
    // Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation("androidx.compose.ui:ui-test-manifest:$vCompose")
}

//TODO: how to do this with ksp?
//kapt {
//    correctErrorTypes = true
//}

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

tasks.withType<Test> {
    useJUnit()          // we still use junit4
    //useTestNG()
    //useJUnitPlatform()
}
