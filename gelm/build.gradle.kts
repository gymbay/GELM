val libraryCompileSdk: Int = 35
val libraryMinSdk: Int = 23
val libraryGroupId = "io.github.gymbay"
val libraryName = "gelm"
val libraryVersion = "1.1.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
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

/**
 * Порядок публикации новой версии
 *
 * 1. Поднимаем версию в build.gradle в параметре libraryVersion
 * 2. Генерируем ZIP архив проекта с помощьой generateUploadPackage
 *    !!! Для генерации необходимо убедиться в наличии прописанных ключей в файле gelm/gradle.properties
 *    !!! Убедиться что ключ валидный и опубликован через GPG Keychain
 * 3. Достаем подготовленный ZIP архив из директории gelm/build/deploy-$libraryVersion
 * 4. Авторизуемся в MavenCentral и загружаем архив в https://central.sonatype.com/publishing
 *    !!! В поле Deployment Name указываем название библиотеки - gelm
 *    !!! В поле Description можно указать описание релиза
 *    !!! В Upload Your File выбираем архив из шага 3
 * 5. После загрузки архива выполнится валидация
 * 6. После успешной валидации появится кнопка Publish
 * 7. Через некоторое время библиотека будет опубликована в maven central
 **/
tasks.register<Zip>("generateUploadPackage") {
    val publishTask = tasks.named(
        "publishReleasePublicationToMavenRepository",
        PublishToMavenRepository::class.java
    )
    val paths = publishTask.map { it.repository.url }
    from(paths)
    // Exclude maven-metadata.xml as Sonatype fails upload validation otherwise
    exclude {
        // Exclude left over directories not matching current version
        // That was needed otherwise older versions empty directories would be include in our ZIP
        if (it.file.isDirectory && it.path.matches(Regex(""".*\d+\.\d+.\d+$""")) && !it.path.contains(
                libraryVersion
            )
        ) {
            return@exclude true
        }

        // Only take files inside current version directory
        // Notably excludes maven-metadata.xml which Maven Central upload validation does not like
        (it.file.isFile && !it.path.contains(libraryVersion))
    }
    into(layout.buildDirectory.get().toString()) {
        rename { name ->
            name.substringAfter("deploy-$libraryVersion")
        }
    }
    archiveFileName.set("deploy-$libraryVersion.zip")
}