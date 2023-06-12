import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlin.io.path.*

@OptIn(ExperimentalSerializationApi::class)
fun main() {
  val json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
  }

  val root = Path("is_safe_string").createDirectories()

  (root / "pack.mcmeta").outputStream().buffered().use { output ->
    json.encodeToStream(
      PackMetadata(
        PackMetadata.PackMetadataSection(
          description = "",
          packFormat = 15
        )
      ),
      output
    )
    output.write('\n'.code)
  }

  (root / "data" / "is_safe_string" / "loot_tables" / "init.json").outputStream().buffered().use { output ->
    json.encodeToStream(
      LootTable(
        type = "command",
        functions = listOf(
          LootTable.Function(
            function = "set_nbt",
            tag = "{u+000a: '\r', u+000d: '\n'}"
          )
        ),
        pools = listOf(
          LootTable.Pool(
            entries = listOf(
              LootTable.Pool.Entry(
                type = "item",
                name = "stone"
              )
            ),
            rolls = 1
          )
        )
      ),
      output
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

  val objective = "is_safe_string"
  val r0 = "#0"
  val r1 = "#1"

  val mainStorage = "is_safe_string:"
  val dataStorage = "is_safe_string_data:"

  val one = "one"
  val two = "two"

  val oneByteChars: List<String> = ('\u0001'..'\u007f').mapTo(mutableListOf()) {
    when (it) {
      '\n', '\r' -> "\"\""
      '"'        -> "'\"'"
      '\\'       -> "\"\\\\\""
      else       -> "\"$it\""
    }
  }
  val twoBytesChars = listOf('\u0000') + ('\u0080'..'\u07ff')

  val init = "init/"
  writeFunction(init) {
    appendLine("scoreboard objectives add $objective dummy")

    oneByteChars
      .joinToString(",", "[", "]")
      .let { oneByteChars ->
        appendLine("data modify storage $dataStorage $one set value $oneByteChars")
      }
    val init1 = "${init}1"
    appendLine("execute summon item_frame run function ${isSafeString(init1)}")
    writeFunction(init1) {
      appendLine("loot replace entity @s container.0 loot ${isSafeString("init")}")
      appendLine("data modify storage $dataStorage $one[${'\n'.code - 1}] set from entity @s Item.tag.u+000a")
      appendLine("data modify storage $dataStorage $one[${'\r'.code - 1}] set from entity @s Item.tag.u+000d")
      appendLine("kill")
    }

    twoBytesChars
      .joinToString(",", "[", "]") { "\"$it\"" }
      .let { twoBytesChars ->
        appendLine("data modify storage $dataStorage $two set value $twoBytesChars")
      }
  }

  val isSafeString = "is_safe_string/"
  writeFunction(isSafeString) {
    val input = "input"
    val length = r0
    appendLine("execute store result score $length $objective run data get storage $mainStorage $input")

    val maxSafeByteLength = 65535
    val maxSafeCodeUnitLength = maxSafeByteLength / 3
    val output = "output"
    appendLine("execute if score $length $objective matches ..${maxSafeCodeUnitLength} run data modify storage $mainStorage $output set value true")

    appendLine("execute if score $length $objective matches ${maxSafeByteLength + 1}.. run data modify storage $mainStorage $output set value false")

    val isSafeString1 = "${isSafeString}1"
    appendLine("execute if score $length $objective matches ${maxSafeCodeUnitLength + 1}..${maxSafeByteLength} run function ${isSafeString(isSafeString1)}")
    writeFunction(isSafeString1) {
      val isSafeString2 = "${isSafeString}2"
      appendLine("function ${isSafeString(isSafeString2)}")
      writeFunction(isSafeString2) {
        val scrutinee = "scrutinee"
        appendLine("data modify storage $mainStorage $scrutinee set string storage $mainStorage $input 0 1")
        appendLine("data modify storage $mainStorage $input set string storage $mainStorage $input 1")

        val isSafeString3 = "${isSafeString}3"
        appendLine("function ${isSafeString(isSafeString3)}")
        writeFunction(isSafeString3) {
          val count = r1
          appendLine("data modify storage $mainStorage $one set from storage $dataStorage $one")
          appendLine("execute store result score $count $objective run data modify storage $mainStorage $one[] set from storage $mainStorage $scrutinee")
          appendLine("data remove storage $mainStorage $one")
          appendLine("execute if score $count $objective matches ..${oneByteChars.size - 1} run return 0")

          appendLine("scoreboard players add $length $objective 1")

          appendLine("data modify storage $mainStorage $two set from storage $dataStorage $two")
          appendLine("execute store result score $count $objective run data modify storage $mainStorage $two[] set from storage $mainStorage $scrutinee")
          appendLine("data remove storage $mainStorage $two")
          appendLine("execute if score $count $objective matches ..${twoBytesChars.size - 1} run return 0")

          appendLine("scoreboard players add $length $objective 1")
        }

        appendLine("execute if score $length $objective matches ..${maxSafeByteLength} run function ${isSafeString(isSafeString2)}")
      }

      appendLine("execute store success storage $mainStorage $output byte 1 if data storage is_safe_string: {input: \"\"}")
    }

    appendLine("data remove storage $mainStorage $input")
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

@Serializable
data class LootTable(
  val type: String,
  val functions: List<Function>,
  val pools: List<Pool>,
) {
  @Serializable
  data class Function(
    val function: String,
    val tag: String,
  )

  @Serializable
  data class Pool(
    val entries: List<Entry>,
    val rolls: Int,
  ) {
    @Serializable
    data class Entry(
      val type: String,
      val name: String,
    )
  }
}
