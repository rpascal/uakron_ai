package com.uakron.ai;

import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.bayes.NaiveBayes;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.Console;
import java.io.File;
import java.util.*;


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

                        float yes = 0;
                        float no = 0;

                        List<String> predictionValues = new ArrayList<>( Arrays.asList( buildClassifier.toString().split("\n")));

                        HashMap<String, String> map = getNewAttributes();

                        Iterator it = map.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            String value = pair.getValue().toString();

                            Optional<String> v = predictionValues.stream().filter(x -> x.contains(value)).findFirst();

                            if(v.isPresent()){
                                String[] s = v.get().replaceAll("(^\\s+|\\s+$)", "").split("\\s+");
                                float newYes = Float.parseFloat(s[1]);
                                float newNo = Float.parseFloat(s[2]);
                                yes += newYes;
                                no += newNo;
                            }
                            it.remove();
                        }

                        if(yes > no){
                            printGroup("Prediction is YES");
                        }else if(no > yes){
                            printGroup("Prediction is NO");
                        }else{
                            printGroup("Prediction is UNDETERMINED");
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

    private HashMap<String, String> getNewAttributes() {

        HashMap<String, String> map = new HashMap<>();

        Instances newCases = new Instances(dataSet);
        newCases.delete();


        Instance newInstance = new DenseInstance(newCases.numAttributes());
        printGroup("Enter values...");

        for (int i = 0; i < newCases.numAttributes() - 1; ++i) {
            Attribute attribute = newCases.attribute(i);
            boolean set = false;
            while (!set) {
                try {
                    String value = readFromConsole(attribute.name() + ": ");
                    newInstance.setValue(attribute, value);
                    map.put(attribute.name(), value);
                    set = true;
                } catch (IllegalArgumentException e) {
                    printGroup(e.toString(), "Error, Try entering a different value");
                }
            }
        }
        return map;
    }




    private String readFromConsole(String s) {
        return console.readLine(s);
    }

    private void exit() {
        System.exit(0);
    }

}
