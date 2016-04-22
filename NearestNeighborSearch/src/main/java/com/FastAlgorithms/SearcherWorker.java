package com.FastAlgorithms;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by Jeremy on 4/8/2016.
 */
public class SearcherWorker implements Runnable {

    public INDArray input;
    public INDArray output;
    private double epsilon;
    private int colIndex;
    private int numSamples;
    private double bound;
    private double multiplier;

    public SearcherWorker(INDArray myInput, INDArray myoutput, int myColumn, double myEpsilon){
        colIndex = myColumn;
        epsilon = myEpsilon;
        input = myInput;
        numSamples = input.shape()[0];
        output = myoutput;
        //For computation efficiency, I have an upper bound that the norm can be otherwise the loop skips
        bound = Math.pow(epsilon,2)*-1*Math.log(Math.pow(10,-10));
        multiplier = -1 / Math.pow(epsilon,2);
    }

    public void run(){
        double exponent = 0.0;
        INDArray norm;
        INDArray temp;
        for(int i = 0; i < numSamples; i++){
            norm = input.getRow(i);
            norm = norm.sub(input.getRow(colIndex));
            temp = norm.norm2(1);
            if(temp.getDouble(0) > bound)continue;
            temp.muli(multiplier);
            exponent = Math.exp(temp.getDouble(0));
            temp.putScalar(0, exponent);
            output.put(i, colIndex, temp.getScalar(0));
        }
    }
}
