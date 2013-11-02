package edu.ubc.mirrors;

public interface Callback<T> {

    public void handle(T t);
}
