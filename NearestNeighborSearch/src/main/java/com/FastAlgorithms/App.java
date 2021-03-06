package com.FastAlgorithms;

import com.google.gson.Gson;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.DoubleBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.NDArray;
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
            INDArray output_kd;
            double[] epsilons = {.1,.5,1,5,10,100,200,1000};
            String fileLocation = "C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\";
            String fileName = "3dCurveNoise";
            String fileExtension = ".json";

            array = get2DArray(fileLocation+fileName+fileExtension);

            testDataSize(1);


//            for(int i = 0; i < epsilons.length; i++){
//                System.out.println(epsilons[i]);
//                t = System.nanoTime();
//                output = singleThreadedNearestNeighbor(array, epsilons[i]);
//                t = System.nanoTime() - t;
//                System.out.println("\tSerial took: " + t/1000000 + "." + t%1000000 + " milliseconds");
//                t = System.nanoTime();
//                output_multi = multiThreadedNearestNeighbor(array, epsilons[i]);
//                t = System.nanoTime() - t;
//                System.out.println("\tParallel took: " + t/1000000 + "." + t%1000000 + " milliseconds");
//                testEquality(output,output_multi, 0.0000000001);
//                t = System.nanoTime();
//                output_kd = JLNearestNeighbors(20, array, epsilons[i]);
//                t = System.nanoTime() - t;
//                System.out.println("\tKD Tree took: " + t/1000000 + "." + t%1000000 + " milliseconds");
//                testEquality(output,output_kd, 0.001);
//                writeToMatlabJson(output,"C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\"+fileName+"_epsilon_"+Double.toString(epsilons[i])+".json");
//                writeToMatlabJson(output_kd,"C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\"+fileName+"_epsilon_"+Double.toString(epsilons[i])+"_kd.json");
//            }
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


    public static INDArray JLNearestNeighbors(int numRuns, INDArray input, double epsilon){
        INDArray randomProjection = KDTree.randomProjection(input);
        KDTree kdTree = new KDTree(randomProjection, 10);
        kdTree.makeKDTree();
        for(int j = 1; j <= numRuns; j++){
            randomProjection = KDTree.randomProjection(input);
            kdTree.setNewInput(randomProjection);
            kdTree.makeKDTree();
        }
        int numdataPoints = input.shape()[0];
        int[] shape = {numdataPoints,numdataPoints};
        INDArray nearestNeighbors = new NDArray(shape);
        final double multiplier = -1 / Math.pow(epsilon,2);
        double exponent = 0.0;
        INDArray norm;
        INDArray temp;
        for(int i = 0; i < numdataPoints; i++){
            int nearNeighbor = kdTree.list.pop(i);
            while(nearNeighbor != -1){
                norm = input.getRow(i);
                norm = norm.sub(input.getRow(nearNeighbor));
                temp = norm.norm2(1);
                temp.muli(multiplier);
                exponent = Math.exp(temp.getDouble(0));
                temp.putScalar(0, exponent);
                int[] index = {i,nearNeighbor};
                nearestNeighbors.putScalar(index, exponent);
                nearNeighbor = kdTree.list.pop(i);
            }
        }
        return nearestNeighbors;
    }

    public static void testDataSize(double epsilon){
        final int numTrials = 10;
        final int numberDataSets = 30;
        String fileLocation = "C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\SampleSizeTesting\\";
        String fileName = "clusters";
        String fileExtension = ".json";

        INDArray array;
        INDArray output;
        int[] shape = {4,30};
        INDArray testData = new NDArray(shape);
        int currentDataSet = 100;
        long t;
        long average = 0;
        int[] index = {0,0};
        for(int i = 0; i < numberDataSets; i++){
            System.out.println("Data Size: " + currentDataSet);
            index[0] = 0;
            index[1] = i;
            testData.putScalar(index, currentDataSet);
            array = get2DArray(fileLocation+currentDataSet+fileName+fileExtension);
            for(int j = 0;j < numTrials;j++){
                t = System.nanoTime();
                output = singleThreadedNearestNeighbor(array, epsilon);
                t = System.nanoTime() - t;
                average += t;
            }
            average/=numTrials;
            System.out.println("\tSerial: " + average/1000000 + "." + average%1000000 + " milliseconds");
            index[0] = 1; //Second row is the average serial time
            testData.putScalar(index, average);
            for(int j = 0;j < numTrials;j++){
                t = System.nanoTime();
                output = multiThreadedNearestNeighbor(array, epsilon);
                t = System.nanoTime() - t;
                average += t;
            }
            average/=numTrials;
            System.out.println("\tMultiThreaded: " + average/1000000 + "." + average%1000000 + " milliseconds");
            index[0] = 2;
            testData.putScalar(index, average);
            for(int j = 0;j < numTrials;j++){
                t = System.nanoTime();
                output = JLNearestNeighbors(20,array,epsilon);
                t = System.nanoTime() - t;
                average += t;
            }
            average/=numTrials;
            System.out.println("\tKD Tree: " + average/1000000 + "." + average%1000000 + " milliseconds");
            index[0] = 3;
            testData.putScalar(index, average);
            currentDataSet+=100;
        }
        writeToMatlabJson(testData, fileLocation+"TestDataForDifferentSampleSizes.json");
    }

    public static void testDimensionSize(double epsilon){
        final int numTrials = 10;
        final int numberDataSets = 20;
        String fileLocation = "C:\\Users\\Jeremy\\Documents\\MATLAB\\Fast Algorithms\\Project\\DimensionTesting\\";
        String fileName = "d";
        String fileExtension = ".json";

        INDArray array;
        INDArray output;
        int[] shape = {4,0};
        INDArray testData = new NDArray(shape);

        long t;
        long average = 0;
        int[] index = {0,0};
        for(int i = 0; i < numberDataSets; i++){
            System.out.println("Data Size: " + i);
            index[0] = 0;
            index[1] = i;
            testData.putScalar(index, i);
            array = get2DArray(fileLocation+i+fileName+fileExtension);
            for(int j = 0;j < numTrials;j++){
                t = System.nanoTime();
                output = singleThreadedNearestNeighbor(array, epsilon);
                t = System.nanoTime() - t;
                average += t;
            }
            average/=numTrials;
            System.out.println("\tSerial: " + average/1000000 + "." + average%1000000 + " milliseconds");
            index[0] = 1; //Second row is the average serial time
            testData.putScalar(index, average);
            for(int j = 0;j < numTrials;j++){
                t = System.nanoTime();
                output = multiThreadedNearestNeighbor(array, epsilon);
                t = System.nanoTime() - t;
                average += t;
            }
            average/=numTrials;
            System.out.println("\tMultiThreaded: " + average/1000000 + "." + average%1000000 + " milliseconds");
            index[0] = 2;
            testData.putScalar(index, average);
            for(int j = 0;j < numTrials;j++){
                t = System.nanoTime();
                output = JLNearestNeighbors(20,array,epsilon);
                t = System.nanoTime() - t;
                average += t;
            }
            average/=numTrials;
            System.out.println("\tKD Tree: " + average/1000000 + "." + average%1000000 + " milliseconds");
            index[0] = 3;
            testData.putScalar(index, average);
        }
        writeToMatlabJson(testData, fileLocation+"TestDataForDifferentDimensions.json");
    }

}
