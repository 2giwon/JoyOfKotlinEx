import java.io.BufferedReader
import java.io.File

class FileReader private constructor(private val reader: BufferedReader) : AbstractReader(reader), AutoCloseable {
    override fun close() {
        reader.close()
    }

    companion object {
        operator fun invoke(path: String): Result<Input> = try {
            Result(FileReader(File(path).bufferedReader()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun readPersonFromFile(path: String): Result<List<Person>> =
        FileReader(path).map { input ->
            input.use {
                Stream.unfold(it, ::person).toList()
            }
        }

