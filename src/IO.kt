import List.Companion.cons
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/*
       IO클래스를 파라미터화 한다. IO를 생성할 때는 인자로 받는 함수는 IO클래스의 타입파라미터에 해당하는
       타입 인스턴스를 반환하는 함수다.
 */
class IO<out A>(private val f: () -> A) {

    operator fun invoke() = f()

//    operator fun invoke(io: IO<@UnsafeVariance A>): A {
//        tailrec fun invokeHelper(io: IO<A>): A = when (io) {
//            is Return -> io.value                   // 받은 IO가 Return이면 계산이 끝났기 때문에 값을 반환
//            is Suspend -> io.resume()               // 받은 IO가 Suspend면 내부에 들어있는 효과를 먼저 실행해서 받은 값을 반환
//            else -> {
//                val ct = io as Continue<A, A>       // 받은 IO가 Continue이면 내부에 들어있는 subIO를 먼저 읽는다.
//                val sub = ct.sub
//                val f = ct.f
//                when (sub) {
//                    // sub가 Return이면 sub에 담긴 값을 f에 적용해 결과를 재귀로 호출
//                    is Return -> invokeHelper(f(sub.value))
//                    // 받은 IO가 Suspend면 sub를 계산한 결과에 f를 적용한 결과를 재귀로 호출
//                    is Suspend -> invokeHelper(f(sub.resume()))
//                    else -> {
//                        val ct2 = sub as Continue<A, A>       // sub가 Continue이면 내부에 있는 IO를 추출하고 체인을 만들어내는 sub와 flatmap한다.
//                        val sub2 = ct2.sub
//                        val f2 = ct2.f
//                        invokeHelper(sub2.flatMap { f2(it).flatMap(f) })
//
//                    }
//                }
//            }
//        }
//
//        return invokeHelper(io)
//    }

//    fun <B> map(f: (A) -> B): IO<B> = flatMap { return (f(it)) }
//    fun <B> flatMap(f: (A) -> IO<B>): IO<B> = Continue(this, f) as IO<B>
//
//    class IORef<A>(private var value: A) {
//        fun set(a: A): IO<A> {
//            value = a
//            return unit(a)
//        }
//
//        fun get(): IO<A> = unit(value)
//        fun modify(f: (A) -> A): IO<A> = get().flatMap({ a -> set(f(a)) })
//    }

//    internal class Return<out A>(val value: A) : IO<A>()         // 계산이 반환할 값
//
//    internal class Suspend<out A>(val resume: () -> A) : IO<A>() // 인자를 받지 않고 효과를 적용하여 값을 반환
//
//    internal class Continue<A, out B>(val sub: IO<A>, val f: (A) -> IO<B>): IO<A>()

    fun show(message: String): IO<Unit> = IO { println(message) }

    fun <A> toString(rd: Result<A>): String =
            rd.map { it.toString() }.getOrElse { rd.toString() }

    fun inverse(i: Int): Result<Double> = when (i) {
        0 -> Result.failure("Div by 0")
        else -> Result(1.0 / i)
    }

    operator fun <A> plus(io: IO<A>): IO<A> = IO {
        f()
        io.f()
    }

    fun <B> map(g: (A) -> B): IO<B> = IO {
        g(this())
    }

    fun <B> flatMap(g: (A) -> IO<B>): IO<B> = IO {
        g(this())()
    }

    object Console {
        private val br = BufferedReader(InputStreamReader(System.`in`))

        fun readIn(): IO<String> = IO {
            try {
                br.readLine()
            } catch (e: IOException) {
                throw IllegalStateException(e)
            }
        }

        fun println(o: Any): IO<Unit> = IO { kotlin.io.println(o.toString()) }
        fun print(o: Any): IO<Unit> = IO { kotlin.io.print(o.toString()) }
    }

    companion object {
        /*
            empty 인스턴스는 타입 파라미터로 Unit을 지정하고,
            아무것도 반환하지 않는 함수를 IO의 생성자 인자로 사용해 만든다.
            (Nothing을 반환하는 타입과 이 함수가 다르다는 점에 유의)
         */
        val empty: IO<Unit> = IO {}

        /*
            동반 객체에 있는 invoke 함수는 단순 값을 IO로 감싸서 반환
         */
        operator fun <A> invoke(a: A): IO<A> = IO { a }

        fun <A, B, C> ioMap(ioa: IO<A>, iob: IO<B>, f: (A) -> (B) -> C): IO<C> =
                ioa.flatMap { a: A ->
                    iob.map { b: B ->
                        f(a)(b)
                    }
                }

        fun <A> repeat(n: Int, io: IO<A>): IO<List<A>> {
            val stream: Stream<IO<A>> = Stream.fill(n, Lazy { io })

            val f: (A) -> (List<A>) -> List<A> =
                    { a: A ->
                        { listA: List<A> -> cons(a, listA) }
                    }

            val g: (IO<A>) -> (Lazy<IO<List<A>>>) -> IO<List<A>> =
                    { ioA ->
                        { lazyIoListA: Lazy<IO<List<A>>> ->
                            ioMap(ioA, lazyIoListA(), f)
                        }
                    }

            val z: Lazy<IO<List<A>>> = Lazy { IO { List<A>() } }
            return stream.foldRight(z, g)
        }

        fun <A, B> forever(ioa: IO<A>): IO<B> {
            return ioa.flatMap {
                {
                    ioa.flatMap {
                        {
                            forever<A, B>(ioa)
                        }()
                    }
                }()
            }
        }

//        internal fun <A> unit(a: A): IO<A> = IO.Suspend { a}
    }
}

fun getName() = "Mickey"
