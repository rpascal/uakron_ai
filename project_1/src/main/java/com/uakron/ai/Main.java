package com.uakron.ai;

import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.Console;
import java.io.File;
import java.util.Random;


public class Main {

    private Console console;
    private J48 j48Model;
    private Instances dataSet;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        console = System.console();
        j48Model = new J48();
        dataLoadedActions();
    }

    public void dataLoadedActions() {
        while (true) {

            boolean dataLoaded = dataSet != null;
            String disabledString = (dataLoaded ? "" : " ( Disabled until data is loaded )");
            printGroup(
                    "Selcet a option",
                    " 1. Learn decision tree and save ( .arff or .csv file )",
                    " 2. Load an existing model file",
                    " 3. Save loaded tree as .model file" + disabledString,
                    " 4. Apply the decision tree to new cases" + disabledString,
                    " 5. Exit");

            String selected = readFromConsole("Select Option: ");

            try {
                switch (Integer.parseInt(selected)) {
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

                        saveDataset();

                        printGroup("Data Loaded and Saved");
                        break;
                    }
                    case 2: {
                        File file = selectFile(null);
                        if (file == null) {
                            printGroup("Please select a valid file.");
                            break;
                        }

                        String filename = file.getAbsolutePath();

                        dataSet = (Instances) SerializationHelper.read(filename);
                        dataSet.setClassIndex(dataSet.numAttributes() - 1);
                        j48Model.buildClassifier(dataSet);

                        printGroup("Data Loaded");
                        break;
                    }
                    case 3: {
                        if(!dataLoaded){
                            break;
                        }
                        saveDataset();
                        break;
                    }
                    case 4: {
                        if(!dataLoaded){
                            break;
                        }
                        Instances newCases = getNewAttributes();

                        try {
                            Evaluation eval = new Evaluation(newCases);

                            if (newCases.numInstances() >= 10 && readFromConsole("Cross Validate? [y/n]: ").toLowerCase().equals("y")) {
                                eval.crossValidateModel(j48Model, newCases, 10, new Random(1));
                            } else {
                                eval.evaluateModel(j48Model, newCases);
                            }

                            printGroup(eval.toSummaryString("Results:\n", true));
                            printGroup(eval.toMatrixString());
                        } catch (Exception e) {
                            printGroup(e.toString(), "Unable to apply the decision tree to entered cases");
                        }
                        break;
                    }
                    case 5: {
                        exit();
                    }
                    default: {
                        printGroup("Please enter 1, 2, 3, or 4...");
                        continue;
                    }
                }
            } catch (Exception e) {
                printGroup(e.toString(), "Error loading data");
            }
        }
    }

    private void saveDataset() {
        try {
            String savePath = readFromConsole("Filename to save (.model will be appended): ");
            String directoryName = "savedModels/";

            File directory = new File(directoryName);
            if (!directory.exists()) {
                directory.mkdir();
            }

            SerializationHelper.write(directoryName + savePath + ".model", dataSet);

            printGroup("Model saved as " + savePath + ".model");
        } catch (Exception e) {
            printGroup(e.toString(), "Unable to save tree model");
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
        try {
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
        } catch (Exception e) {
            printGroup("Could not find working directory.");
        }
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    private Instances getNewAttributes() {
        Instances newCases = new Instances(dataSet);
        newCases.delete();

        while (true) {
            Instance newInstance = new DenseInstance(newCases.numAttributes());
            printGroup("Enter values for the attributes...");

            for (int i = 0; i < newCases.numAttributes(); ++i) {
                Attribute attribute = newCases.attribute(i);
                boolean set = false;
                while (!set) {
                    try {
                        String value = readFromConsole(attribute.name() + ": ");
                        newInstance.setValue(attribute, value);
                        set = true;
                    } catch (IllegalArgumentException e) {
                        printGroup(e.toString(), "Error, Try entering a different value");
                    }
                }
            }

            newCases.add(newInstance);

            if (readFromConsole("Add another case? [y/n] ").toLowerCase().equals("n")) {
                break;
            }
        }

        return newCases;
    }

    private String readFromConsole(String s) {
        return console.readLine(s);
    }

    private void exit() {
        System.exit(0);
    }

}