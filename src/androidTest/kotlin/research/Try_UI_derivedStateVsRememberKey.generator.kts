import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

println()
println("-------------------------------------------------------------------------------------------")
println()

val class_name = "Try_UI_derviedStateVsRememberKey"
val file = class_name + ".kt"
val file_template = class_name + ".kt"
val file_backup = class_name + ".kt.backup"
val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))

val composables = listOf(

    "DerivedState_from_Value:                   val value by remember                            { derivedStateOf { externalValue             } }",
    "DerivedState_from_Param:                   val value by remember                            { derivedStateOf { param                     } }",
    "RememberKey_Value_stateofValue:            val value by remember(externalValue            ) { mutableStateOf ( externalValue             ) }",
    "RememberKey_Value_stateofValue10:          val value by remember(externalValue            ) { mutableStateOf ( externalValue        / 10 ) }",
    "RememberKey_Value10_stateofValue10:        val value by remember(externalValue        / 10) { mutableStateOf ( externalValue        / 10 ) }",
    "RememberKey_Value_Value:                   val value =  remember(externalValue            ) {                  externalValue               }",
    "RememberKey_Value_Value10:                 val value =  remember(externalValue            ) {                  externalValue        / 10   }",
    "RememberKey_Value10_Value10:               val value =  remember(externalValue        / 10) {                  externalValue        / 10   }",
    "Remember__stateofParam:                    val value by remember                            { mutableStateOf ( param                     ) }",
    "Remember__stateofParam10:                  val value by remember                            { mutableStateOf ( param                / 10 ) }",
    "Remember__Param:                           val value =  remember                            { param                                        }",
    "Remember__Param10:                         val value =  remember                            { param                                 / 10   }",
    "DerivedState_State:                        val value by remember                            { derivedStateOf { externalState.value       } }",
    "DerivedState_State10:                      val value by remember                            { derivedStateOf { externalState.value  / 10 } }",
    "DerivedState_StateValue:                   val value by remember                            { derivedStateOf { externalStateValue        } }",
    "DerivedState_StateValue10:                 val value by remember                            { derivedStateOf { externalStateValue   / 10 } }",
    "DerivedState_byState:                      val value by remember                            { derivedStateOf { externalByState           } }",
    "DerivedState_byState10:                    val value by remember                            { derivedStateOf { externalByState      / 10 } }",
    "DerivedState_byStateValue:                 val value by remember                            { derivedStateOf { externalByStateValue      } }",
    "DerivedState_byStateValue10:               val value by remember                            { derivedStateOf { externalByStateValue / 10 } }",
    "RememberKey_State_stateofState:            val value by remember(externalState.value      ) { mutableStateOf ( externalState.value       ) }",
    "RememberKey_State_stateofState10:          val value by remember(externalState.value      ) { mutableStateOf ( externalState.value  / 10 ) }",
    "RememberKey_State10_stateofState10:        val value by remember(externalState.value  / 10) { mutableStateOf ( externalState.value  / 10 ) }",
    "RememberKey_State_State:                   val value =  remember(externalState.value      ) {                  externalState.value         }",
    "RememberKey_State_State10:                 val value =  remember(externalState.value      ) {                  externalState.value  / 10   }",
    "RememberKey_State10_State10:               val value =  remember(externalState.value  / 10) {                  externalState.value  / 10   }",
    "RememberKey_byState_byState:               val value =  remember(externalByState          ) {                  externalByState             }",
    "RememberKey_byState_byState10:             val value =  remember(externalByState          ) {                  externalByState      / 10   }",
    "RememberKey_byState10_byState10:           val value =  remember(externalByState      / 10) {                  externalByState      / 10   }",
    "RememberKey_byStateValue_byStateValue:     val value =  remember(externalByStateValue     ) {                  externalByStateValue        }",
    "RememberKey_byStateValue_byStateValue10:   val value =  remember(externalByStateValue     ) {                  externalByStateValue / 10   }",
    "RememberKey_byStateValue10_byStateValue10: val value =  remember(externalByStateValue / 10) {                  externalByStateValue / 10   }",
    "RememberKey_Param_stateofParam:            val value by remember(param                    ) { mutableStateOf ( param                     ) }",
    "RememberKey_Param_stateofParam10:          val value by remember(param                    ) { mutableStateOf ( param                / 10 ) }",
    "RememberKey_Param10_stateofParam10:        val value by remember(param                / 10) { mutableStateOf ( param                / 10 ) }",
    "RememberKey_Param_Param:                   val value =  remember(param                    ) { param                                        }",
    "RememberKey_Param_Param10:                 val value =  remember(param                    ) { param                                 / 10   }",
    "RememberKey_Param10_Param10:               val value =  remember(param                / 10) { param                                 / 10   }",
    "Param:                                     val value =                                        param                                         ",
    "Param10:                                   val value =                                        param                                 / 10    ",

    )
    .map {
        val (name, prepare) = it.split(Regex(": *"), limit = 2)
        Pair(name, prepare)
    }
    .toMap()

val functions = composables.map { (name, prepare) ->

    val args = if (prepare.contains("param")) "(param: Int)" else "()"

    val prepare = prepare.replace(Regex(""" +"""), " ")

    """
    @Composable
    fun ${name}${args} {
        ${prepare}
        Row {
            Text(
                text = "   ${'$'}value   ",
                textAlign = TextAlign.End,
                modifier = Modifier
                    .width(70.dp)
                    .recomposeHighlighter()
            )
            Text(" ${name}")
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    """
}.joinToString("\n")

val indent = "        "

val invocations = composables.map { (name, prepare) ->

    val args = if (prepare.contains("param")) "(externalValue)" else "()"

    indent + "${name}${args}"

}.joinToString("\n")

var text = File(file_template).readText()

//File(file_backup).writeText(text)

var changes = 0

// use matchEntire instead of replace because replacement can contain '$'

run {
    val match = Regex(
        """(.*^ *//functions-begin *$).*(^ *//functions-end *$.*)""",
        options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
    ).matchEntire(text)
    if (match != null) {
        text = match.groups[1]!!.value + functions + match.groups[2]!!.value
        changes++
    } else
        println("no change in functions")
}

run {
    val match = Regex(
        """(.*^ *//invocations-begin *$).*(^ *//invocations-end *$.*)""",
        options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
    ).matchEntire(text)
    if (match != null) {
        text =
            match.groups[1]!!.value +
                    "\n        Text(\"\")\n\n$invocations\n\n        Text(\"\")\n        Text(\"generated: ${time}\")\n" +
                    match.groups[2]!!.value
        changes++
    } else
        println("no change in invocations")
}

if (changes > 0) {
    File(file).writeText(text)
    println(text)
} else
    println("no changes!")
