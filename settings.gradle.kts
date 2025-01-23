rootProject.name = "smaug"
include("src:main:test")
findProject(":src:main:test")?.name = "test"
