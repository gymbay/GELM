val libraryCompileSdk: Int = 36
val libraryMinSdk: Int = 23
val libraryGroupId = "io.github.gymbay"
val libraryName = "gelm"
val libraryVersion = "2.0.0"

plugins {
    alias(libs.plugins.jetbrainsKotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(libs.androidx.lifecycle.viewmodel)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.coroutines.test)
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "$libraryGroupId.$libraryName"
    compileSdk = libraryCompileSdk

    defaultConfig {
        minSdk = libraryMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("src/androidMain/consumer-rules.pro")

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            groupId = libraryGroupId
            artifactId = if (name == "kotlinMultiplatform") libraryName else "$libraryName-$name"
            version = libraryVersion

            pom {
                name = "GELM"
                description = """
                    Kotlin Multiplatform (KMP) library for the ELM Architecture.
                    Targets: Android, iOS, JVM.
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
    sign(publishing.publications)
}

// Javadoc JAR для JVM-публикации (требуется Maven Central).
// Dokka Javadoc не поддерживает KMP, поэтому публикуем пустой JAR — Maven Central этого достаточно.
val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "Empty Javadoc JAR for gelm-jvm (Maven Central requirement)"
    group = "documentation"
    archiveClassifier.set("javadoc")
}

publishing.publications.withType<MavenPublication>().matching { it.name == "jvm" }.all {
    artifact(dokkaJavadocJar)
}

/**
 * Порядок публикации новой версии
 *
 * 1. Поднимаем версию в build.gradle в параметре libraryVersion
 * 2. Генерируем ZIP архив проекта с помощьой generateUploadPackage
 *    !!! Для генерации необходимо убедиться в наличии прописанных ключей в файле gelm/gradle.properties
 *    !!! Убедиться что ключ валидный и опубликован через GPG Keychain
 * 3. Достаем подготовленный ZIP архив из директории gelm/build/ (файл deploy-$libraryVersion.zip)
 * 4. Авторизуемся в MavenCentral и загружаем архив в https://central.sonatype.com/publishing
 *    !!! В поле Deployment Name указываем название библиотеки - gelm
 *    !!! В поле Description можно указать описание релиза
 *    !!! В Upload Your File выбираем архив из шага 3
 * 5. После загрузки архива выполнится валидация
 * 6. После успешной валидации появится кнопка Publish
 * 7. Через некоторое время библиотека будет опубликована в maven central
 **/
tasks.register<Zip>("generateUploadPackage") {
    dependsOn(tasks.withType<PublishToMavenRepository>())
    from(layout.buildDirectory.dir("deploy-$libraryVersion")) {
        exclude("**/maven-metadata.xml")
    }

    archiveFileName.set("deploy-$libraryVersion.zip")
    destinationDirectory.set(layout.buildDirectory)
}
