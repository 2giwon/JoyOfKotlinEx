import java.io.Closeable

interface Input : Closeable {
    fun readString(): Result<Pair<String, Input>>       // 차례대로 정수와 문자열을 입력한다.
    fun readInt(): Result<Pair<Int, Input>>
    fun readString(message: String): Result<Pair<String, Input>> = readString()     // 메시지를 파라미터로 넘긴다
    fun readInt(message: String): Result<Pair<Int, Input>> = readInt()
}