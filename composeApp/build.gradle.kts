import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.beans.SimpleBeanInfo

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.power.assert)
}

sqldelight {
    databases {
        create("NimbusDb") {
            packageName = "net.tactware.nimbus.db"
            srcDirs("src/main/sqldb")
            schemaOutputDirectory = file("src/main/sqldb/schemas")
            migrationOutputDirectory = file("src/main/sqldb/migrations")
            verifyMigrations = true
            deriveSchemaFromMigrations = true
        }
    }
}


kotlin {
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
        optIn.add("app.cash.paging.ExperimentalPagingApi")
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlin.test.junit)
            }
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.sqlDelight.coroutines)
            implementation(libs.koin.annotation)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose)
            implementation(libs.koin.coroutines)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.kotlinx.atomicfu)
            implementation(libs.kgit)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.apache)
            implementation(libs.ktor.client.logging.jvm)
            implementation(libs.ktor.serialization.kotlinx.json.jvm)
            implementation(libs.ktor.server.auth)
            implementation(libs.ktor.client.content.negotiation.jvm)
            implementation(libs.ktor.client.auth)

            // Paging library
            implementation(libs.paging.compose.common)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.sqlDelight.jvm)
        }
    }
}


compose.desktop {
    application {
        mainClass = "net.tactware.nimbus.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "net.tactware.nimbus"
            packageVersion = "1.0.0"
        }
    }
}
tasks.whenTaskAdded {
    if (name == "kspCommonMainKotlinMetadata") {
        tasks.named("kspKotlinDesktop") {
            dependsOn(this@whenTaskAdded)
        }
    }
}

tasks.whenTaskAdded {
    if (name.contains("ReleaseUnitTest")) {
        enabled = false
    }
}

powerAssert {
    functions = listOf("kotlin.test.assertEquals", "kotlin.test.assertTrue", "kotlin.test.assertFalse", "kotlin.assert")
}


//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspDesktop", libs.koin.ksp.compiler)
}

ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_CONFIG_CHECK", "true")
}
