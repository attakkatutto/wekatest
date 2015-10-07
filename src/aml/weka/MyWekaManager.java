/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aml.weka;

import aml.entity.Result;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * Class for manage WEKA with java code
 *
 * @author ddefalco
 */
public final class MyWekaManager {

    private Instances instances;
    private int classIndex;
    private int runs;
    private int folds;
    private BufferedWriter bwr;
    private final String ROW_RESULT_FILE = " %s, %s, %s, %s, %s, %s \n";

    /**
     * Constructor with file that contains dataset
     *
     * @param dataset File with instances to analyze
     */
    public MyWekaManager(File dataset) {
        try {
            this.runs = 5;
            this.folds = 10;
            this.classIndex = 0;
            loadInstances(dataset);
            createFile();
        } catch (Exception ex) {
            Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Constructor with file that contains dataset
     *
     * @param dataset File with instances to analyze
     */
    public MyWekaManager(File dataset,int old) {
        try {
            this.runs = 5;
            this.folds = 10;
            this.classIndex = 0;
            loadInstances(dataset);
            createFileOld();
        } catch (Exception ex) {
            Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Load the instances of dataset
     *
     * @param dataset File contains data
     * @throws Exception
     */
    public void loadInstances(File dataset) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(dataset);
        /**
         * Load dataset in instances variable
         */
        instances = loader.getDataSet();
        /**
         * Delete the first attribute of dataset it's the transaction id
         * attribute
         */
        instances.deleteAttributeAt(0);
        instances.setClassIndex(instances.numAttributes() - 1);
        classIndex = getClassNOIndex();
    }

    /**
     * WEKA cross validation classifier method that calculate the f-Measure of
     * the model
     *
     * @param classifier Abstract classifier that build model
     * @param options for the cassifier
     * @return double fMeasure value
     */
    public MyWekaResult crossValidation(AbstractClassifier classifier, String[] options, String name) {
        double _fMeasure = 0;
        if (options != null) {
            try {
                classifier.setOptions(options);
            } catch (Exception ex) {
                Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (int run = 0; run < runs; run++) {
            instances.stratify(folds);
            for (int fold = 0; fold < folds; fold++) {
                try {
                    System.out.println("-" + name + "- run: " + run + " fold: " + fold);
                    Instances _train = instances.trainCV(folds, fold);
                    Instances _test = instances.testCV(folds, fold);
                    classifier.buildClassifier(_train);
                    Evaluation _evaluation = new Evaluation(_train);
                    _evaluation.evaluateModel(classifier, _test);
                    _fMeasure += _evaluation.fMeasure(classIndex);
                } catch (Exception ex) {
                    Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return new MyWekaResult(name, _fMeasure / (runs * folds));
    }

    /**
     * Create the file to write te results from WEKA
     *
     * @throws IOException
     */
    private void createFile() throws IOException {
        /*Create the results file*/
        File filer = new File("." + File.separator + "dbfiles" + File.separator + "WEKA_RESULTS.CSV");
        FileWriter fwr = new FileWriter(filer.getAbsoluteFile(), true);
        bwr = new BufferedWriter(fwr);
    }
    
    /**
     * Create the file to write te results from WEKA
     *
     * @throws IOException
     */
    private void createFileOld() throws IOException {
        /*Create the results file*/
        File filer = new File("." + File.separator + "dbfiles" + File.separator + "WEKA_RESULTS_OLD.CSV");
        FileWriter fwr = new FileWriter(filer.getAbsoluteFile(), true);
        bwr = new BufferedWriter(fwr);
    }

     /**
     * Calculate the f-Measure results from building models with
     * J48 - SMO - IBK algorithms 
     * @param paramName
     * @param paramValue 
     */
    public void calculateResultsOld(String paramName, double paramValue) {
        try {
            System.out.println("- Start algorithm J48");
            double _dt = crossValidation(new J48(), null,"J48").getValue();
            System.out.println("- Start algorithm SMO");
            double _svm = crossValidation(new SMO(), null,"SMO").getValue(); 
            System.out.println("- Start algorithm KNN");
            double _knn = crossValidation(new IBk(), new String[]{"-K", "3"},"IBK").getValue();
            System.out.println("- Start algorithm Random Forest -");
            double _rf = crossValidation(new RandomForest(),new String[]{"-I", "100", "-K", "0", "-S", "1"},"RF").getValue();
            writeResultOld(new Result(paramName, paramValue, _dt, _svm, _knn, _rf));
        } catch (Exception ex) {
            Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Calculate the f-Measure results from building models with J48 - SMO - IBK
     * algorithms
     *
     * @param paramName
     * @param paramValue
     */
    public void calculateResults(String paramName, double paramValue) {
        try {
            int threadNum = 4;
            ExecutorService executor = Executors.newFixedThreadPool(threadNum);
            List<Callable<MyWekaResult>> taskList = new ArrayList<Callable<MyWekaResult>>();
            Result res = new Result();
            res.setParamName(paramName);
            res.setParamValue(paramValue);

            // Start thread for the first
            taskList.add(new Callable<MyWekaResult>() {
                @Override
                public MyWekaResult call() {
                    return crossValidation(new J48(), null, "J48");
                }
            });
            //executor.execute(futureTask_1);

            // Start thread for the second            
            taskList.add(new Callable<MyWekaResult>() {
                @Override
                public MyWekaResult call() {
                    return crossValidation(new SMO(), null, "SMO");
                }
            });
            //executor.execute(futureTask_2);

            // Start thread for the third            
            taskList.add(new Callable<MyWekaResult>() {
                @Override
                public MyWekaResult call() {
                    return crossValidation(new IBk(), new String[]{"-K", "3"}, "IBK");
                }
            });
            //executor.execute(futureTask_3);

            // Start thread for the fourth            
            taskList.add(new Callable<MyWekaResult>() {
                @Override
                public MyWekaResult call() {
                    return crossValidation(new RandomForest(), new String[]{"-I", "100", "-K", "0", "-S", "1"}, "RF");
                }
            });
            //executor.execute(futureTask_4);
            List<Future<MyWekaResult>> futures = executor.invokeAll(taskList);

            // Wait until all results are available and combine them at the same time
            for (Future<MyWekaResult> future : futures) {
                MyWekaResult m = future.get();
                switch (m.getName()) {
                    case "J48":
                        res.setDecisiontree(m.getValue());
                        break;
                    case "SMO":
                        res.setSvm(m.getValue());
                        break;
                    case "IBK":
                        res.setKnn(m.getValue());
                        break;
                    case "RF":
                        res.setRandomForest(m.getValue());
                        break;
                }
            }
            writeResult(res);
            executor.shutdown();
        } catch (Exception ex) {
            Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write result instance to file
     *
     * @param r
     */
    private void writeResult(Result r) {
        if (bwr != null) {
            try {
                bwr.append(String.format(ROW_RESULT_FILE, r.getParamName(), r.getParamValue(), r.getDecisiontree(), r.getSvm(), r.getKnn(), r.getRandomForest()));
                bwr.close();
            } catch (IOException ex) {
                Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                instances.clear();
            }
        }
    }

    /**
     * Write result instance to file
     *
     * @param r
     */
    private void writeResultOld(Result r) {
        if (bwr != null) {
            try {
                bwr.append(String.format(ROW_RESULT_FILE, r.getParamName(), r.getParamValue(), r.getDecisiontree(), r.getSvm(), r.getKnn(), r.getRandomForest()));
                bwr.close();
            } catch (IOException ex) {
                Logger.getLogger(MyWekaManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                instances.clear();
            }
        }
    }
    
    
    /**
     * Get index of fraud class attribute (HONEST = 'NO')
     *
     * @return index of Fraud Class
     */
    private int getClassNOIndex() {
        int _ret = 0;
        for (int i = 0; i < instances.numClasses(); i++) {
            if (instances.classAttribute().value(i).trim().equals("NO")) {
                _ret = i;
            }
        }
        return _ret;
    }
}
