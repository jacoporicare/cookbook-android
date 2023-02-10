plugins {
    // Standard
    id("com.android.application")
    kotlin("android")

    // Annotation processors
    kotlin("kapt")

    // Firebase
    id("com.google.gms.google-services")

    // Apollo
    id("com.apollographql.apollo3") version "3.7.4"

    // Hilt
    id("dagger.hilt.android.plugin")

    // App version via Git tags
    id("com.gladed.androidgitversion") version "0.4.14"
}

android {
    namespace = "cz.jakubricar.zradelnik"

    compileSdk = 33

    defaultConfig {
        applicationId = "cz.jakubricar.zradelnik"
        minSdk = 26
        targetSdk = 33
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
            buildConfigField("String", "NEW_RECIPES_TOPIC", "\"new_recipes.debug\"")
            resValue("string", "uses_cleartext_traffic", "true")
        }

        create("development") {
            dimension = "environment"
            buildConfigField("String", "API_URL", "\"https://api-test.zradelnik.eu/graphql\"")
            buildConfigField("String", "NEW_RECIPES_TOPIC", "\"new_recipes.debug\"")
        }

        create("production") {
            dimension = "environment"
            buildConfigField("String", "API_URL", "\"https://api.zradelnik.eu/graphql\"")
            buildConfigField("String", "NEW_RECIPES_TOPIC", "\"new_recipes\"")
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
                "-Xopt-in=kotlin.RequiresOptIn"
            )
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib", "1.6.10"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:29.1.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.3.3")
    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.3")

    // Accompanist
    implementation("com.google.accompanist:accompanist-insets:0.19.0")
    implementation("com.google.accompanist:accompanist-insets-ui:0.19.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.19.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.19.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.6.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // Work
    implementation("androidx.work:work-runtime-ktx:2.8.0")

    // Apollo
    implementation("com.apollographql.apollo3:apollo-runtime:3.7.4")
    implementation("com.apollographql.apollo3:apollo-normalized-cache-sqlite:3.7.4")
    implementation("com.apollographql.apollo3:apollo-adapters:3.7.4")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.41")
    kapt("com.google.dagger:hilt-compiler:2.41")
    // Hilt Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    // Hilt Work
    implementation("androidx.hilt:hilt-work:1.0.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Coil
    implementation("io.coil-kt:coil-compose:1.4.0")

    // Markdown
    implementation("com.github.jeziellago:compose-markdown:0.2.6")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test:1.3.3")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.3.3")

    debugImplementation("androidx.compose.ui:ui-tooling:1.3.3")
}

apollo {
    packageName.set("cz.jakubricar.zradelnik")

    mapScalar("Date", "java.time.OffsetDateTime")
    mapScalar("Upload", "com.apollographql.apollo3.api.Upload")
}
