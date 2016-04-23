package com.FastAlgorithms;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jeremy on 4/23/2016.
 */
public class ListOfNearestNeighbors {
    private KList[] list;
    private int numSamples, size;
    public ListOfNearestNeighbors(int numberofsamples, int size){
        list = new KList[numberofsamples];
        for(int i = 0; i < numberofsamples; i++){
            list[i] = new KList(size);
        }
        this.size = size;
        numSamples = numberofsamples;
    }

    public void push(int datapoint, int indexofNeighbor)throws Exception{
        list[datapoint].add(indexofNeighbor);
    }
    public int pop(int datapoint){
        return list[datapoint].remove();
    }

    private void validateDimensions(int i, int j)throws Exception{
        if(i < 0 || i >= numSamples){
            throw new Exception("Invalid Index at point " + i);
        }
        if(j < 0 || j >= size){
            throw new Exception("Attempted access of List element " + j);
        }
    }

    public void print(){
        for(int i = 0; i < numSamples; i++){
            System.out.println("List " + i);
            list[i].print();
        }
    }
}

class KList{
    private int MAX_SIZE;
    private int[] data;
    private AtomicInteger top = new AtomicInteger(0);
    public KList(int size){
        MAX_SIZE = size;
        data = new int[MAX_SIZE];
    }
    public void add(int item) throws Exception{
        while(true) {
            int mytop = top.get();
            if (mytop == MAX_SIZE) {
                throw new Exception("Array is full");
            }
            for(int i = 0; i < mytop; i++){
                if(item == data[i]){
                    return;
                }
            }
            if(top.compareAndSet(mytop, mytop+1)) {
                data[mytop] = item;
                return;
            }
        }
    }
    public void print(){
        for(int i = 0; i < top.get(); i++){
            System.out.println(data[i]);
        }
    }

    public int remove(){
        while(true) {
            int mytop = top.get();
            if(mytop == 0){
                return -1;
            }
            if (top.compareAndSet(mytop, mytop - 1)) {
                return data[mytop];
            }
        }
    }
}
