package com.uakron.ai;

import weka.core.converters.ConverterUtils.DataSource;
import weka.core.*;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;


public class Main {

    private Console console;
    private J48 j48Model;
    private Instances dataSet;


    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        console = System.console();

        gettingDataActions();
        dataLoadedActions();
    }

    public void gettingDataActions() {
        while (true) {
            printGroup(" 1. Learn decision tree and learn ( .arff or .csv file )", " 2. Load an existing model file (tree)", " 3. Exit");
            String selected = console.readLine("Please Select Option: ");

            j48Model = new J48();

            try {
                switch (Integer.parseInt(selected)) {
                    // Handle loading the arff file
                    case 1: {

                        File file = selectFile(new FileNameExtensionFilter(
                                "arff & CSV", "arff", "csv"));
                        if (file == null) {
                            printGroup("Please select a valid file.");
                            break;
                        }

                        String filename = file.getAbsolutePath();

                        DataSource source = new DataSource(filename);
                        dataSet = source.getDataSet();

                        dataSet.setClassIndex(dataSet.numAttributes() - 1);
                        j48Model.buildClassifier(dataSet);


                        saveTree();

                        break;
                    }
                    case 2: {

                        File file = selectFile(null);
                        if (file == null) {
                            printGroup("Please select a valid file.");
                            break;
                        }

                        String filename = file.getAbsolutePath();

                        j48Model = (J48) SerializationHelper.read(filename);
                        break;
                    }
                    case 3: {
                        exit();
                    }
                    default: {
                        printGroup("Please enter 1, 2, or 3...");
                        continue;
                    }
                }
                printGroup(j48Model.toString());

                return;
            } catch (Exception e) {
                printGroup(e.toString());
                printGroup("Error loading data");
            }
        }
    }


    public void dataLoadedActions() {
        while (true) {
            printGroup(
                    "Selcet a option",
                    " 1. Select a new data file to learn from or load a existing tree",
                    " 2. Save loaded tree as .model file",
                    " 3. Apply the decision tree to new cases",
                    " 4. Exit");


            String selected = console.readLine("Select Option: ");

            switch (Integer.parseInt(selected)) {
                case 1: {
                    gettingDataActions();
                    break;
                }
                case 2: {
                    saveTree();
                    break;
                }
                // Apply the decision tree against read in cases
                case 3: {
                    if (dataSet == null) {
                        printGroup("Training data must be loaded from a .arff file to determine attributes");

                    }

                    // Clone the set of instances
                    Instances newCases = new Instances(dataSet);
                    newCases.delete();

                    while (true) {
                        Instance newInsatnce = new DenseInstance(newCases.numAttributes());

                        printGroup("Enter values for the attributes...");

                        for (int i = 0; i < newCases.numAttributes(); ++i) {
                            Attribute attribute = newCases.attribute(i);
                            String value = console.readLine(attribute.name() + ": ");

                            newInsatnce.setValue(attribute, value);
                        }

                        newCases.add(newInsatnce);


                        if (!console.readLine("Add another case? [y/n] ").toLowerCase().equals("y"))
                            break;
                    }

                    try {
                        // Do evaluate model against the learnt model
                        Evaluation eval = new Evaluation(newCases);

                        if (newCases.numInstances() >= 10 && console.readLine("Cross Validate? [y/n]: ").toLowerCase().equals("y")) {
                            eval.crossValidateModel(j48Model, newCases, 10, new Random(1));
                        } else {
                            eval.evaluateModel(j48Model, newCases);
                        }

                        // Print confusion matrix
                        printGroup(eval.toSummaryString("\nResults\n======\n", false));
                        printGroup(eval.toMatrixString());
                    } catch (Exception e) {

                        printGroup(e.toString());
                        printGroup("Unable to cross validate and create confusion matrix against entered cases");

                    }

                    break;
                }
                case 4: {
                    exit();
                }
                default: {
                    printGroup("Please enter 1, 2, 3, or 4...");
                    continue;
                }
            }
        }
    }

    private void exit() {
        System.exit(0);
    }

    private void saveTree() {
        try {
            String savePath = console.readLine("Filename to save (.model will be appended): ");

            String directoryName = "savedModels/";

            File directory = new File(directoryName);
            if (! directory.exists()){
                directory.mkdir();
            }

            SerializationHelper.write(directoryName + savePath + ".model", j48Model);

            printGroup("Model saved as " + savePath + ".model");

        } catch (Exception e) {

            printGroup(e.toString());
            printGroup("Unable to save tree model");

        }
    }

    private void printGroup(String... vals) {
        for (String val : vals) {
            System.out.println(val);
        }
        System.out.println();
    }


    private File selectFile(FileNameExtensionFilter filter) {
        JFileChooser chooser = new JFileChooser();
        if (filter != null) {
            chooser.setFileFilter(filter);
        }
        try{
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
        }catch(Exception e){
            printGroup("Could not find working directory.");
        }
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }


}