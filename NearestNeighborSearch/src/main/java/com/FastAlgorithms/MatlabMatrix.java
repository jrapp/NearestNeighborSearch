package com.FastAlgorithms;

/**
 * Created by Jeremy on 3/30/2016.
 */
public class MatlabMatrix {
    public String _ArrayType_;
    public int[] _ArraySize_;
    public double[] _ArrayData_;

    public MatlabMatrix(int[] size, String type, double[] data){
        _ArrayType_ = type;
        _ArrayData_ = data;
        _ArraySize_ = size;
    }
}
