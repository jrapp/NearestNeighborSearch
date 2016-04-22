package com.FastAlgorithms;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * Created by Jeremy on 4/22/2016.
 */
public class KDTree {
    private INDArray input;
    private int k = 1;
    public KDTree(INDArray myinput, int myk){
        input = myinput;
        k = myk;
    }
    public KDTree(INDArray myInput){
        input = myInput;
    }

    public NDArrayIndex[] makeKDTree(){
        NDArrayIndex[] candidate = new NDArrayIndex[input.shape()[0]]; //Array of indexes for each point in the input
        NDArrayIndex init = new NDArrayIndex();
        init.init(0,input.shape()[0]); //Create the initial partition of all of the indices
        NDArrayIndex[] partitions = createPartitions(init);
        return candidate;
    }

    private NDArrayIndex[] createPartitions(INDArrayIndex indices){
        
    }

}
