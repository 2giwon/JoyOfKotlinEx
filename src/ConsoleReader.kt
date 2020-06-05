import java.io.BufferedReader
import java.io.InputStreamReader

class ConsoleReader(reader: BufferedReader) : AbstractReader(reader) {
    override fun readString(message: String): Result<Pair<String, Input>> {     // 두 개의 기본 함수를 이용하여 콘솔에 표시한다
        print("$message ")
        return readString()
    }

    override fun readInt(message: String): Result<Pair<Int, Input>> {
        print("$message ")
        return readInt()
    }

    companion object {
        operator fun invoke(): ConsoleReader = ConsoleReader(BufferedReader(InputStreamReader(System.`in`)))
    }
}

private fun readPersonFromConsole(): List<Person> =
        Stream.unfold(ConsoleReader(), ::person).toList()