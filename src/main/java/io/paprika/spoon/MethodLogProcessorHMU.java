package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import utils.CsvReader;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by mehdi on 16-06-27.
 */
public class MethodLogProcessorHMU extends AbstractProcessor<CtMethod> {
    private HashSet<String> mimMethode;

    public MethodLogProcessorHMU()
    {
        System.out.println("Processor MethodLogProcessorHMU Start ... ");
        // Get applications information from the CSV - output
        formatCsv();
    }

    // Format the Csv output to get the IGS invocation and position
    private void formatCsv(){
        mimMethode = new HashSet<>();

        ArrayList<String> csv_reader = CsvReader.csv("Soundwaves_HMU_filtered");
        for (String e : csv_reader) {
            //TODO splite in the CSV reader
            String [] split = e.split(",");
            // Method name
            mimMethode.add(split[1].split("#")[0]);
        }
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        for (String e : mimMethode) if(e.equals(candidate.getSimpleName().split("\\(")[0])) return true ;
        return false;
    }

    public void process(CtMethod element) {
        CtStatement logMethod = getFactory().Code().createCodeSnippetStatement("android.util.Log.d(\"SMELL\",\"HMU\")");

        try{
            element.getBody().insertBegin(logMethod);
        }
        // In case of empty method
        catch(NullPointerException e){
            element.getFactory().Code().createCtBlock(logMethod);
        }

        getEnvironment().report(this, Level.WARN, element, "INFO :" + element.getReference());
    }
}
