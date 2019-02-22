//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//
//import weka.classifiers.trees.J48;
//import weka.core.DenseInstance;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils;
//import weka.core.converters.ConverterUtils.DataSource;
//
///**
// * Weka decision trees demo.
// */
//public class DecisionTree {
//    private Instances trainingData;
//
//    public static void main(String[] args) throws Exception {
//
////        DecisionTree decisionTree = new DecisionTree("input/films.arff");
//        DecisionTree decisionTree = new DecisionTree("input/breast-cancer.arff");
//
//        J48 id3tree = decisionTree.trainTheTree();
//
//        // Print the resulted tree
//        System.out.println(id3tree.toString());
//
//        // Test the tree
////        Instance testInstance = decisionTree.prepareTestInstance();
////        int result = (int) id3tree.classifyInstance(testInstance);
////
////        String readableResult = decisionTree.trainingData.attribute(3).value(result);
////        System.out.println(" ----------------------------------------- ");
////        System.out.println("Test data               : " + testInstance);
////        System.out.println("Test data classification: " + readableResult);
//    }
//
//    public DecisionTree(String fileName) {
//        try {
//
//            DataSource source = new DataSource(fileName);
//            trainingData = source.getDataSet();
//            System.out.println(trainingData.toSummaryString());
//
//
//            // Setting class attribute
//            trainingData.setClassIndex(trainingData.numAttributes() - 1);
////            trainingData.setClassIndex(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//    }
//
//    private J48 trainTheTree() {
//        J48 id3tree = new J48();
//
//        String[] options = new String[1];
//        // Use unpruned tree.
//        options[0] = "-U";
//
//        try {
//            id3tree.setOptions(options);
//            id3tree.buildClassifier(trainingData);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return id3tree;
//    }
//
//    private Instance prepareTestInstance() {
//        Instance instance = new DenseInstance(3);
//        instance.setDataset(trainingData);
//
//        instance.setValue(trainingData.attribute(0), "age");
//        instance.setValue(trainingData.attribute(1), "menopause");
//        instance.setValue(trainingData.attribute(2), "tumor-size");
//
//        return instance;
//    }
//}