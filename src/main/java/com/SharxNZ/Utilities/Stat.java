package com.SharxNZ.Utilities;

public class Stat <T>{
    private T value;

    public Stat(){}

    public Stat(T value){
        this.value = value;
    }

    public T get(){
        return value;
    }

    public void set(T value){
        this.value = value;
    }
}
