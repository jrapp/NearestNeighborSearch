package com.FastAlgorithms;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.DefaultRandom;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.cpu.NDArray;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Jeremy on 4/22/2016.
 */
public class KDTree {
    private INDArray input;
    private int k = 1;   //Size of the number of elements in the leaves at the end
    private int data_dim;
    Random rand = new DefaultRandom();
    public KDTree(INDArray myinput, int myk){
        input = myinput;
        k = myk;
        data_dim = input.shape()[1] - 1; //get the dimension of the input data (indexing starts at zero
    }
    public KDTree(INDArray myInput){
        input = myInput;
        data_dim = input.shape()[1] - 1; //get the dimension of the input data
    }

    public void makeKDTree(){
        int numSamples = input.shape()[0];
        quicksort(0, numSamples - 1, 1);
    }


    private void quicksort(int start, int end, int dim){
        if(end - start <= k){
            return;
        }
        int i = start;
        int j = end;
        double pivot = input.getDouble(start + (end - start) / 2, dim); // middle element of the section given
        while(i <= j){
            while(input.getDouble(i, dim) < pivot){
                i++;
            }
            while(input.getDouble(j, dim) > pivot){
                j--;
            }
            if(i <= j){
                double temp = input.getDouble(i, dim);
                int[] ind = {i,dim};
                input.putScalar(ind, input.getDouble(j, dim));
                ind[0] = j;
                input.putScalar(ind, temp);
                i++;
                j--;
            }
        }
        if(start < j){
            quicksort(start, j, (dim+1) % data_dim);
        }
        if(i < end){
            quicksort(i, end, (dim +1) % data_dim);
        }
    }

    public INDArray randomProjection(INDArray original){
        return null;
    }

    public INDArray RandomGaussian(final int[] dims){
        final INDArray Gaussian = new NDArray(dims);
        int numPoints = dims[0]*dims[1];
        ExecutorService exec = Executors.newFixedThreadPool(5);
        Future<?> future[] = new Future[dims[1]];
        class GaussianCreater implements Runnable{
            private int col;
            public GaussianCreater(int mycol){
                col = mycol;
            }
            public void run(){
                for(int i = 0; i < dims[0];i++){
                    int[] mydims = {i, col};
                    Gaussian.putScalar(mydims, rand.nextGaussian());
                }
            }
        }
        for(int i = 0; i < dims[1];i++){
            future[i] = exec.submit(new GaussianCreater(i));
        }
        try{
            for(int i = 0; i < dims[1]; i++) {
                (future[i]).get();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(Gaussian);
        return Gaussian;
    }
}
