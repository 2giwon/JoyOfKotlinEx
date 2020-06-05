import java.io.BufferedReader

abstract class AbstractReader(private val reader: BufferedReader) : Input {     // 여러 다른 입력 소스를 사용 할 수 있도록
    /*   리더에서 한 줄을 읽어서 Result를 반환
     *   줄이 비어 있으면 Empty를 반환,
     *   데이터가 들어 있으면 Result.Success를 반환
     *   뭔가 잘못되면 Result.Failure 를 반환
     */
    override fun readString(): Result<Pair<String, Input>> = try {
        reader.readLine().let {
            when {
                it.isEmpty() -> Result()
                else -> Result(Pair(it, this))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun readInt(): Result<Pair<Int, Input>> = try {
        reader.readLine().let {
            when {
                it.isEmpty() -> Result()
                else -> Result(Pair(it.toInt(), this))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun close() = reader.close()                   // BufferedReader의 close
}