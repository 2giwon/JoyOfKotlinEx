fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean = if (a()) true else b()

fun and(a: Boolean, b: Boolean): Boolean = if (a) b else false

fun getSecond(): Boolean = throw IllegalStateException()

val first: Boolean by lazy { true }

class Lazy<out A>(function: () -> A) : () -> A {
    private val value: A by lazy(function)
    override operator fun invoke(): A = value

    fun <B> map(f: (A) -> B): Lazy<B> = Lazy { f(value) }
    fun <B> flatMap(f: (A) -> Lazy<B>): Lazy<B> = Lazy { f(value)() }

    companion object {
        fun <A> sequenceResult(list: List<Lazy<A>>): Lazy<Result<List<A>>> =
                Lazy {
                    list.foldRight(Result(List())) { x: Lazy<A> ->
                        { y: Result<List<A>> ->
                            map2(Result.of(x), y) { a: A ->
                                { b: List<A> ->
                                    b.cons(a)
                                }
                            }
                        }
                    }
                }

        fun <A> sequence(list: List<Lazy<A>>): Lazy<List<A>> = Lazy { list.map { it() } }
        val lift2: ((String) -> (String) -> String) -> (Lazy<String>) -> (Lazy<String>) -> Lazy<String> =
                { f: (String) -> (String) -> String ->
                    { lazy1 ->
                        { lazy2 ->
                            Lazy { f(lazy1())(lazy2()) }
                        }

                    }
                }

        fun <A, B, C> lift2(f: (A) -> (B) -> C): (Lazy<A>) -> (Lazy<B>) -> Lazy<C> =
                { lazy1 ->
                    { lazy2 ->
                        Lazy { f(lazy1())(lazy2()) }

                    }
                }
    }
}

val constructMessage: (Lazy<String>) -> (Lazy<String>) -> Lazy<String> =
        { greeting ->
            { name ->
                Lazy { "${greeting()}, ${name()}" }
            }
        }





