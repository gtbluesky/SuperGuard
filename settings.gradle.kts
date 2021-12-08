rootProject.name = "ObscureApk"
include(":app")
include(":plugin")
project(":plugin").projectDir = File("buildSrc")
