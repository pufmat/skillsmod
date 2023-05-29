plugins {
    id("fabric-loom") version "1.1-SNAPSHOT"
    id("checkstyle")
}

base.archivesName.set("${project.properties["archives_base_name"]}")
version = "${project.properties["mod_version"]}-${project.properties["minecraft_version"]}-common"
group = "${project.properties["maven_group"]}"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")

    compileOnly("net.fabricmc:sponge-mixin:${project.properties["mixin_version"]}")

    testImplementation("org.junit.jupiter:junit-jupiter:${project.properties["junit_version"]}")
}

loom {
    mixin {
        useLegacyMixinAp.set(false)
    }
}