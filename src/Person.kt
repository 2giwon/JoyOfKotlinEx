data class Person(val id: Int, val firstName: String, val lastName: String)

fun person(input: Input): Result<Pair<Person, Input>> =
        input.readInt("Enter your ID: ").flatMap { id ->
            id.second.readString("Enter your first Name: ").flatMap { name ->
                name.second.readString("Enter your last Name: ").map { lastName ->
                    Pair(Person(id.first, name.first, lastName.first), lastName.second)
                }
            }
        }