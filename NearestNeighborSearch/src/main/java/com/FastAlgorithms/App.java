package com.FastAlgorithms;

import com.google.gson.Gson;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.DoubleBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.FileWriter;
import java.lang.Math;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class App 
{
    public static void main( String[] args )
    {
        try{

            long t;
            INDArray array;
            INDArray output;
            INDArray output_multi;
            double[] epsilons = {.1,.5,1,5,10,100,200,1000};
            String fileLocation = "C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\";
            String fileName = "200_3dcurve";
            String fileExtension = ".json";
            //String fileName = "n_100.json";

            array = get2DArray(fileLocation+fileName+fileExtension);
            //System.out.println(array);

            KDTree kd = new KDTree(array, 3);
            kd.makeKDTree();
            kd.RandomGaussian(array.shape());

            for(int i = 0; i < epsilons.length; i++){
                System.out.println(epsilons[i]);
                t = System.nanoTime();
                output = singleThreadedNearestNeighbor(array, epsilons[i]);
                t = System.nanoTime() - t;
                System.out.println("\tSerial took: " + t/1000000 + "." + t%1000000 + " milliseconds");
                t = System.nanoTime();
                output_multi = multiThreadedNearestNeighbor(array, epsilons[i]);
                t = System.nanoTime() - t;
                System.out.println("\tParallel took: " + t/1000000 + "." + t%1000000 + " milliseconds");
                testEquality(output,output_multi, 0.0000000001);
                writeToMatlabJson(output_multi,"C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\"+fileName+"_epsilon_"+Double.toString(epsilons[i])+".json");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static INDArray getNDArray(String fileLocation){
        Gson gson = new Gson();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
            MatlabMatrix mat = gson.fromJson(reader, MatlabMatrix.class);
            INDArray array = Nd4j.create(mat._ArrayData_, mat._ArraySize_);
            return array;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static INDArray get2DArray(String fileLocation){
        Gson gson = new Gson();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
            double[][] data = gson.fromJson(reader, double[][].class);
            INDArray array = Nd4j.create(data);
            return array;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static INDArray getArray(String fileLocation){
        Gson gson = new Gson();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
            double[] data = gson.fromJson(reader, double[].class);
            INDArray array = Nd4j.create(data);
            return array;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void writeToMatlabJson(INDArray out, String location){
        Gson gson = new Gson();
        try{
            MatlabMatrix mat = new MatlabMatrix(out.shape(), "double", out.data().asDouble());
            String json = gson.toJson(mat);
            FileWriter fileWriter = new FileWriter(location);
            fileWriter.write(json);
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static INDArray singleThreadedNearestNeighbor(INDArray input, double epsilon){
        //System.out.println("Serial nearest neighbor...");
        long t = System.nanoTime();
        int numberPoints = input.shape()[0];
        int[] outshape = {numberPoints,numberPoints};
        INDArray output = Nd4j.create(outshape);

        final double multiplier = -1 / Math.pow(epsilon,2);
        double exponent = 0.0;
        INDArray norm;
        INDArray temp;
        INDArray column = Nd4j.create(numberPoints, 1);
        t = System.nanoTime() - t;
        //System.out.println("\t\tSerial speedup took: " + t/1000000 + "." + t%1000000 + " milliseconds");
        for(int i = 0; i < numberPoints; i++) {
            for (int j = 0; j < numberPoints; j++) {
                norm = input.getRow(i);
                norm = norm.sub(input.getRow(j));
                temp = norm.norm2(1);
                temp.muli(multiplier);
                exponent = Math.exp(temp.getDouble(0));
                temp.putScalar(0, exponent);
                column.put(j, temp.getScalar(0));
            }
            output.putColumn(i, column);
        }
        return output;
    }



    public static INDArray multiThreadedNearestNeighbor(INDArray input, double epsilon){
        //This method should run the nearest neighbor search with multiple threads
        //System.out.println("Multithreaded Nearest Neighbor...");
        long t = System.nanoTime();
        int numPoints = input.shape()[0];
        int[] outShape = {numPoints, numPoints};
        INDArray output = Nd4j.create(outShape);
        ExecutorService exec = Executors.newFixedThreadPool(5);
        Future<?> future[] = new Future[numPoints];
        for(int i = 0; i < numPoints; i++){
            future[i] = exec.submit(new SearcherWorker(input,output,i,epsilon));
        }
        try{
            for(int i = 0; i < numPoints; i++) {
                (future[i]).get();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        t = System.nanoTime() - t;
        //System.out.println("\t\tParallel speedup took: " + t/1000000 + "." + t%1000000 + " milliseconds");
        exec.shutdown();
        while(!exec.isTerminated()){};
        return output;
    }
    public INDArray kdTreeNearestNeighbors(INDArray input, double epsilon){
        int numPoints = input.shape()[0];
        int[] outShape = {numPoints, numPoints};
        INDArray output = Nd4j.create(outShape);

        return output;
    }

    public static boolean testEquality(INDArray matrix1, INDArray matrix2, double tolerance){
        int[] dim1 = matrix1.shape();
        int[] dim2 = matrix2.shape();
        for(int i = 0; i < dim1.length; i++){
            if(dim1[i] != dim2[i]) {
                System.out.println("Matrix dimensions do not match");
                return false;
            }
        }
        if(dim1.length != dim2.length){
            System.out.println("Matrix dimensions do not match");
            return false;
        }
        if(dim1.length != 2){
            System.out.println("Higher dimensional equality tester not implemented");
            return false;
        }
        boolean rv = true;
        for(int i = 0; i < dim1[0]; i++){
            for(int j = 0; j < dim1[1]; j++){
                if(matrix1.getDouble(i,j) - matrix2.getDouble(i,j) > tolerance){
                    System.out.println("Element at " + i + "," + j + " mismatch " + matrix1.getDouble(i,j) + " != " + matrix2.getDouble(i,j));
                    rv = false;
                }
            }
        }
        return rv;
    }



}
