sealed class Stream<out A> {                    // Stream 클래스를 같은 파일이나 내포 클래스가 아니면 직접 상속할 수 없다.

    abstract fun isEmpty(): Boolean

    abstract fun head(): Result<A>              // head함수는 스트림이 비어 있는 경우 Empty를 반환해야하므로 Result<A>를 반환타입으로 사용

    abstract fun tail(): Result<Stream<A>>      // 같은 이유로 tail함수도 Result<Stream<A>> 를 반환타입으로 한다.

    abstract fun takeAtMost(n: Int): Stream<A>

    abstract fun dropAtMost(n: Int): Stream<A>

    abstract fun toList(): List<A>

    abstract fun takeWhile(p: (A) -> Boolean): Stream<A>

    abstract fun dropWhile(p: (A) -> Boolean): Stream<A>

    abstract fun exists(p: (A) -> Boolean): Boolean

    abstract fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B

    private object Empty : Stream<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun head(): Result<Nothing> = Result()

        override fun tail(): Result<Stream<Nothing>> = Result()

        override fun takeAtMost(n: Int): Stream<Nothing> = this

        override fun dropAtMost(n: Int): Stream<Nothing> = this

        override fun toList(): List<Nothing> = toList(this)

        override fun takeWhile(p: (Nothing) -> Boolean): Stream<Nothing> = this

        override fun dropWhile(p: (Nothing) -> Boolean): Stream<Nothing> = this

        override fun exists(p: (Nothing) -> Boolean): Boolean = exists(this, p)

        override fun <B> foldRight(z: Lazy<B>, f: (Nothing) -> (Lazy<B>) -> B): B = z()
    }

    private class Cons<out A>(
            internal val hd: Lazy<A>,                // 비어 있지 않은 스트림을 Cons 하위클래스로 표현
            internal val tl: Lazy<Stream<A>>
    ) : Stream<A>() {
        override fun isEmpty(): Boolean = false

        override fun head(): Result<A> = Result(hd())

        override fun tail(): Result<Stream<A>> = Result(tl())

        override fun takeAtMost(n: Int): Stream<A> = when {
            n > 0 -> cons(hd, Lazy { tl().takeAtMost(n - 1) })
            else -> Empty
        }

        override fun dropAtMost(n: Int): Stream<A> = dropAtMost(n, this)

        override fun toList(): List<A> = toList(this)

        override fun takeWhile(p: (A) -> Boolean): Stream<A> = when {
            p(hd()) -> cons(hd, Lazy { tl().takeWhile(p) })
            else -> Empty
        }

        override fun dropWhile(p: (A) -> Boolean): Stream<A> = dropWhile(this, p)

        override fun exists(p: (A) -> Boolean): Boolean = exists(this, p)

        override fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B =
                f(hd())(Lazy { tl().foldRight(z, f) })
    }

    fun takeWhileViaFoldRight(p: (A) -> Boolean): Stream<A> =
            foldRight(Lazy { Empty }) { a ->
                { b: Lazy<Stream<A>> ->
                    if (p(a)) cons(Lazy { a }, b) else Empty
                }
            }

    fun headSafeViaFoldRight(): Result<A> =
            foldRight(Lazy { Result<A>() }) { a -> { Result(a) } }

    fun <B> map(f: (A) -> B): Stream<B> =
            foldRight(Lazy { Empty }) { a ->
                { b: Lazy<Stream<B>> -> cons(Lazy { f(a) }, b) }
            }

    fun filter(p: (A) -> Boolean): Stream<A> =
            foldRight(Lazy { Empty }) { a ->
                { b: Lazy<Stream<A>> ->
                    if (p(a)) cons(Lazy { a }, b) else b()
                }
            }

    fun append(stream2: Lazy<Stream<@UnsafeVariance A>>): Stream<A> =
            this.foldRight(stream2) { a: A ->
                { b: Lazy<Stream<A>> ->
                    Stream.cons(Lazy { a }, b)
                }
            }

    fun <B> flatMap(f: (A) -> Stream<B>): Stream<B> =
            foldRight(Lazy { Empty }) { a ->
                { b: Lazy<Stream<B>> ->
                    f(a).append(b)
                }
            }

    fun find(p: (A) -> Boolean): Result<A> = filter(p).head()

    fun fibs(): Stream<Int> = iterate(Pair(1, 1)) { (x, y) -> Pair(x, x + y) }.map { it.first }

    fun filter2(p: (A) -> Boolean): Stream<A> =
            dropWhile { x -> !p(x) }.let { stream ->
                when (stream) {
                    is Empty -> stream
                    is Cons -> stream.head().map { a ->
                        cons(Lazy { a }, Lazy {
                            stream.tl().filter(p)
                        })
                    }.getOrElse(Empty)
                }
            }

    companion object {
        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> = Cons(hd, tl)

        operator fun <A> invoke(): Stream<A> = Empty

        fun from(n: Int): Stream<Int> =                     // from 팩토리 함수는 주어진 값으로 시작하는 연속적인 정수로 이뤄진 무한 스트림
                iterate(1) { it + 1 }

        fun <A> repeat(f: () -> A): Stream<A> = cons(Lazy { f() }, Lazy { repeat(f) })

        tailrec fun <A> dropAtMost(n: Int, stream: Stream<A>): Stream<A> = when {
            n > 0 -> when (stream) {
                is Empty -> stream
                is Cons -> dropAtMost(n - 1, stream.tl())
            }
            else -> stream
        }

        fun <A> toList(stream: Stream<A>): List<A> {
            tailrec fun <A> toList(list: List<A>, stream: Stream<A>): List<A> =
                    when (stream) {
                        Empty -> list
                        is Cons -> toList(list.cons(stream.hd()), stream.tl())
                    }
            return toList(List(), stream).reverse2()
        }

        fun <A> iterate(seed: A, f: (A) -> A): Stream<A> =
                cons(Lazy { seed }, Lazy { iterate(f(seed), f) })

        fun <A> iterate(seed: Lazy<A>, f: (A) -> A): Stream<A> =
                cons(seed, Lazy { iterate(f(seed()), f) })

        tailrec fun <A> dropWhile(stream: Stream<A>, p: (A) -> Boolean): Stream<A> =
                when (stream) {
                    is Empty -> stream
                    is Cons -> when {
                        p(stream.hd()) -> dropWhile(stream.tl(), p)
                        else -> stream
                    }
                }

        tailrec fun <A> exists(stream: Stream<A>, p: (A) -> Boolean): Boolean =
                when (stream) {
                    Empty -> false
                    is Cons -> when {
                        p(stream.hd()) -> true
                        else -> exists(stream.tl(), p)
                    }
                }

        fun <A, S> unfold(z: S, f: (S) -> Result<Pair<A, S>>): Stream<A> =
                f(z).map { (a, s) -> cons(Lazy { a }, Lazy { unfold(s, f) }) }.getOrElse(Empty)
    }

}