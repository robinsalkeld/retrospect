package edu.ubc.mirrors;

public interface Callback<T> {

    public Object handle(T t);
}
