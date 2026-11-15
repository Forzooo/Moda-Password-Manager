plugins {
    id("java-library")
//    `maven-publish`
}

group = "moda"
version = "1.0.0"
description = "Moda Password Manager"
java.sourceCompatibility = JavaVersion.VERSION_21

//application {
//    mainClass = ""
//}

//publishing {
//    publications.create<MavenPublication>("maven") {
//        from(components["java"])
//    }
//}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
