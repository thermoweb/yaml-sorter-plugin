rootProject.name = "yaml-sorter-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("snakeyaml", "org.yaml:snakeyaml:2.2")
        }
    }
}

