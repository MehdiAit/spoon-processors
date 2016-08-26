package io.paprika.spoon;

import android.util.ArrayMap;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.reference.SpoonClassNotFoundException;
import utils.CsvReader;

import java.util.*;

/**
 * Created by kevin on 01/07/16
 *
 * HMU Handler
 */
public class HashMapProcessor extends AbstractProcessor<CtClass<?>> {

    private Set<String> hmuOccurences;
    private boolean withAssignedCast = false;
    private Map<String, List<String>> modifiedVariables = new HashMap<>();
    private List<String> modifiedFields = new ArrayList<>();
    private Map<String, List<String>> waitingMethods = new HashMap<>();
    private Map<String, CtClass> previousClasses = new HashMap<>();

    public HashMapProcessor(String file){
        System.out.println("Processor HashMapProcessor Start ... ");
        // Get applications information from the CSV - output
        hmuOccurences = CsvReader.formatCsv(file);
    }

    /**
     * As the checks are both on the class and its methods, it would be redundant to check anything here
     *
     * @param invok a method of a class
     * @return True
     */
    @Override
    public boolean isToBeProcessed(CtClass<?> invok) {
        return true;
    }

    /**
     * Process the class in 3 steps :
     * 1 - Correct its methods which contains HMU and keep the modified variables
     * 2 - Iterate over every method of that class to find occurences of invocation having one of the modified variables as argument
     * 3 - Correct the methods behind those invocations
     *
     * @param invok the class analyzed
     */
    public void process(CtClass<?> invok){
        // Store the class for later use
        this.previousClasses.put(invok.getSimpleName(), invok);

        // Clear last class consequences
        this.modifiedVariables.clear();
        this.modifiedFields.clear();

        // Do trivial changes
        for (CtMethod<?> method : invok.getAllMethods()){
            this.modifiedVariables.put(method.getSimpleName(), new ArrayList<String>());

            // Keep modified local vars and class attributes (fields)
            if(checkValidToCsv(method)){
                processFields(method);
                processLocalVars(method);
            }
        }

        // Find consequences of those changes
        for (CtMethod<?> method : invok.getAllMethods()){
            if(this.modifiedVariables.get(method.getSimpleName()).size() + this.modifiedFields.size() > 0){
                analyzeConsequences(method);
            }


            // Do waiting job for this method if it exists
            // TODO : Does not work if the concerned class was analyzed before the submethod use was found.
            if (waitingMethods.get(invok.getSimpleName()) != null
                    && waitingMethods.get(invok.getSimpleName()).size() > 0){
                if(waitingMethods.get(invok.getSimpleName()).contains(method.getSimpleName())){
                    modifySubMethods(method);

                    // Clear job done
                    waitingMethods.get(invok.getSimpleName()).remove(method.getSimpleName());
                }
            }

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
            this.modifiedFields.add(assigned.getVariable().toString());

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
            this.modifiedVariables.get(invok.getSimpleName()).add(codeLine.getReference().toString());

            // Checks if the variable declaration has to be changed
            withAssignedCast = codeLine.getType().getActualClass().equals(HashMap.class);

            if(withAssignedCast){

                List<CtTypeReference<?>> types = codeLine.getType().getActualTypeArguments();

                codeLine.setType(getFactory().Code().createCtTypeReference(ArrayMap.class));

                codeLine.getType().setActualTypeArguments(types);
            }

            CtConstructorCall<?> constr = (CtConstructorCall) codeLine.getAssignment();
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
    private void analyzeConsequences(final CtMethod invok){
        final List<String> localVars = modifiedVariables.get(invok.getSimpleName());

        List<CtInvocation<?>> list = invok.getBody().getElements(new AbstractFilter<CtInvocation<?>>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation<?> element) {
                for(CtElement arg : element.getArguments()){
                    if(localVars.contains(arg.toString()) || modifiedFields.contains(arg.toString())){
                        return true;
                    }
                }
                return false;
            }
        });

        for (CtInvocation subMethod : list){
            analyzeSubMethods(subMethod.getExecutable());
        }
    }

    /**
     * Check if it is possible to modify the method now and modify it if so
     *
     * @param call the method being called
     */
    private void analyzeSubMethods(CtExecutableReference call){
        CtBlock truc;
        try{
            // If it is a custom class : step into the call to modify the method
            truc = call.getExecutableDeclaration().getBody();
        }
        catch (SpoonClassNotFoundException e){
            // Seek in the previous classes analyzed
            if(previousClasses.containsKey(call.getDeclaringType().getSimpleName())){
                CtClass<?> prevClass = previousClasses.get(call.getDeclaringType().getSimpleName());
                for (CtMethod<?> method : prevClass.getAllMethods()){
                    if(method.getSimpleName().equals(call.getSimpleName())){
                        modifySubMethods(method);
                    }
                }
                return;
            }

            // Wait for Spoon to analyze this custom class
            if(waitingMethods.get(call.getDeclaringType().getSimpleName()) == null){
                waitingMethods.put(call.getDeclaringType().getSimpleName(), new ArrayList<String>());
            }

            waitingMethods.get(call.getDeclaringType().getSimpleName()).add(call.getSimpleName());

            System.err.println("Class temporarily unavailable (waiting Spoon AST) : " + call.getDeclaringType().getSimpleName()
                    +"\n"+ call);
            return;
        }

        if (truc == null){
            // If the declaring type is a Collection : find the variable and change its type arguments
            System.err.println("Java defined class : " + call.getDeclaringType().getSimpleName());
            return;
        }

        modifySubMethods(call.getExecutableDeclaration());
        System.out.println("Modify call : "+call.getSimpleName()+" in "+call.getDeclaringType().getSimpleName());
    }

    /**
     * Correct the selected sub method
     *
     * @param call
     */
    private void modifySubMethods(CtExecutable call){
        // Find HashMap parameters
        final List<CtParameter<?>> parameters = call.getElements(new AbstractFilter<CtParameter<?>>(CtParameter.class) {
            @Override
            public boolean matches(CtParameter<?> element) {
                return element.getType().getSimpleName().equals("HashMap");
            }
        });

        // Convert those parameters
        for(CtParameter parameter : parameters){
            HashMapToArrayMap(parameter.getType());
        }

        // Find every uses of the selected parameters
        List<CtInvocation<?>> invoks = call.getBody().getElements(new AbstractFilter<CtInvocation<?>>(CtInvocation.class) {
            @Override
            public boolean matches(CtInvocation<?> element) {
                for(CtElement arg : element.getArguments()){
                    for (CtParameter parameter : parameters){
                        if(parameter.getSimpleName().equals(arg.toString())){
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        System.out.println("modify method : "+call.getSimpleName() + " in " + call.getParent(CtClass.class).getSimpleName());

        // Recursive sub method modifications
        for (CtInvocation subMethod : invoks){
            analyzeSubMethods(subMethod.getExecutable());
        }
    }

    /**
     * Simple conversion from a CtTypeReference
     *
     * @param ref
     */
    private void HashMapToArrayMap(CtTypeReference<?> ref){
        List<CtTypeReference<?>> types = ref.getActualTypeArguments();
        ref.replace(getFactory().Code().createCtTypeReference(ArrayMap.class));
        ref.setActualTypeArguments(types);
    }

    /**
     * Check if the method analyzed is present in the reference csv
     *
     * @param candidate the method analyzed
     * @return
     */
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