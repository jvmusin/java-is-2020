package impl.pair;

public class NumberPairImpl<T extends Number, K extends Number> extends PairImpl<T, K> {
    public NumberPairImpl(T first, K second) {
        super(first, second);
    }
}