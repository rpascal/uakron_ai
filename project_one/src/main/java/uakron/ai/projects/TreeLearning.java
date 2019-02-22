package uakron.ai.projects;

import weka.core.converters.ConverterUtils.DataSource;
import weka.core.*;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import java.io.*;
import java.util.*;

public class TreeLearning
{
    public static void main(String[] args)
    {
        new TreeLearning();
    }

    J48 learntModel;
    Instances train;

    public TreeLearning()
    {
        loadOrLearnTree();

        while (true)
        {
            executePrimaryActions();
        }
    }

    public void loadOrLearnTree()
    {
        while (true)
        {
            System.out.println("Would you like to learn from a .arff file or load an existing model?");
            System.out.println(" 1. Learn from a .arff file");
            System.out.println(" 2. Load an existing model file (tree)");
            System.out.println(" 3. Exit");
            System.out.println("");

            Console console = System.console();
            String selected = console.readLine("Select Option: ");

            String filename;
            learntModel = new J48();

            try
            {
                switch (Integer.parseInt(selected))
                {
                    // Handle loading the arff file
                    case 1:
                        filename = console.readLine("Enter .arff file path: ");

                        // Load the data source and learn the model
                        DataSource source = new DataSource(filename);
                        train = source.getDataSet();

                        // Learn the tree
                        train.setClassIndex(train.numAttributes() - 1);
                        learntModel.buildClassifier(train);
                        break;

                    // Handle loading the existing model
                    case 2:
                        // Deserialize the object
                        filename  = console.readLine("Enter model file path: ");
                        learntModel = (J48) SerializationHelper.read(filename);
                        break;

                    case 3: System.exit(0);

                        // Invalid option
                    default:
                        System.out.print("Invalid option, try again\n\n");
                        continue;
                }

                // Print out the loaded tree
                System.out.println("");
                System.out.println(learntModel.toString());

                return;
            }
            catch (Exception e)
            {
                System.out.println("");
                System.out.println(e.toString());
                System.out.println("File could not be loaded, please try again");
                System.out.println("");
            }
        }
    }

    /**
     * This is a SUPER METHOD!
     */
    public void executePrimaryActions()
    {
        while (true)
        {
            System.out.println("Selcet a option");
            System.out.println(" 1. Load a new .arff file to learn from, or load a existing tree");
            System.out.println(" 2. Save loaded tree as .model file");
            System.out.println(" 3. Test the accuracy of the decision tree");
            System.out.println(" 4. Apply the decision tree to new cases");
            System.out.println(" 5. Exit");
            System.out.println("");

            Console console = System.console();
            String selected = console.readLine("Select Option: ");

            switch (Integer.parseInt(selected))
            {
                // Reload the tree
                case 1:
                    loadOrLearnTree();
                    break;

                // Serialize and save the loaded tree
                case 2:
                    try
                    {
                        String savePath = console.readLine("Filename to save (excluding .model): ");
                        SerializationHelper.write(savePath + ".model", learntModel);

                        System.out.println("Model saved as " + savePath + ".model");
                        System.out.println("");
                    }
                    catch (Exception e)
                    {
                        System.out.println("");
                        System.out.println(e.toString());
                        System.out.println("Unable to save tree model");
                        System.out.println("");
                    }
                    break;

                // Test the accuracy of the decision tree
                case 3:
                    String dataFile = console.readLine("Data file to test against: ");

                    try
                    {
                        // Load in the new data
                        DataSource source = new DataSource(dataFile);
                        Instances testData = source.getDataSet();
                        testData.setClassIndex(testData.numAttributes() - 1);

                        // Cross validate learnt model against the test data
                        Evaluation eval = new Evaluation(testData);

                        if (console.readLine("Cross Validate? [y/n]: ").toLowerCase().equals("y"))
                        {
                            eval.crossValidateModel(learntModel, testData, 10, new Random(1));
                        }
                        else
                        {
                            eval.evaluateModel(learntModel, testData);
                        }

                        // Print confusion matrix
                        System.out.println(eval.toSummaryString("\nResults\n======\n", false));
                        System.out.println(eval.toMatrixString());
                    }
                    catch (Exception e)
                    {
                        System.out.println("");
                        System.out.println(e.toString());
                        System.out.println("Unable to cross validate new data and generate confusion matrix");
                        System.out.println("");
                    }
                    break;

                // Apply the decision tree against read in cases
                case 4:
                    if (train == null)
                    {
                        System.out.println("Training data must be loaded from a .arff file to determine attributes");
                        System.out.println("");
                    }

                    // Clone the set of instances
                    Instances newCases = new Instances(train);
                    newCases.delete();

                    while (true)
                    {
                        Instance newInsatnce = new DenseInstance(newCases.numAttributes());
                        System.out.println("");
                        System.out.println("Enter values for the attributes...");

                        for (int i = 0; i < newCases.numAttributes(); ++i)
                        {
                            Attribute attribute = newCases.attribute(i);
                            String value = console.readLine(attribute.name() + ": ");

                            newInsatnce.setValue(attribute, value);
                        }

                        newCases.add(newInsatnce);

                        System.out.println("");

                        if ( ! console.readLine("Add another case? [y/n] ").toLowerCase().equals("y"))
                            break;
                    }

                    try
                    {
                        // Do evaluate model against the learnt model
                        Evaluation eval = new Evaluation(newCases);

                        if (newCases.numInstances() >= 10 && console.readLine("Cross Validate? [y/n]: ").toLowerCase().equals("y"))
                        {
                            eval.crossValidateModel(learntModel, newCases, 10, new Random(1));
                        }
                        else
                        {
                            eval.evaluateModel(learntModel, newCases);
                        }

                        // Print confusion matrix
                        System.out.println(eval.toSummaryString("\nResults\n======\n", false));
                        System.out.println(eval.toMatrixString());
                    }
                    catch (Exception e)
                    {
                        System.out.println("");
                        System.out.println(e.toString());
                        System.out.println("Unable to cross validate and create confusion matrix against entered cases");
                        System.out.println("");
                    }

                    break;

                case 5: System.exit(0);

                    // Invalid option
                default:
                    System.out.print("Invalid option, try again\n\n");
                    continue;
            }
        }
    }

}