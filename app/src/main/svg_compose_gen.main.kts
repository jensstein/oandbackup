@file:Repository("https://repo1.maven.org/maven2/")
@file:Repository("https://maven.google.com")
@file:Repository("https://jitpack.io")

@file:DependsOn("com.github.DevSrSouza:svg-to-compose:-SNAPSHOT")
@file:DependsOn("com.android.tools:sdk-common:27.2.0-alpha16")
@file:DependsOn("com.android.tools:common:27.2.0-alpha16")
@file:DependsOn("com.squareup:kotlinpoet:1.12.0")
@file:DependsOn("org.ogce:xpp3:1.1.6")

import br.com.devsrsouza.svg2compose.Svg2Compose
import br.com.devsrsouza.svg2compose.VectorType
import java.io.File

val assetsDir = File("res/regular")
val srcDir = File("java/com/machiav3lli/backup/ui/compose")

Svg2Compose.parse(
    applicationIconPackage = "com.machiav3lli.backup.ui.compose.icons",
    accessorName = "Matrix",
    outputSourceDirectory = srcDir,
    vectorsDirectory = assetsDir,
    type = VectorType.SVG,
)
