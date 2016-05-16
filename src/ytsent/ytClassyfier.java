    
package ytsent;

/**
 * Created by EssejSllim on 5/6/2016.
 */

import java.awt.*;
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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

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
        String scriptName = "src\\resources\\cleanCommentsCSV.py";
        //initialize the Python interpreter
        PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
        org.python.util.PythonInterpreter interp = new org.python.util.PythonInterpreter();
        //set the variable names for the python script
        interp.set("readFileName", readFileName);
        interp.set("writeFileName", "src\\resources\\cleanedComments.txt");
        //run the script
        interp.execfile(scriptName);
        //close the interpreter
    }

    public static String test(NaiveBayes model, String writeFileName) throws Exception {
        //set the correct script name
        String scriptName = "src\\resources\\buildArffForUnlabeled.py";
        //initialize the python interpreter
        PythonInterpreter.initialize(System.getProperties(), System.getProperties(), new String[0]);
        org.python.util.PythonInterpreter interp = new org.python.util.PythonInterpreter();
        //set the variable names for the python script
        interp.set("readFileName", "src\\resources\\cleanedComments.txt");
        interp.set("writeFileName", writeFileName);
        //run the script
        interp.execfile(scriptName);
        //close the interpreter
        interp.close();

        Instances numericData = new Instances(
                                new BufferedReader(
                                        new FileReader(writeFileName)));

        numericData.setClassIndex(numericData.numAttributes() - 1);
        
        //create nominal insances
        NumericToNominal filter = new NumericToNominal();
        String[] options = new String[2];
        options[0] = "-R";
        options[1] = "1-4";
        filter.setOptions(options);
        filter.setInputFormat(numericData);
        
        Instances nominalData = Filter.useFilter(numericData, filter);
        nominalData.setClassIndex(nominalData.numAttributes() - 1);
        
        Instances labeled = new Instances(nominalData);
        labeled.setClassIndex(labeled.numAttributes() - 1);
        //System.out.println(labeled.toString());

        double numPositive = 0, numNegative = 0, numNeutral = 0;
        double totalSent = 0;
        for (int i = 0; i < numericData.numInstances(); i++) {
            //System.out.println(i);
            //System.out.println(unlabeled.instance(i));
            double clsLabel = model.classifyInstance(numericData.instance(i));
            double nominalLabel = model.classifyInstance(nominalData.instance(i)) -1;
            labeled.instance(i).setClassValue(clsLabel);
            totalSent = totalSent + clsLabel;
          
            if (nominalLabel>0){
                numPositive ++;
            } else if (nominalLabel<0){
                numNegative ++;
            } else {
                numNeutral++;
            }
        }
       
        
        double avgSent = totalSent/nominalData.numInstances();
        String output;
        String message;
        if(avgSent > 0.75) {
            message = "This video's comments are overwhelmingly positive";
        }
        else if (avgSent > 0.5) {
            message = "This video's comments are mostly positive";
        }
        else if (avgSent > 0.25) {
            message = "This video's comments are somewhat positive";
        }
        else if(avgSent > -0.25) {
            message = "This video's comments are fairly neutral";
        }
        else if (avgSent > -0.5) {
            message = "This video's comments are somewhat negative";
        }
        else if (avgSent > -0.75) {
            message = "This video's comments are mostly negative";
        }
        else {
            message = "This video's comments are overwhelmingly negative";
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(writeFileName));
        writer.write(labeled.toString());
        writer.newLine();
        writer.flush();
        writer.close();
        output = "<html><center>" + message + "<br>" + "Average Sentiment:  " + avgSent
                + "<br> Positive Comments: " + nicePercent(numPositive, labeled.numInstances())
                + "<br> Neutral Comments: " + nicePercent(numNeutral, labeled.numInstances())
                + "<br> Negative Comments: " + nicePercent(numNegative, labeled.numInstances())
                + "</center></html>";
        return(output);
    }
    static String nicePercent(double part, double whole){
        return Double.toString(((double) Math.round(10000*part/whole)) /100)+ "%";
    }
}
