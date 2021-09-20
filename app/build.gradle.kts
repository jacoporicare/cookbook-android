plugins {
    // Standard
    id("com.android.application")
    kotlin("android")

    // Annotation processors
    kotlin("kapt")

    // Apollo
    id("com.apollographql.apollo") version Versions.apollo

    // Hilt
    id("dagger.hilt.android.plugin")

    // App version via Git tags
    id("com.gladed.androidgitversion") version "0.4.14"
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "cz.jakubricar.zradelnik"
        minSdk = 26
        targetSdk = 31
        versionCode = androidGitVersion.code()
        versionName = androidGitVersion.name()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "uses_cleartext_traffic", "false")
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }

        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    flavorDimensions.add("environment")
    productFlavors {
        create("local") {
            dimension = "environment"
            buildConfigField("String", "API_URL", "\"http://10.0.2.2:8888/graphql\"")
            resValue("string", "uses_cleartext_traffic", "true")
        }

        create("development") {
            dimension = "environment"
            buildConfigField("String", "API_URL", "\"https://develop.api.zradelnik.eu/graphql\"")
        }

        create("production") {
            dimension = "environment"
            buildConfigField("String", "API_URL", "\"https://api.zradelnik.eu/graphql\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs =
            freeCompilerArgs + listOf(
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.coroutines.FlowPreview",
                "-Xallow-result-return-type"
            )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", Versions.kotlin))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose}")

    // Accompanist
    implementation("com.google.accompanist:accompanist-insets:${Versions.accompanist}")
    implementation("com.google.accompanist:accompanist-swiperefresh:${Versions.accompanist}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Versions.accompanist}")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.activity:activity-compose:1.3.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha09")

    // Preference
    implementation("androidx.preference:preference-ktx:1.1.1")

    // Work
    implementation("androidx.work:work-runtime-ktx:2.6.0")

    // Apollo
    implementation("com.apollographql.apollo:apollo-runtime:${Versions.apollo}")
    implementation("com.apollographql.apollo:apollo-coroutines-support:${Versions.apollo}")
    implementation("com.apollographql.apollo:apollo-normalized-cache-sqlite:${Versions.apollo}")

    // Hilt
    implementation("com.google.dagger:hilt-android:${Versions.hilt}")
    implementation("com.google.android.material:material:1.4.0")
    kapt("com.google.dagger:hilt-compiler:${Versions.hilt}")
    implementation("androidx.hilt:hilt-work:1.0.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test:${Versions.compose}")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.compose}")

    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
}

apollo {
    generateKotlinModels.set(true)
    customTypeMapping.set(
        mapOf(
            "Date" to "java.time.OffsetDateTime"
        )
    )
}
