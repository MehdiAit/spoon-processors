package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.reflect.visitor.filter.ReferenceTypeFilter;
import spoon.reflect.visitor.filter.RegexFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtConstructorCallImpl;
import spoon.support.reflect.reference.SpoonClassNotFoundException;
import utils.CsvReader;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by geoffrey on 08/04/16
 * Edited by mehdi on 30/05/16.
 *
 * MIM Handler
 */
public class HashMapProcessor extends AbstractProcessor<CtMethod> {

    private HashSet<String> hmuOccurences;
    private HashMapUsage useCase = HashMapUsage.Normal;
    private boolean withAssignedCast = false;

    public HashMapProcessor(){
        System.out.println("Processor HashMapProcessor Start ... ");
        // Get applications information from the CSV - output
        hmuOccurences = CsvReader.formatCsv("Telegram_HMU_filtered_valid");
    }

    @Override
    public boolean isToBeProcessed(CtMethod invok) {

        // Get applications information from the CSV - output
        //formatCsv();

        // Get the method name + parameters
        //String[] call = invok.getReference().toString().split("#")[1].split("\\(|\\)");

        return checkValidToCsv(invok);
    }

    public void process(CtMethod invok) {
        CtClass root = invok.getParent(CtClass.class);

        List<CtAssignment> list = invok.getBody().getElements(new AbstractFilter<CtAssignment>(CtAssignment.class) {
            @Override
            public boolean matches(CtAssignment element) {
                try {
                    return (element.getAssignment().getType().getActualClass().equals(HashMap.class));
                }
                catch (SpoonClassNotFoundException e){
                    System.err.println(e.getMessage());
                }
                return false;
            }
        });



        for (CtAssignment codeLine : list) {

            CtExpression assigned = codeLine.getAssigned();
            withAssignedCast = codeLine.getAssigned().getType().getActualClass().equals(HashMap.class);




            /*
            List<CtVariableReference> refs = codeLine.getAssigned().getReferences(new ReferenceTypeFilter<CtVariableReference>(CtVariableReference.class));
            for(CtVariableReference ref : refs){
                System.out.println(ref.toString());
            }
            */

            CtConstructorCall constr = (CtConstructorCall) codeLine.getAssignment();
            CtVariableRead arg;

            switch (constr.getArguments().size()){
                case 0:
                    codeLine.getAssignment().replace(
                            getFactory().Code().createCodeSnippetExpression(
                                    codeLine.getAssignment().toString().replaceFirst("HashMap","ArrayMap")
                            )
                    );
                    break;
                case 1:
                    arg = (CtVariableRead)constr.getArguments().get(0);

                    if(arg.getType().getActualClass().equals(HashSet.class)){
                        constr.replace(
                                getFactory().Code().createCodeSnippetExpression(
                                        constr.toString().replaceFirst("HashMap","ArrayMap")
                                )
                        );
                    }
                    else {
                        constr.removeArgument(arg);
                        constr.replace(
                                getFactory().Code().createCodeSnippetExpression(
                                        constr.toString().replaceFirst("HashMap","ArrayMap")
                                )
                        );
                        codeLine.insertAfter(getFactory().Code().createCodeSnippetStatement(codeLine.getAssigned()+".putAll("+arg+")"));

                        //root.getField("mValuesMap").replace(getFactory().Field().create(root.getField("mValuesMap")));

                        System.out.println(root.getField(
                                codeLine.getAssigned().toString().substring(
                                        codeLine.getAssigned().toString().lastIndexOf(".")+1
                                )
                        ).toString());
                    }

                    break;
                case 2:
                    arg = (CtVariableRead)constr.getArguments().get(1);

                    constr.removeArgument(arg);
                    constr.replace(
                            getFactory().Code().createCodeSnippetExpression(
                                    constr.toString().replaceFirst("HashMap","ArrayMap")
                            )
                    );
                    break;
                default:
                    break;
            }


            //codeLine.getFactory().Code().createCodeSnippetExpression()

            System.out.println("I found an HashMap in "+invok.getSimpleName());
        }





//        switch(useCase){
//            case Normal:
//                break;
//            case OneParameterInt:
//                break;
//            case OneParameterMap:
//                break;
//            case TwoParameter:
//                break;
//            case Clone:
//                break;
//            default:
//                break;
//        }

    }

    private boolean checkValidToCsv(CtMethod candidate){
        String class_file = candidate.getPosition().getFile().getName().split("\\.")[0];

        for(String occurence : hmuOccurences){
            String csvClassName = occurence.substring(occurence.lastIndexOf(".")+1);

            if(class_file.equals(csvClassName) &&
                    occurence.split("#")[0].equals(candidate.getSimpleName().split("\\(")[0])){
                return true;
            }
        }

        return false;
    }

    private enum HashMapUsage{
        Normal,
        OneParameterInt,
        OneParameterMap,
        TwoParameter,
        Clone
    }
}