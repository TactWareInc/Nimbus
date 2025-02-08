import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("NimbusDb") {
            packageName = "net.tactware.nimbus.db"
            srcDirs("src/main/sqldb")
        }
    }
}


kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
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
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.kotlinx.atomicfu)
            implementation(libs.kgit)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
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