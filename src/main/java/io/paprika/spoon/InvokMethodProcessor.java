package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.AbstractFilter;
import utils.CsvReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mehdi on 04/05/16
 *
 * IGS Handler
 */
public class InvokMethodProcessor extends AbstractProcessor<CtInvocation> {
    // Application name and methods invocation
    private HashMap<String, ArrayList<String>> appInfo;
    private ArrayList<String> igsName;
    // is it a getter or setter
    private boolean isGetter = false;
    private boolean isSetter = false;

    // Return the getter/setter name
    private String igsInvocationName = null;

    // Return the getter/setter field application
    private String getField = null;

    public InvokMethodProcessor(String file)
    {
        System.out.println("Processor InvokMethodProcessor Start ... ");
        // Get applications information from the CSV - output
        formatCsv(file);
    }

    // Format the Csv output to get the IGS invocation and position
    private void formatCsv(String file){
        appInfo = new HashMap<>();
        igsName = new ArrayList<>();
        ArrayList<String> csv_reader = CsvReader.csv(file);

        for (String e : csv_reader) {
            String [] split = e.split(",");
            // 0; App key (name) - 1; Where the IGS has been invoked - 2; The IGS invoked
            igsName.add(split[2]);
            // Rewrites on the same key until it changes
            appInfo.put(split[1], igsName);
        }
    }

    // Format the Spoon methods information to .... Csv output
    private String [] spoonFormat(String spoonMethod){
        // Split the Spoon method name
        return spoonMethod.split("#")[1].split("\\(|\\)");

        /* Return the type if it not null (ex : setVara(int) --> int)
        String[] split = spoonMethod.split("#")[1].split("\\(")//[1].split("\\(|\\)");
         */
    }

    @Override
    public boolean isToBeProcessed(CtInvocation invok) {
        // TODO change to CtMethod for faster filter
        // Get the executable in the current file
        String my_igs = invok.getExecutable().toString();

        // Get the file class name
        String class_file = invok.getPosition().getFile().getName().split("\\.")[0];

        for (String e: appInfo.keySet()) {
            String csvClassName = e.split("\\.")[e.split("\\.").length - 1];
            if (class_file.equals(csvClassName)){

                String [] methodName = spoonFormat(my_igs);
                for (String f: appInfo.get(e)) {
                    if(methodName[0].equals(f.split("#")[0])){
                        // Is it a getter or setter ? if split.length > 1 (a parameter exist) this is a setter
                        if (methodName.length > 1) {
                            isSetter = true;
                            igsInvocationName = methodName[0];
                            return true;
                        }
                        else {
                            isGetter = true;
                            igsInvocationName = methodName[0];
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void process(CtInvocation invok) {
        // Get the root class
        CtClass root = invok.getParent(CtClass.class);

        // Get the IGS or Setter
        CtMethod method = (CtMethod) root.getMethodsByName(igsInvocationName).get(0);

        if (isGetter){
            // Filter to get the field of the getter/setter
            method.getBody().getLastStatement().getElements(new AbstractFilter<CtReturn>(CtReturn.class) {
                @Override
                public boolean matches(CtReturn element) {
                    getField = element.getReturnedExpression().toString();
                    return super.matches(element);
                }
            });

            //Use Expression
            CtExpression igsGetter = getFactory().Code().createCodeSnippetExpression(getField);
            invok.replace(igsGetter);
            isGetter = false;
            getEnvironment().report(this, Level.WARN, invok, "INFO : GETTER on --> " + invok.getPosition());
        }
        else if(isSetter){
            // Filter to get the field of the getter/setter
            method.getBody().getLastStatement().getElements(new AbstractFilter<CtAssignment>(CtAssignment.class) {
                @Override
                public boolean matches(CtAssignment element) {
                    getField = element.getAssignment().toString();
                    return super.matches(element);
                }
            });

            //Use CtStatement for code transformation
            //TODO : change the string arg on "createCodeSnippetStatement" with a "CtAssignment" class
            CtStatement igsSetter = getFactory().Code().createCodeSnippetStatement(getField + " = " + invok.getArguments().get(0));
            invok.replace(igsSetter);
            isSetter = false;
            getEnvironment().report(this, Level.WARN, invok, "INFO : SETTER on --> " + invok.getPosition());
        }

    }
}
