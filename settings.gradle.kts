rootProject.name = "SuperGuard"
include(":app")
include(":plugin")
project(":plugin").projectDir = File("buildSrc")
