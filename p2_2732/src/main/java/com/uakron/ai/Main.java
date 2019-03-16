package com.uakron.ai;

import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.Console;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    private Console console;
    private NaiveBayes buildClassifier;
    private Instances dataSet;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        console = System.console();
        buildClassifier = new NaiveBayes();
        loop();
    }

    public void loop() {
        while (true) {

            boolean dataLoaded = dataSet != null;
            String disabledString = (dataLoaded ? "" : " ( Disabled until data is loaded )");

            printGroup(
                    "Select an option",
                    " 1. Learn decision tree and save ( .arff or .csv file )",
                    " 2. Load an existing model file",
                    " 3. Save loaded tree as .model file" + disabledString,
                    " 4. Apply the decision tree to new cases" + disabledString,
                    " 5. Exit");

            String selected = readFromConsole("Select an Option: ");

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
                        buildClassifier.buildClassifier(dataSet);

                        printGroup(buildClassifier.toString(), buildClassifier.displayModelInOldFormatTipText(),
                                buildClassifier.globalInfo(), Integer.toString(buildClassifier.getClassEstimator().getOptions().length));

                        for(String s : buildClassifier.getClassEstimator().getOptions() ){
                            printGroup(s);
                        }

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
                        buildClassifier.buildClassifier(dataSet);

                        printGroup("Data Loaded");
                        break;
                    }
                    case 3: {
                        if (!dataLoaded) {
                            break;
                        }
                        saveDataset();
                        break;
                    }
                    case 4: {
                        if (!dataLoaded) {
                            break;
                        }
//                        traverseDecisionTree(buildClassifier.graph().split("\n"));

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

    private void traverseDecisionTree(String nodes[]) {
        for (int j = 1; j < nodes.length - 1; j++) {
            if (nodes[j].indexOf("shape=box") != -1) {
                printGroup("The decision Attribute is : " + nodes[j].substring(nodes[j].indexOf("=") + 2, nodes[j].indexOf(" (")));
                break;
            }
            String mes = "Enter the value for " + nodes[j].substring(nodes[j].indexOf("=") + 2, nodes[j].indexOf("]") - 2) + ": ";
            String condition_value = readFromConsole(mes);
            j++;
            while ((nodes[j].indexOf("= " + condition_value) == -1) && j < nodes.length - 1) {
                j++;
            }
            String childNodeValue = nodes[j].substring(0, nodes[j].indexOf(" "));
            childNodeValue = childNodeValue.substring(childNodeValue.indexOf("->") + 2);
            while ((!childNodeValue.equals(nodes[j + 1].substring(0, nodes[j + 1].indexOf(" ")))) && j + 1 < nodes.length - 1) {
                j++;
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


    private String readFromConsole(String s) {
        return console.readLine(s);
    }

    private void exit() {
        System.exit(0);
    }

}
