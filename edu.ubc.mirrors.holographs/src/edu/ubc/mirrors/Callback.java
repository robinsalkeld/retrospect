package edu.ubc.mirrors;

public interface Callback<T> {

    public T handle(T t);
}
