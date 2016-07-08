package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import utils.CsvReader;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;

/**
 * Created by mehdi on 16-06-27.
 */
public class MethodLogProcessorMIM extends AbstractProcessor<CtMethod> {
    private HashSet<String> mimMethods;

    public MethodLogProcessorMIM()
    {
        System.out.println("Processor MethodLogProcessorMIM Start ... ");
        // Get applications information from the CSV - output
        mimMethods = CsvReader.formatCsv("Soundwaves_MIM_filtered_valid");
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {

        return checkValidToCsv(candidate) && checkAnnotation(candidate);
    }

    public void process(CtMethod element) {

        CtStatement logMethod = getFactory().Code().createCodeSnippetStatement("android.util.Log.d(\"SMELL\",\"MIM\")");

        try{
            element.getBody().insertBegin(logMethod);
        }
        // In case of empty method
        catch(NullPointerException e){
            element.getFactory().Code().createCtBlock(logMethod);
        }

        getEnvironment().report(this, Level.WARN, element, "INFO :" + element.getReference());
    }

    private boolean checkValidToCsv(CtMethod candidate){
        String class_file = candidate.getPosition().getFile().getName().split("\\.")[0];

        for(String occurence : mimMethods){
            String csvClassName = occurence.substring(occurence.lastIndexOf(".")+1);

            if(class_file.equals(csvClassName) &&
                    occurence.split("#")[0].equals(candidate.getSimpleName().split("\\(")[0])){
                return true;
            }
        }

        return false;
    }

    private boolean checkAnnotation(CtMethod candidate){
        for(CtAnnotation annotation : candidate.getAnnotations()){
            if(annotation.toString().trim().matches("(.*)@Override(.*)")){
                return false;
            }
        }

        return true;
    }
}
