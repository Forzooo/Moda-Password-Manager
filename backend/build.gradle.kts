plugins {
    id("java")
}

group = "moda"
version = "0.6.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":itc"))
    // Source: https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on
    implementation("org.bouncycastle:bcprov-jdk18on:1.87")
    // Source: https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    // Source: https://mvnrepository.com/artifact/com.google.api-client/google-api-client
    implementation("com.google.api-client:google-api-client:2.7.2")
    // Source: https://mvnrepository.com/artifact/com.google.apis/google-api-services-drive
    implementation("com.google.apis:google-api-services-drive:v3-rev20250210-2.0.0")
    // Source: https://mvnrepository.com/artifact/com.google.oauth-client/google-oauth-client-jetty
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.38.0")
    // Source: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation("com.fasterxml.jackson.core:jackson-core:2.22.3")
    // Source: https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.3")
    // Source: https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.22.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}