import kotlin.random.Random

fun main(args: Array<String>) {
//    val ra = Result(4)          // 실패할 수 있는 함수가 반환하는 데이터를 시뮬레이션 한다.
//    val rb = Result(0)
//
//    val inserse: (Int) -> Result<Double> = { x ->
//        when {
//            x != 0 -> Result(1.toDouble() / x)
//            else -> Result.failure("Division by 0")
//        }
//    }
//
//    val showResult: (Double) -> Unit = ::println
//    val showError: (RuntimeException) -> Unit = { println("Error - ${it.message}") }
//
//    val rt1 = ra.flatMap(inserse)
//    val rt2 = rb.flatMap(inserse)
//
//    print("Inverse of 4: ")
//    rt1.forEach(showResult, showError)              // 결과 값 출력
//
//    print("Inverse of 0: ")
//    rt2.forEach(showResult, showError)

    val input = ConsoleReader()                         // 리더 생성
    /*
        readString 사용자 프롬프트를 사용해 호출하고 결과로 Result<Tuple<String, Input>>를 받는다.
        이 결과를 Result<String>을 만들기위해 매핑한다.
     */
//    val rString = input.readString("Enter your Name: ").map { t -> t.first }
//
//    /*
//        프로그램의 비즈니스 로직( 사용자 관점에서 볼 때 프로그램이 수행해야 하는 일) 부분
//     */
//    val nameMessage = rString.map { "Hello $it!" }
//    nameMessage.forEach(::println, onFailure = { println(it.message) })     // 앞에서 배운 패턴의 결과가 오류메시지에 적용
//    val rInt = input.readInt("Enter your age: ").map { t -> t.first }
//    val ageMessage: Result<String> = rInt.map { "YOU look younger than $it!" }
//    ageMessage.forEach(::println, onFailure = { println("Invalid age. Please enter an integer") })

//    readPersonFromConsole().forEach (::println)
//
//    val path = "data.txt"
//    readPersonFromFile(path).forEach(
//            onSuccess = { list: List<Person> -> list.forEach(::println) },
//            onFailure = ::println
//    )

    readPersonFromScript(
            "1", "Mickey", "Mouse",
            "2", "Minnie", "Mouse",
            "3", "Donald", "Duck").forEach(::println)


    val instruction = IO { print("Hello, ") }
    val instruction2 = IO { print(getName()) }
    val instruction3 = IO { print("!\n") }

    val script: IO<Unit> = instruction + instruction2 + instruction3

    script()

//    val program: IO = script.foldRight(IO.empty) { io -> { io + it } }

//    val script2 = sayHello()
//    script2()

    val program = IO.repeat(3, sayHello())
    program()
}

private fun sayHello(): IO<Unit> = IO.Console.print("Enter your Name: ")
        .flatMap { IO.Console.readIn() }
        .map { buildMessage(it) }
        .flatMap { IO.Console.println(it) }

private fun buildMessage(name: String): String = "Hello, $name"

private val f = { x: Int ->
    println("Mapping $x")
    x * 3
}

private val p = { x: Int ->
    println("Filtering $x")
    x % 2 == 0
}

fun listTest() {
    val list = List<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    println(list)
    println(list.sum2(List(1, 2, 3, 4, 5, 6, 7, 8)))
    println(list.product2(List(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)))

    println(list.setHead(33))
    println(list.init())
    println(list.length2())
    println(list.reverse2())
    println(list.concat(List(12, 3, 4, 5, 45)))
    println(List.concatViaFoldLeft(list, List(11, 22, 33, 44, 55)))
    println(triple(list))

    println(list.drop(2))

    println(List(1.0, 2.0, 3.0, 4.0).apply { doubleToString(this) })

    println(List(1, 2, 3, 4, 5).filter { it > 3 })

    println(List(1, 2, 3, 5).map { it + 1 })

    println(List(1, 2, 3, 4, 5).map2 { it + 1 })

    println(List(1, 2, 3, 4, 5).flatMap { i -> List(i, -i) })
}

fun lazyTest() {
    val first = Lazy {
        println("Evaluating first")
        true
    }

    val second = Lazy {
        println("Evaluating second")
        throw IllegalStateException()
    }

    println(first() || second())
    println(first() || second())
    println(or(first, second))

    val greetings = Lazy {
        println("Evaluating greetings")
        "Hello"
    }

    val name1: Lazy<String> = Lazy {
        println("Evaluating name")
        "Mickey"
    }

    val name2: Lazy<String> = Lazy {
        println("Evaluating name")
        "Donald"
    }

    val defaultMessage = Lazy {
        println("Evaluating default message")
        "No greetings when time is odd"
    }

//    val message1 = constructMessage(greetings, name1)
//    val message2 = constructMessage(greetings, name2)
//    val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0
//    println(if (condition) message1() else defaultMessage())
//    println(if (condition) message1() else defaultMessage())
//    println(if (condition) message2() else defaultMessage())

    val greetingMessage = constructMessage(greetings)
    val message1 = greetingMessage(name1)
    val message2 = greetingMessage(name2)
    val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0
    println(if (condition) message1() else defaultMessage())
    println(if (condition) message2() else defaultMessage())

//    val greets: (String) -> String = { "Hello, $it" }
//    val name: Lazy<String> = Lazy {
//        println("Evaluating name")
//        "Mickey"
//    }

//    val message = name.map(greets)
//    println(if (condition) message() else defaultMessage())
//    println(if (condition) message() else defaultMessage())

//    val getGreetings: (Locale) -> String = {
//        println("Evaluating greetings")
//        "asdfasdfsdfasdf"
//    }
//    val greetings2: Lazy<String> = Lazy{ getGreetings(Locale.US) }
//    val flatGreets: (String) -> Lazy<String> =
//        { name3 -> greetings2.map { "$it, $name3" } }
//
//    val name4: Lazy<String> = Lazy {
//        println("computing name")
//        "Mickey4"
//    }
//
//    val message2 = name4.flatMap(flatGreets)
//    println(if (condition) message2() else defaultMessage())
//    println(if (condition) message2() else defaultMessage())

    val name5: Lazy<String> = Lazy {
        println("Evaluating name5")
        "Mickey"
    }
    val name6: Lazy<String> = Lazy {
        println("Evaluating name6")
        "Donald"
    }
    val name7 = Lazy {
        println("Evaluating name7")
        "Goofy"
    }

    val list5 = Lazy.sequence(List(name5, name6, name7))
    val defaultMessage2 = "No greetings when time is odd"
    println(if (condition) list5() else defaultMessage2)
    println(if (condition) list5() else defaultMessage2)

    val stream = Stream.from(1)
    stream.head().forEach({ println(it) })
    stream.tail().flatMap { it.head() }.forEach({ println(it) })
    stream.tail().flatMap { it.tail().flatMap { it.head() } }.forEach({ println(it) })
    stream.head().forEach({ println(it) })
    stream.tail().flatMap { it.head() }.forEach({ println(it) })
    stream.tail().flatMap { it.tail().flatMap { it.head() } }.forEach({ println(it) })

    fun inc(i: Int): Int = (i + 1).let {
        println("generating $it")
        it
    }

    val list =
            Stream.iterate(0, ::inc)
                    .takeAtMost(60000)
                    .dropAtMost(10000)
                    .takeAtMost(10)
                    .toList()

    println(list)

    val list10 = List(1, 2, 3, 4, 5).map(f).filter(p)
    println(list10)

    val stream10 = Stream.from(1).takeAtMost(5).map(f).filter(p)
    println(stream10.toList())
}