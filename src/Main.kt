import kotlin.random.Random

fun main(args: Array<String>) {
    val ra = Result(4)          // 실패할 수 있는 함수가 반환하는 데이터를 시뮬레이션 한다.
    val rb = Result(0)

    val inserse: (Int) -> Result<Double> = { x ->
        when {
            x != 0 -> Result(1.toDouble() / x)
            else -> Result.failure("Division by 0")
        }
    }

    val showResult: (Double) -> Unit = ::println
    val showError: (RuntimeException) -> Unit = { println("Error - ${it.message}") }

    val rt1 = ra.flatMap(inserse)
    val rt2 = rb.flatMap(inserse)

    print("Inverse of 4: ")
    rt1.forEach(showResult, showError)              // 결과 값 출력

    print("Inverse of 0: ")
    rt2.forEach(showResult, showError)
}

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

fun triple(list: List<Int>): List<Int> =
        List.foldRight(list, List.invoke()) {
            { acc: List<Int> ->
                acc.cons(it * 3)
            }
        }

fun doubleToString(list: List<Double>): List<String> =
        List.foldRight(list, List()) { head ->
            { acc: List<String> ->
                acc.cons(head.toString())
            }
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