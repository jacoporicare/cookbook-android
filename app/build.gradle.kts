plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.git.version)
}

android {
    namespace = "cz.jakubricar.zradelnik"
    compileSdk = 35

    defaultConfig {
        applicationId = "cz.jakubricar.zradelnik"
        minSdk = 26
        targetSdk = 35
        versionCode = androidGitVersion.code()
        versionName = androidGitVersion.name()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "uses_cleartext_traffic", "false")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }

        release {
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
            buildConfigField("String", "API_URL", "\"http://10.0.2.2:4000/graphql\"")
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
                "-opt-in=kotlin.RequiresOptIn"
            )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Compose - legacy
    implementation(libs.compose.material.icons.extended)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Apollo
    implementation(libs.apollo.runtime)
    implementation(libs.apollo.normalized.cache.sqlite)
    implementation(libs.apollo.adapters)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.androidx.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.messaging.ktx)

    // Logging
    implementation(libs.timber)

    // Coil
    implementation(libs.coil.compose.android)
    implementation(libs.coil.network.okhttp)

    // Markdown
    implementation(libs.markdown)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

apollo {
    packageName.set("cz.jakubricar.zradelnik")

    mapScalar("Date", "java.time.OffsetDateTime")
    mapScalar("Upload", "com.apollographql.apollo3.api.Upload")
}
