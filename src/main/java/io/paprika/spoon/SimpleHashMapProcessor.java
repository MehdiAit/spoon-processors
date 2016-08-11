package io.paprika.spoon;

import android.util.ArrayMap;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import utils.CsvReader;

import java.util.*;

/**
 * Created by kevin on 01/07/16
 *
 * HMU Handler
 */
public class SimpleHashMapProcessor extends AbstractProcessor<CtClass> {

    public SimpleHashMapProcessor(String file){
        System.out.println("Processor HashMapProcessor Start ... ");
    }

    /**
     * check if the method name is present in the reference csv file
     *
     * @param invok a method of a class
     * @return is present or not ?
     */
    @Override
    public boolean isToBeProcessed(CtClass invok) {
        return true;
    }

    public void process(CtClass invok){
        List<CtType> list = invok.getElements(new AbstractFilter<CtType>(CtType.class) {
            @Override
            public boolean matches(CtType element) {
                return element.getSimpleName().equals("HashMap");
            }
        });

        System.out.println(list.size());
    }
}