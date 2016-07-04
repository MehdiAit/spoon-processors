package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import utils.CsvReader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by geoffrey on 08/04/16
 * Edited by mehdi on 30/05/16.
 *
 * MIM Handler
 */
public class HashMapProcessor extends AbstractProcessor<CtMethod> {

    private ArrayList<String> hmuOccurences;
    private HashMapUsage useCase = HashMapUsage.Normal;

    // Format the Csv output to get the IGS invocation and position
    private void formatCsv(){
        hmuOccurences = new ArrayList<>();
        ArrayList<String> csv_reader = CsvReader.csv("igs");

        for (String e : csv_reader) {
            String [] split = e.split(",");
            hmuOccurences.add(split[1]);
        }
    }

    @Override
    public boolean isToBeProcessed(CtMethod invok) {
        // Get applications information from the CSV - output
        formatCsv();

        // Get the method name + parameters
        String[] call = invok.getReference().toString().split("#")[1].split("\\(|\\)");

        // Get the file class name
        String class_name = invok.getPosition().getFile().getName().split("\\.")[0];

        /*
        * Check if current method is in csv
        * Then check if HashMap is used
        */

        for (String hmu: hmuOccurences) {
            String className = hmu.substring(hmu.lastIndexOf("\\.")+1);
            String methodName = hmu.split("#")[0];

            if(class_name.equals(className) && methodName.equals(call[0])){
                // Check if HashMap is used then find usage case
                if(invok.getType().getActualClass().getName().matches(".*HashMap.*")){
                    /*
                    * Case 1 : Normal affectation -> HashMap<>()
                    * - simple transformation to ArrayMap
                    *
                    * Case 2 : Parameterized affectation -> HashMap<>(int initialCapacity)
                    * - simple transformation ArrayMap<>(initialCapacity)
                    *
                    * Case 3 : Parameterized affectation -> HashMap<>(Map mapToCopy)
                    * - Create empty ArrayMap
                    * - use addAll(mapToCopy) with the parameter
                    *
                    * Case 4 : clone usage -> HashMap.clone()
                    * - Use a copy constructor
                    *
                    * Case 5 : Two parameter hashMap -> HashMap<>(int,float)
                    * - Ignore second parameter
                    */
                    if(call.length == 1){
                        // Check how the instance is created (new or clone)
                        useCase = HashMapUsage.Normal;

                        //useCase = HashMapUsage.Clone;
                    }
                    else if(call.length == 2){
                        // Check if it is an int or a Map
                        useCase = HashMapUsage.OneParameterInt;
                        //useCase = HashMapUsage.OneParameterMap;
                    }
                    else if(call.length == 3){
                        useCase = HashMapUsage.TwoParameter;
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public void process(CtMethod invok) {



        CtClass root = invok.getParent(CtClass.class);


        switch(useCase){
            case Normal:
                break;
            case OneParameterInt:
                break;
            case OneParameterMap:
                break;
            case TwoParameter:
                break;
            case Clone:
                break;
            default:
                break;
        }

    }

    private enum HashMapUsage{
        Normal,
        OneParameterInt,
        OneParameterMap,
        TwoParameter,
        Clone
    }
}