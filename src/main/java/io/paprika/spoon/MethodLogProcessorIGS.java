package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.support.reflect.code.CtConstructorCallImpl;
import utils.CsvReader;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by mehdi on 16-06-27.
 */
public class MethodLogProcessorIGS extends AbstractProcessor<CtMethod> {
    private HashSet<String> igsInvocation;

    public MethodLogProcessorIGS(String file)
    {
        System.out.println("Processor MethodLogProcessorIGS Start ... ");
        // Get applications information from the CSV - output
        igsInvocation = CsvReader.formatCsv(file);
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {

        String class_file = "";
        try {
            //class_file = candidate.getPosition().getFile().getName().split("\\.")[0];
            String tmp = candidate.getParent(CtClass.class).getQualifiedName();
            class_file = tmp.substring(tmp.lastIndexOf(".")+1);
        }
        catch (NullPointerException e){
            return false;
        }


        for(String occurence : igsInvocation){
            String csvClassName = occurence.substring(occurence.lastIndexOf(".")+1);

            if(class_file.equals(csvClassName) &&
                    occurence.split("#")[0].equals(candidate.getSimpleName().split("\\(")[0])){
                return true;
            }
        }

        return false;
    }

    public void process(CtMethod element) {
        CtStatement logMethod = getFactory().Code().createCodeSnippetStatement("android.util.Log.d(\"SMELL\",\"IGS\")");

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
