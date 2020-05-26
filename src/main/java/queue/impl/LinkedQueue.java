package queue.impl;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Iterator;

public class LinkedQueue extends AbstractQueue<Integer> {
    private static class Node {
        Node next;
        Integer value;

        Node(Integer x) {
            value = x;
        }
    }

    Node preFirst;
    Node last;
    int size;

    public LinkedQueue() {
        preFirst = last = new Node(null);
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            Node cur = preFirst;

            @Override
            public boolean hasNext() {
                return cur.next != null;
            }

            @Override
            public Integer next() {
                if (!hasNext()) return null;
                cur = cur.next;
                return cur.value;
            }
        };
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean offer(Integer value) {
        Node cur = new Node(value);
        last.next = cur;
        last = cur;
        size++;
        return true;
    }

    @Override
    public Integer poll() {
        if (isEmpty()) return null;
        preFirst = preFirst.next;
        size--;
        return preFirst.value;
    }

    @Override
    public Integer peek() {
        return isEmpty() ? null : preFirst.next.value;
    }
}
