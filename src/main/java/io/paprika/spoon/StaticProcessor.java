package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import utils.CsvReader;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by geoffrey on 08/04/16
 * Edited by mehdi on 30/05/16.
 *
 * MIM Handler
 */
public class StaticProcessor extends AbstractProcessor<CtMethod> {

    HashSet<String> meth_toStatic;

    public StaticProcessor()
    {
        System.out.println("Processor StaticProcessor Start ... ");
        // Get applications information from the CSV - output
        meth_toStatic = CsvReader.formatCsv("Soundwaves_MIM_filtered_valid");
    }

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {

        return checkValidToCsv(candidate) && checkAnnotation(candidate);
    }

    public void process(CtMethod element) {
        element.addModifier(ModifierKind.STATIC);
        getEnvironment().report(this, Level.WARN, element, "INFO :" + element.getReference());
        System.out.println("----------- Static processor end --------------");
    }

    private boolean checkValidToCsv(CtMethod candidate){
        String class_file = candidate.getPosition().getFile().getName().split("\\.")[0];

        for(String occurence : meth_toStatic){
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