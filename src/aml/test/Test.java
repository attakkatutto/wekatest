/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aml.test;

import aml.weka.MyWekaManager;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Davide
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //oldTest();
        newTest();
               
    }
    
    
    public static void newTest(){     
        double paramValue = 0.5;
        String paramName = "P1";
        File[] folder = new File("." + File.separator + "dbfiles" + File.separator).listFiles();
        Arrays.sort(folder, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }            
        });
        long start = System.currentTimeMillis();
        for (File f : folder){
            if (f.getName().contains("SYNTHETIC")) {
            MyWekaManager m = new MyWekaManager();
            m.calculateResults(f,paramName, paramValue);
            paramValue = paramValue + 0.05;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(" - time elapsed (msec): " + (end - start));
    }
    
    
//    public static void oldTest(){     
//        double paramValue = 0.5;
//        String paramName = "P1";
//        File[] folder = new File("." + File.separator + "dbfiles" + File.separator).listFiles();
//        long start = System.currentTimeMillis();
//        for (File f : folder){
//            if (f.getName().contains("SYNTHETIC")) {
//            MyWekaManager m = new MyWekaManager(1);
//            m.calculateResultsOld(f,paramName, paramValue);
//            paramValue = paramValue + 0.05;
//            }
//        }
//        long end = System.currentTimeMillis();
//        System.out.println(" - time elapsed (msec): " + (end - start));
//    }
}
