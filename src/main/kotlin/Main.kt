import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlin.io.path.*

val json = Json {
  prettyPrint = true
}

@OptIn(ExperimentalSerializationApi::class)
fun main() {
  val root = Path("is_safe_string").createDirectories()

  (root / "pack.mcmeta").outputStream().buffered().use { output ->
    json.encodeToStream(
      PackMetadata(
        PackMetadata.PackMetadataSection(
          description = "",
          packFormat = 15,
        )
      ),
      output,
    )
    output.write('\n'.code)
  }

  val functions = root / "data" / "is_safe_string" / "functions"

  fun isSafeString(path: String = ""): String {
    return "is_safe_string:$path"
  }

  fun writeFunction(name: String, block: Appendable.() -> Unit) {
    (functions / "$name.mcfunction").createParentDirectories().outputStream().buffered().use { output ->
      output.write(StringBuilder().apply { block() }.toString().encodeToByteArray())
    }
  }

  val init = "init/"
  writeFunction(init) {
    val one = "one"
    ('\u0000'..'\u007f')
      .filter { it != '\r' && it != '\n' }
      .mapTo(mutableListOf()) { "\"$it\"" }
      .also { it['"'.code - 2] = "'\"'" }
      .also { it['\\'.code - 2] = "\"\\\\\"" }
      .joinToString(",", "[", "]")
      .let { oneByteChars ->
        appendLine("data modify storage ${isSafeString()} $one set value $oneByteChars")
      }

    val two = "two"
    ('\u0080'..'\u07ff')
      .joinToString(",", "[", "]") { "\"$it\"" }
      .let { twoBytesChars ->
        appendLine("data modify storage ${isSafeString()} $two set value $twoBytesChars")
      }

    val three = "three"
    ('\u0800'..<Char.MIN_HIGH_SURROGATE)
      .joinToString(",", "[", "]") { "\"$it\"" }
      .let { threeBytesChars ->
        appendLine("data modify storage ${isSafeString()} $three set value $threeBytesChars")
      }

    val highSurrogate = "high_surrogate"
    StringBuilder()
      .apply {
        append('[')
        (Char.MAX_HIGH_SURROGATE downTo Char.MIN_HIGH_SURROGATE).forEach { high ->
          append('"')
          append(high)
          append(Char.MIN_LOW_SURROGATE)
          append('"')
          append(',')
        }
        append(']')
      }.let { highSurrogateChars ->
        appendLine("data modify storage ${isSafeString()} $highSurrogate set value $highSurrogateChars")
      }
    val initHighSurrogate = "${init}high_surrogate"
    appendLine("function ${isSafeString(initHighSurrogate)}")
    writeFunction(initHighSurrogate) {
      appendLine("data modify storage ${isSafeString()} $three append string storage ${isSafeString()} $highSurrogate[-1] 0 1")
      appendLine("data remove storage ${isSafeString()} $highSurrogate[-1]")
      appendLine("execute if data storage ${isSafeString()} $highSurrogate[0] run function ${isSafeString(initHighSurrogate)}")
    }
    appendLine("data remove storage ${isSafeString()} $highSurrogate")

    val lowSurrogate = "low_surrogate"
    StringBuilder()
      .apply {
        append('[')
        (Char.MAX_LOW_SURROGATE downTo Char.MIN_LOW_SURROGATE).forEach { low ->
          append('"')
          append(Char.MIN_HIGH_SURROGATE)
          append(low)
          append('"')
          append(',')
        }
        append(']')
      }.let { lowSurrogateChars ->
        appendLine("data modify storage ${isSafeString()} $lowSurrogate set value $lowSurrogateChars")
      }
    val initLowSurrogate = "${init}low_surrogate"
    appendLine("function ${isSafeString(initLowSurrogate)}")
    writeFunction(initLowSurrogate) {
      appendLine("data modify storage ${isSafeString()} $three append string storage ${isSafeString()} $lowSurrogate[-1] 1")
      appendLine("data remove storage ${isSafeString()} $lowSurrogate[-1]")
      appendLine("execute if data storage ${isSafeString()} $lowSurrogate[0] run function ${isSafeString(initLowSurrogate)}")
    }
    appendLine("data remove storage ${isSafeString()} $lowSurrogate")
  }
}

@Serializable
data class PackMetadata(
  val pack: PackMetadataSection,
) {
  @Serializable
  data class PackMetadataSection(
    val description: String,
    @SerialName("pack_format") val packFormat: Int,
  )
}
