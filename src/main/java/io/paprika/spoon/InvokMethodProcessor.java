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
        ArrayList<String> csv_reader = CsvReader.csv(file);

        for (String e : csv_reader) {
            String [] split = e.split(",");
            // 0, App key (name) - 1, Where the IGS has been invoked - 2, The IGS invoked
            String where = split[1];
            String who = split[2];

            if(appInfo.containsKey(where)){
                appInfo.get(where).add(who);
            }
            else{
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add(who);
                appInfo.put(where, tmp);
            }
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
        String [] tmp = my_igs.split("#")[0].split("\\.");
        class_file = tmp[tmp.length-1];

        for (String e: appInfo.keySet()) {
            String[] splitedElement = e.split("\\.");
            String csvClassName = splitedElement[splitedElement.length - 1];

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

        // If the size is equal to 0 its possible that the method doesn't excite in the class or the root is not a class ex: Enum
        if (root.getMethodsByName(igsInvocationName).size() > 0) {

            // Get the IGS or Setter
            CtMethod method = (CtMethod) root.getMethodsByName(igsInvocationName).get(0);

            if (method.getBody().getStatements().size() > 1){
                System.err.println(getField + " is calling a non simple method ("+igsInvocationName+" with a size of "+method.getBody().getStatements().size()+") at "+invok.getPosition());
                return;
            }

            if (isGetter) {
                // Filter to get the field of the getter/setter
                method.getBody().getLastStatement().getElements(new AbstractFilter<CtReturn>(CtReturn.class) {
                    @Override
                    public boolean matches(CtReturn element) {
                        getField = element.getReturnedExpression().toString();
                        return super.matches(element);
                    }
                });

                //Use Expression
                try{
                    CtExpression igsGetter = getFactory().Code().createCodeSnippetExpression(getField);
                    invok.replace(igsGetter);
                }
                catch (Exception e){
                    System.err.println(getField + " is false getter ("+igsInvocationName+" with a size of "+method.getBody().getStatements().size()+") at "+invok.getPosition());
                }
                isGetter = false;
                getEnvironment().report(this, Level.WARN, invok, "INFO : GETTER on --> " + invok.getPosition());
            } else if (isSetter) {
                // Filter to get the field of the getter/setter
                method.getBody().getLastStatement().getElements(new AbstractFilter<CtAssignment>(CtAssignment.class) {
                    @Override
                    public boolean matches(CtAssignment element) {
                        getField = element.getAssigned().toString();
                        return super.matches(element);
                    }
                });

                //Use CtStatement for code transformation
                //TODO : change the string arg on "createCodeSnippetStatement" with a "CtAssignment" class
                try{
                    CtStatement igsSetter = getFactory().Code().createCodeSnippetStatement(getField + " = " + invok.getArguments().get(0));
                    invok.replace(igsSetter);
                }
                catch (Exception e){
                    System.err.println(getField + " is false setter ("+igsInvocationName+" with a size of "+method.getBody().getStatements().size()+") at "+invok.getPosition());
                }

                isSetter = false;
                getEnvironment().report(this, Level.WARN, invok, "INFO : SETTER on --> " + invok.getPosition());
            }

        }
    }

}
