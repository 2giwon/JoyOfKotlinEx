sealed class List<out A> {                  // sealed 클래스는 암묵적으로 추상클래스 이며, 생성자는 암묵적으로 비공개이다.
    abstract fun isEmpty(): Boolean     // 각 확장 클래스는 추상 isEmpty 함수를 다르게 구현한다.
    abstract fun drop(n: Int): List<A>
    fun cons(a: @UnsafeVariance A): List<A> = Cons(a, this)
    abstract fun concat(list: List<@UnsafeVariance A>): List<A>
    abstract fun forEach(ef: (A) -> Unit)

//    abstract class Empty<A> : advanced.List<A>() {
//        override fun concat(list: advanced.List<A>): advanced.List<A> = list
//    }

    fun setHead(a: @UnsafeVariance A): List<A> = when (this) {
        is Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> Cons(a, this.tail)
    }

    private object Nil : List<Nothing>() {
        override fun isEmpty(): Boolean = true
        override fun toString(): String = "[Nil]"
        override fun drop(n: Int): List<Nothing> = this
        override fun concat(list: List<Nothing>): List<Nothing> = list
        override fun forEach(ef: (Nothing) -> Unit) {}
    }

    private class Cons<A>(internal val head: A,
                          internal val tail: List<A>) : List<A>() {     // 비어 있지 않은 리스트를 표현하는 Cons확장 클래스
        override fun isEmpty(): Boolean = false
        override fun toString(): String = "[${toString("", this)}NIL]"
        private tailrec fun toString(acc: String, list: List<A>): String =
                when (list) {               // 공재귀 함수로 toString을 구현한다.
                    is Nil -> acc
                    is Cons -> toString("$acc${list.head}, ", list.tail)
                }

        override fun drop(n: Int): List<A> {
            tailrec fun drop(n: Int, list: List<A>): List<A> =
                    if (n <= 0) list else when (list) {
                        is Cons -> drop(n - 1, list.tail)
                        is Nil -> list
                    }
            return drop(n, this)
        }

        override fun concat(list: List<A>): List<A> = Cons(this.head, list.concat(this.tail))

        override fun forEach(ef: (A) -> Unit) {
            tailrec fun forEach(list: List<A>) {
                when (list) {
                    Nil -> {
                    }
                    is Cons -> {
                        ef(list.head)
                        forEach(list.tail)
                    }
                }
            }

            forEach(this)
        }
    }

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B =
            foldRight(this, identity, f)

    fun init(): List<A> = reverse().drop(1).reverse()

    fun length(): Int = foldRight(0) { { it + 1 } }

    fun length2(): Int = foldLeft(0) { acc -> { _ -> acc + 1 } }

    fun reverse(): List<A> = reverse(List.invoke(), this)

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = foldLeft(identity, this, f)

    fun reverse2(): List<A> = foldLeft(invoke()) { acc -> { acc.cons(it) } }

    fun <B> foldRightViaFoldLeft(identity: B, f: (A) -> (B) -> B): B =
            this.reverse2().foldLeft(identity) { acc -> { y -> f(y)(acc) } }

    fun <A, B> operation(list: List<A>, identity: B, operator: (A) -> (B) -> (B)): B =
            when (list) {
                List.Nil -> identity
                is List.Cons -> operator(list.head)(operation(list.tail, identity, operator))
            }

    fun sum(list: List<Int>): Int =
            foldRight(list, 0) { x -> { acc -> x + acc } }

    fun product(list: List<Double>): Double =
            foldRight(list, 1.0) { x -> { acc -> x * acc } }

    fun sum2(list: List<Int>): Int =
            foldLeft(0, list) { acc -> { y -> acc + y } }

    fun product2(list: List<Double>): Double =
            foldLeft(1.0, list) { acc -> { y -> acc * y } }

    fun <B> coFoldRight(identity: B, f: (A) -> (B) -> B): B = coFoldRight(identity, this.reverse2(), identity, f)

    fun <A> flatten(list: List<List<A>>): List<A> = list.foldRight(invoke()) { x -> { acc: List<A> -> x.concat(acc) } }

    fun filter(p: (A) -> Boolean): List<A> =
            foldRight(invoke()) { head ->
                { acc: List<A> ->
                    if (p(head)) Cons(head, acc) else acc
                }
            }

    fun <B> map(f: (A) -> B): List<B> =
            foldRight(invoke()) { head ->
                { acc: List<B> ->
                    Cons(f(head), acc)
                }
            }

    fun <B> map2(f: (A) -> B): List<B> =
            coFoldRight(invoke()) { head ->
                { acc: List<B> ->
                    Cons(f(head), acc)
                }
            }

    fun <B> flatMap(f: (A) -> List<B>): List<B> = flatten(map(f))

    fun filter2(p: (A) -> Boolean): List<A> = flatMap { condition -> if (p(condition)) List(condition) else invoke() }

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator
        fun <A> invoke(vararg az: A): List<A> = // operator 키워드를 사용해 선언한 invoke 함수는 클래스 이름()처럼 호출이 가능하다.
                // foldRight 함수의 첫 번째 인자로 쓰이는 Nil을 advanced.List<A>로 명시적으로 타입을 변환한다.
                az.foldRight(Nil as List<A>) { a: A, list: List<A> -> Cons(a, list) }

        tailrec fun <A> reverse(acc: List<A>, list: List<A>): List<A> =
                when (list) {
                    Nil -> acc
                    is Cons -> reverse(acc.cons(list.head), list.tail)
                }

        fun <A, B> foldRight(list: List<A>,             // A, B 타입을 나타낸다
                             identity: B,                // 접기 연산의 항등원
                             f: (A) -> (B) -> B): B =     // 연산자를 표현하는 함수로 커리한 형태
                when (list) {
                    List.Nil -> identity
                    is List.Cons -> f(list.head)(foldRight(list.tail, identity, f))
                }

        tailrec fun <A, B> foldLeft(acc: B, list: List<A>, f: (B) -> (A) -> B): B =
                when (list) {
                    List.Nil -> acc
                    is List.Cons -> foldLeft(f(acc)(list.head), list.tail, f)
                }

        private tailrec fun <A, B> coFoldRight(acc: B, list: List<A>, identity: B, f: (A) -> (B) -> B): B =
                when (list) {
                    List.Nil -> acc
                    is List.Cons -> coFoldRight(f(list.head)(acc), list.tail, identity, f)
                }

        fun <A> concatViaFoldRight(list1: List<A>, list2: List<A>): List<A> =
                foldRight(list1, list2) { x -> { acc -> Cons(x, acc) } }

        fun <A> concatViaFoldLeft(list1: List<A>, list2: List<A>): List<A> =
                list1.reverse2().foldLeft(list2) { acc -> acc::cons }

    }

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