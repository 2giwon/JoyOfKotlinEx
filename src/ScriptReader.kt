import advanced.getOrElse

class ScriptReader : Input {

    constructor(commands: advanced.List<String>) : super() {
        this.commands = commands
    }

    constructor(vararg commands: String) : super() {
        this.commands = advanced.List(*commands)
    }

    private val commands: advanced.List<String>

    override fun readString(): Result<Pair<String, Input>> = when {
        commands.isEmpty() ->
            Result.failure("Not enough entries in script")
        else -> Result(Pair(
                commands.headSafe().getOrElse(""),
                ScriptReader(commands.drop(1))))
    }

    override fun readInt(): Result<Pair<Int, Input>> = try {
        when {
            commands.isEmpty() ->
                Result.failure("Not enough entries in script")
            Integer.parseInt(commands.headSafe().getOrElse("")) >= 0 ->
                Result(Pair(Integer.parseInt(
                        commands.headSafe().getOrElse("")),
                        ScriptReader(commands.drop(1))))
            else -> Result()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun close() {

    }

}

fun readPersonFromScript(vararg commands: String): List<Person> =
        Stream.unfold(ScriptReader(*commands), ::person).toList()