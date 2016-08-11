package io.paprika.spoon;

import android.util.ArrayMap;
import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
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
 * Created by kevin on 01/07/16
 *
 * HMU Handler
 */
public class HashMapProcessor extends AbstractProcessor<CtMethod> {

    private Set<String> hmuOccurences;
    private boolean withAssignedCast = false;
    private List<CtLocalVariableReference> modifiedVariables = new ArrayList<>();
    private List<CtFieldReference> modifiedFields = new ArrayList<>();

    public HashMapProcessor(String file){
        System.out.println("Processor HashMapProcessor Start ... ");
        // Get applications information from the CSV - output
        hmuOccurences = CsvReader.formatCsv(file);
    }

    /**
     * check if the method name is present in the reference csv file
     *
     * @param invok a method of a class
     * @return is present or not ?
     */
    @Override
    public boolean isToBeProcessed(CtMethod invok) {
        return checkValidToCsv(invok);
    }

    public void process(CtMethod invok){
        // Clear last method consequences
        this.modifiedVariables.clear();
        this.modifiedFields.clear();

        processFields(invok);
        processLocalVars(invok);
        if(this.modifiedVariables.size() + this.modifiedFields.size() > 0){
            analyzeConsequences(invok);
        }
    }

    /**
     * Transforms HMU that are assigned to class fields (attributes)
     *
     * @param invok
     */
    private void processFields(CtMethod invok) {
        CtClass root = invok.getParent(CtClass.class);

        List<CtAssignment> list = invok.getBody().getElements(new AbstractFilter<CtAssignment>(CtAssignment.class) {
            @Override
            public boolean matches(CtAssignment element) {
                try {
                    // Finds any Assignment where the dynamic element is a Constructor for an HashMap and the static one is a Field
                    return (element.getAssignment().getType().getSimpleName().equals("HashMap") &&
                            (element.getAssignment() instanceof  CtConstructorCall)) &&
                            (element.getAssigned() instanceof CtFieldWrite);
                }
                catch (NullPointerException e){
                    return false;
                }
            }
        });

        for (CtAssignment codeLine : list) {

            CtFieldWrite assigned = (CtFieldWrite) codeLine.getAssigned();

            // Add to array of modified variables
            this.modifiedFields.add(assigned.getVariable());

            withAssignedCast = root.getField(assigned.getVariable().toString()).getType().getActualClass().equals(HashMap.class);

            if(withAssignedCast){
                CtField attribute = root.getField(assigned.getVariable().toString());

                List<CtTypeReference<?>> types = attribute.getType().getActualTypeArguments();

                attribute.setType(getFactory().Code().createCtTypeReference(ArrayMap.class));

                attribute.getType().setActualTypeArguments(types);
            }


            CtConstructorCall constr = (CtConstructorCall) codeLine.getAssignment();
            CtExpression arg = null;


            switch (constr.getArguments().size()){
                case 0:
                    codeLine.getAssignment().replace(
                            getFactory().Code().createCodeSnippetExpression(
                                    codeLine.getAssignment().toString().replaceFirst("HashMap","ArrayMap")
                            )
                    );
                    break;
                case 1:
                    arg = constr.getArguments().get(0) instanceof CtLiteral ?
                            (CtLiteral)constr.getArguments().get(0) :
                            (CtVariableRead)constr.getArguments().get(0);

                    if(arg.getType().getActualClass().equals(HashMap.class) ||
                            arg.getType().getActualClass().equals(Map.class)){
                        constr.removeArgument(arg);
                        constr.replace(
                                getFactory().Code().createCodeSnippetExpression(
                                        constr.toString().replaceFirst("HashMap","ArrayMap")
                                )
                        );
                        codeLine.insertAfter(getFactory().Code().createCodeSnippetStatement(codeLine.getAssigned()+".putAll("+arg+")"));
                    }
                    else if (arg.getType().getActualClass().equals(Integer.class) ||
                            arg.getType().getSimpleName().equals("int")){
                        constr.replace(
                                getFactory().Code().createCodeSnippetExpression(
                                        constr.toString().replaceFirst("HashMap","ArrayMap")
                                )
                        );
                    }
                    else{
                        System.err.println("HMU case not handled");
                        System.err.println(codeLine);
                    }

                    break;
                case 2:
                    constr.removeArgument((CtExpression) constr.getArguments().get(1));
                    constr.replace(
                            getFactory().Code().createCodeSnippetExpression(
                                    constr.toString().replaceFirst("HashMap","ArrayMap")
                            )
                    );
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Transforms HMU that are assigned to a local variable of the method
     *
     * @param invok
     */
    private void processLocalVars(CtMethod invok) {

        List<CtLocalVariable> list = invok.getBody().getElements(new AbstractFilter<CtLocalVariable>(CtLocalVariable.class) {
            @Override
            public boolean matches(CtLocalVariable element) {
                try {

                    return (element.getAssignment().getType().getSimpleName().equals("HashMap") &&
                            (element.getAssignment() instanceof  CtConstructorCall));
                }
                catch (NullPointerException e){
                    return false;
                }
            }
        });

        for (CtLocalVariable codeLine : list) {

            // Add to array of modified variables
            this.modifiedVariables.add(codeLine.getReference());

            // Checks if the variable declaration has to be changed
            withAssignedCast = codeLine.getType().getActualClass().equals(HashMap.class);

            if(withAssignedCast){

                List<CtTypeReference<?>> types = codeLine.getType().getActualTypeArguments();

                codeLine.setType(getFactory().Code().createCtTypeReference(ArrayMap.class));

                codeLine.getType().setActualTypeArguments(types);
            }

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
                    try {
                        arg = (CtVariableRead)constr.getArguments().get(0);
                    }
                    catch (ClassCastException e){
                        return;
                    }

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
                        codeLine.insertAfter(getFactory().Code().createCodeSnippetStatement(codeLine.getSimpleName()+".putAll("+arg+")"));

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
        }
    }

    /**
     * Finds the consequences (methods of other class to modify) of the previous transformations
     *
     * @param invok
     */
    private void analyzeConsequences(CtMethod invok){
        final List<CtLocalVariableReference> localVars = this.modifiedVariables;

        List<CtLocalVariableReference> list = invok.getBody().getElements(new AbstractFilter<CtLocalVariableReference>(CtLocalVariableReference.class) {
            @Override
            public boolean matches(CtLocalVariableReference element) {
                return localVars.contains(element);
            }
        });

        for (CtLocalVariableReference var : list){
            CtElement myRoot = var.getParent().getParent();

            // If CtReturn : change method signature if necessary
            if(myRoot instanceof CtReturn){

            }
            // If CtInvocation : check if the target points to the same reference
            else if(myRoot instanceof CtInvocation){
                CtLocalVariableReference target = null;
                try{
                    target = (CtLocalVariableReference) ((CtVariableRead) ((CtInvocation) myRoot).getTarget()).getVariable();
                }
                catch(ClassCastException e){
                    System.err.println(e.getMessage());
                }

                // If it's not an invocation of our variable then it is a consequence to correct
                if(target != var){
                    /*
                    * Two cases :
                    * Map in Map
                    * and custom class
                    */
                    if (((CtInvocation) myRoot).getExecutable().getDeclaringType().getSimpleName().equals("Map")){
                        // Static part
                        for(CtTypeReference<?> argument : target.getDeclaration().getType().getActualTypeArguments()){
                            if(argument.getSimpleName().equals("HashMap")){
                                List<CtTypeReference<?>> types = argument.getActualTypeArguments();
                                argument.replace(getFactory().Code().createCtTypeReference(ArrayMap.class));
                                argument.setActualTypeArguments(types);
                            }
                        }
                        // Dynamic part
                        for(CtTypeReference<?> argument : target.getDeclaration().getAssignment().getType().getActualTypeArguments()){
                            if(argument.getSimpleName().equals("HashMap")){
                                List<CtTypeReference<?>> types = argument.getActualTypeArguments();
                                argument.replace(getFactory().Code().createCtTypeReference(ArrayMap.class));
                                argument.setActualTypeArguments(types);
                            }
                        }
                    }
                    else{
                        // TODO
                        System.out.println();
                    }
                }

            }
            // TODO If CtAssignment : should have been corrected !!!
            else if(myRoot instanceof CtAssignment){
                System.err.println("CtAssignment not corrected !");
                System.err.println(myRoot);
            }
            else{
                System.err.println("Unknown case !");
                System.err.println(myRoot);
            }
        }




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
}