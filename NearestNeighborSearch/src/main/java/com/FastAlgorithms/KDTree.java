package com.FastAlgorithms;

import org.nd4j.linalg.api.ndarray.INDArray;

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

    

}
