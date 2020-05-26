package queue.impl;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Iterator;

public class ArrayQueue extends AbstractQueue<Integer> {
    private static final int INITIAL_SIZE = 1;
    private Integer[] array = new Integer[INITIAL_SIZE];
    private int first;
    private int size;

    private int get(int at) {
        return array[(first + at) % array.length];
    }

    private void grow() {
        Integer[] a = new Integer[size * 2];
        for (int i = 0; i < size; i++) a[i] = get(i);
        first = 0;
        array = a;
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            int at = 0;

            @Override
            public boolean hasNext() {
                return at + 1 < size;
            }

            @Override
            public Integer next() {
                if (!hasNext()) return null;
                return array[at++];
            }
        };
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(Integer value) {
        if (array.length == size) grow();
        array[(first + size++) % array.length] = value;
        return true;
    }

    @Override
    public Integer poll() {
        if (isEmpty()) return null;
        Integer res = get(0);
        first++;
        size--;
        return res;
    }

    @Override
    public Integer peek() {
        if (isEmpty()) return null;
        return get(0);
    }
}
