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
    private HashSet<String> hmuMethods;

    public MethodLogProcessorHMU(String smellFile)
    {
        System.out.println("Processor MethodLogProcessorHMU Start ... ");
        // Get applications information from the CSV - output
        hmuMethods = CsvReader.formatCsv(smellFile);
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        String class_file = candidate.getPosition().getFile().getName().split("\\.")[0];

        for(String occurence : hmuMethods){
            String csvClassName = occurence.substring(occurence.lastIndexOf(".")+1);

            if(class_file.equals(csvClassName) &&
                    occurence.split("#")[0].equals(candidate.getSimpleName().split("\\(")[0])){
                return true;
            }
        }

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
