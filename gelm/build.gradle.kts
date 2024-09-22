val libraryCompileSdk: Int = 34
val libraryMinSdk: Int = 23
val libraryGroupId = "io.github.gymbay"
val libraryName = "gelm"
val libraryVersion = "1.0.0"

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    `maven-publish`
    signing
}

android {
    namespace = "$libraryGroupId.$libraryName"
    compileSdk = libraryCompileSdk

    defaultConfig {
        minSdk = libraryMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        aarMetadata {
            minCompileSdk = libraryMinSdk
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = libraryGroupId
            artifactId = libraryName
            version = libraryVersion

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name = "GELM"
                description = """
                    The Android library for the popular architecture approach of ELM. 
                """.trimIndent()
                url = "https://github.com/gymbay/GELM"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "gymbay"
                        name = "Efremov Alexey"
                        email = "aefremov430@yandex.ru"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/gymbay/GELM.git"
                    developerConnection = "scm:git:ssh://github.com/gymbay/GELM.git"
                    url = "https://github.com/gymbay/GELM"
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("deploy-$libraryVersion"))
        }
    }
}

signing {
    sign(publishing.publications["release"])
}

tasks.register<Zip>("publishingZipArchive") {
    val publishTask = tasks.named(
        "publishReleasePublicationToMavenRepository",
        PublishToMavenRepository::class.java
    )
    val paths = publishTask.map { it.repository.url }
    from(paths)
    into(layout.buildDirectory.get().toString()) {
        rename { name ->
            name.substringAfter("deploy-$libraryVersion")
        }
    }
    archiveFileName.set("deploy-$libraryVersion.zip")
}