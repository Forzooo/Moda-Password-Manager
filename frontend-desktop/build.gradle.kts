import org.panteleyev.jpackage.ImageType

plugins {
    id("java")
    id("org.panteleyev.jpackageplugin") version "2.1.0"
}

group = "moda"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":itc"))
    implementation(project(":backend"))
    // Source: https://mvnrepository.com/artifact/com.formdev/flatlaf
    implementation("com.formdev:flatlaf:3.8")
    // Source: https://mvnrepository.com/artifact/com.miglayout/miglayout-swing
    implementation("com.miglayout:miglayout-swing:11.4.2")
    // Source: https://central.sonatype.com/artifact/io.github.dj-raven/modal-dialog
    implementation("io.github.dj-raven:modal-dialog:2.6.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.register("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jmods"))
}

tasks.register("copyJar", Copy::class) {
    from(tasks.jar).into(layout.buildDirectory.dir("jmods"))
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    appName = "Moda Password Manager"
    appVersion = rootProject.version.toString()
    aboutUrl = "https://github.com/Forzooo/Moda-Password-Manager"
    copyright = "GNU General Public License, version 3 (GPLv3)"
    destination = layout.buildDirectory.dir("dist")
    fileAssociations.setFrom(layout.projectDirectory.file("src/main/resources/file-associations.properties"))
    input = layout.buildDirectory.dir("jmods").get().asFile
    javaOptions = listOf("-Dfile.encoding=UTF-8")
    jLinkOptions = listOf(
        "--ignore-signing-information",  // Bouncy castle is a signed JAR that jpackage does not support
        "--no-header-files",
        "--no-man-pages",
        "--strip-debug"
    )
    mainClass = "moda.passwordmanager.frontend.desktop.Application"
    mainJar = tasks.jar.get().archiveFileName.get()
    licenseFile = rootProject.layout.projectDirectory.file("LICENSE")
    vendor = "Moda"

    // Windows Specific
    windows {
        icon = layout.projectDirectory.file("src/main/resources/icons/logo.ico")
        type = ImageType.EXE
        winDirChooser = true
        winMenu = true
        winMenuGroup = "Moda"
        winShortcut = true
        winShortcutPrompt = true
    }
}

tasks.test {
    useJUnitPlatform()
}