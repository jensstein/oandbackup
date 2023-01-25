#!/usr/bin/ruby

from = "phosphor-original"
to = "adjusted"

begin
  Dir.mkdir(to)
rescue
end

count = 0

Dir.glob("#{from}/*.kt").each { |file|

  name = File.basename(file).delete_suffix(".kt")

  camel = name.gsub(/-(\w)/) { $1.upcase }
  small = name.downcase
  snake = small.gsub("-", "_")
  target = "#{to}/#{camel}.kt"

  puts "#{file} -> #{name} -> #{camel} / #{small} / #{snake} -> #{target}"

  text = File.open(file).read

  #text.gsub!(/\b#{name}/, camel)
  #text.gsub!(/\b#{small}\b/, camel)
  text.gsub!("`#{name}`", camel)
  text.gsub!("`_#{small}`", "_#{snake}")
  #text.gsub!("_#{snake}", "_image")
  #text.gsub!("_#{snake}", "_#{snake}")

  text.gsub!(/defaultWidth\s*=\s*256.0.dp/, "defaultWidth = 24.0.dp")
  text.gsub!(/defaultHeight\s*=\s*256.0.dp/, "defaultHeight = 24.0.dp")
  text.gsub!(/\s*(name|defaultWidth|defaultHeight|viewportWidth|viewportHeight)\s*=\s*(.*?)(,\s*|$)/) { "\n            "+$1+" = "+$2+"," }

  text.sub!(/^import/, "
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import")

  text += "


@Preview
@Composable
fun #{camel}Preview() {
    Image(
        Phosphor.#{camel},
        null
    )
}
"
  File.open(target, "w").puts text

  count+=1
  if count > 1 then
  #  exit
  end
}
