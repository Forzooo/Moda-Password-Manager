plugins {
    id("java")
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

tasks.test {
    useJUnitPlatform()
}