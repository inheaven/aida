package ru.inheaven.aida.okex.util;

import com.google.common.collect.Queues;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

/**
 * @author Anatoly A. Ivanov
 * 16.01.2018 10:09
 */
public class Buffer<E> implements Iterable<E> {
    private Deque<E> deque;

    private int maxSize;

    public Buffer() {
        this(8);
    }

    public Buffer(int maxSize) {
        this.maxSize = maxSize;
        deque = Queues.synchronizedDeque(new ArrayDeque<>(maxSize));
    }

    public static <E> Buffer<E> create(){
        return new Buffer<>();
    }

    public static <E> Buffer<E> create(int maxSize){
        return new Buffer<>(maxSize);
    }

    public boolean add(E e) {
       if (deque.size() > maxSize){
           deque.removeFirst();
       }

       return deque.add(e);
    }

    public void addAll(Collection<E> collection){
        collection.forEach(this::add);
    }

    public E peekLast(){
        return deque.peekLast();
    }

    public Deque<E> getDeque() {
        return deque;
    }

    public boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return deque.iterator();
    }
}
