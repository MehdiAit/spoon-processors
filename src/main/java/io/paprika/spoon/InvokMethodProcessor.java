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
import java.util.List;

/**
 * Created by mehdi on 04/05/16
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
    String getField = null;

    // Format the Csv output to get the IGS invocation and position
    private void formatCsv(){
        appInfo = new HashMap<>();
        igsName = new ArrayList<>();
        ArrayList<String> csv_reader = CsvReader.csv("igs");

        for (String e : csv_reader) {
            String [] split = e.split(",");
            // 1; Where the IGS has been invoked - 2; The IGS invoked
            igsName.add(split[2]);
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
        // Get applications information from the CSV - output
        formatCsv();

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
                        // Is it a getter or setter ? if split.length > 1 this is a setter
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
        if (isGetter){
            //Use Expression
            //CtExpression igsGetter = getFactory().Code().createCodeSnippetExpression("var_a");
            //invok.replace(igsGetter);

            isGetter = false;
            getEnvironment().report(this, Level.WARN, invok, "INFO : GETTER on --> " + invok.getPosition());
        }
        else if(isSetter){
            // Get the root class
            CtClass root = (CtClass)invok.getParent().getParent().getParent();
            // Get the IGS or Setter
            CtMethod method = (CtMethod) root.getMethodsByName(igsInvocationName).get(0);

            // Filter to get the field of the getter/setter
            method.getBody().getLastStatement().getElements(new AbstractFilter<CtAssignment>(CtAssignment.class) {
                @Override
                public boolean matches(CtAssignment element) {
                    getField = element.getAssignment().toString();
                    return super.matches(element);
                }
            });

            //Use CtStatement for code transformation
            CtStatement igsSetter = getFactory().Code().createCodeSnippetStatement(getField + " = " + invok.getArguments().get(0));
            invok.replace(igsSetter);
            isSetter = false;
            getEnvironment().report(this, Level.WARN, invok, "INFO : SETTER on --> " + invok.getPosition());
        }

    }
}
