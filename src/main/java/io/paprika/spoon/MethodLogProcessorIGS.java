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
public class MethodLogProcessorIGS extends AbstractProcessor<CtMethod> {
    private HashSet<String> igsInvocation;

    public MethodLogProcessorIGS()
    {
        System.out.println("Processor MethodLogProcessorIGS Start ... ");
        // Get applications information from the CSV - output
        formatCsv();
    }

    // Format the Csv output to get the IGS invocation and position
    private void formatCsv(){
        igsInvocation = new HashSet<>();

        ArrayList<String> csv_reader = CsvReader.csv("igs");
        for (String e : csv_reader) {
            String [] split = e.split(",");
            // 1; Where the IGS has been invoked - 2; The IGS invoked
            igsInvocation.add(split[1].split("#")[0]);
        }
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        for (String e : igsInvocation) if(e.equals(candidate.getSimpleName().split("\\(")[0])) return true ;
        return false;
    }

    public void process(CtMethod element) {
        CtStatement logMethod = getFactory().Code().createCodeSnippetStatement("android.util.Log.d(\"SMELL\",\"IGS\")");
        element.getBody().insertBegin(logMethod);
        getEnvironment().report(this, Level.WARN, element, "INFO :" + element.getReference());
    }

}
