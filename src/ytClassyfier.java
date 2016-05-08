/**
 * Created by EssejSllim on 5/6/2016.
 */

import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.bayes.NaiveBayes;
import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class ytClassyfier {

    DataSource source;
    Instances data;
    NaiveBayes bayesModel;

    public ytClassyfier(String filename) throws Exception {
        source = new DataSource(filename);
        data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        bayesModel = constructBayes(data);
    }

    /**
     * Constructs a Naive Bayes classifier for the given instances
     * @param instance
     * @throws Exception
     */
    public Evaluation crossValidateBayes(Instances instance) throws Exception {
        NaiveBayes classyfy = new NaiveBayes();
        Evaluation eval = new Evaluation(instance);
        eval.crossValidateModel(classyfy, instance, 10, new Random(1));
        System.out.println(eval.toSummaryString("Results\n===============\n", false));
        return eval;
    }

    public NaiveBayes constructBayes(Instances instance) throws Exception {
        NaiveBayes bayes = new NaiveBayes();
        bayes.buildClassifier(instance);
        return bayes;
    }

    public static void clean(String readFileName) {
        //set the correct script name
        String scriptName = "C:\\Users\\essej\\Documents\\ytSent\\src\\resources\\cleanCommentsCSV.py";
        //initialize the Python interpreter
        PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
        org.python.util.PythonInterpreter interp = new org.python.util.PythonInterpreter();
        //set the variable names for the python script
        interp.set("readFileName", readFileName);
        interp.set("writeFileName", "C:\\Users\\essej\\Documents\\ytSent\\src\\resources\\cleanedComments.txt");
        //run the script
        interp.execfile(scriptName);
        //close the interpreter
    }

    public static void test(NaiveBayes model, String writeFileName) throws Exception {
        //set the correct script name
        String scriptName = "C:\\Users\\essej\\Documents\\ytSent\\src\\resources\\buildArffForUnlabeled.py";
        //initialize the python interpreter
        PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
        org.python.util.PythonInterpreter interp = new org.python.util.PythonInterpreter();
        //set the variable names for the python script
        interp.set("readFileName", "C:\\Users\\essej\\Documents\\ytSent\\src\\resources\\cleanedComments.txt");
        interp.set("writeFileName", writeFileName);
        //run the script
        interp.execfile(scriptName);
        //close the interpreter
        interp.close();

        Instances unlabeled = new Instances(
                                new BufferedReader(
                                        new FileReader("C:\\Users\\essej\\Documents\\ytSent\\src\\resources\\"+writeFileName)));

        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

        Instances labeled = new Instances(unlabeled);
        labeled.setClassIndex(labeled.numAttributes() - 1);
        //System.out.println(labeled.toString());

        for (int i = 0; i < unlabeled.numInstances(); i++) {
            System.out.println(i);
            System.out.println(unlabeled.instance(i));
            double clsLabel = model.classifyInstance(unlabeled.instance(i));
            labeled.instance(i).setClassValue(clsLabel);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(writeFileName));
        writer.write(labeled.toString());
        writer.newLine();
        writer.flush();
        writer.close();

    }

    public static void main (String[] args) throws Exception {
        ytClassyfier ytc = new ytClassyfier("C:\\Users\\essej\\Documents\\ytSent\\src\\resources\\trainingModel.arff");
        NaiveBayes bayesModel = ytc.constructBayes(ytc.data);
        System.out.println(args[0]);
        clean(args[0]);
        //clean("C:\\Users\\essej\\Documents\\cmpu-366\\src\\resources\\comments-NQD4n-6Q_q8.csv");
        test(bayesModel, args[1]);
        //test(bayesModel,"C:\\Users\\essej\\Documents\\cmpu-366\\src\\resources\\classifiedComments.arff");
        //test(bayesModel, "C:\\Users\\essej\\OneDrive\\Documents\\VASSAR\\Senior_Year\\Spring\\CMPU366\\Final-Project\\classyfiedComments.arff");
    }
}
