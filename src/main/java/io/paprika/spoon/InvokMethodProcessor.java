package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;

/**
 * Created by mehdi on 04/05/16
 */
public class InvokMethodProcessor extends AbstractProcessor<CtInvocation> {
    private boolean is_getter = false;
    private boolean is_seter = false;

    @Override
    public boolean isToBeProcessed(CtInvocation invok) {
        String my_igs = invok.getExecutable().toString();

       if (invok.getPosition().getFile().getName().equals("Test.java"))
        {

            if (my_igs.equals("com.example.geoffrey.myapplication.Test#getVar_a()")){
                this.is_getter = true;
                return true;
            }

            if (my_igs.equals("com.example.geoffrey.myapplication.Test#setVar_b(int)")){
                this.is_seter = true;
                return true;
            }

        }
        return false;
    }

    @Override
    public void process(CtInvocation invok) {
        if (is_getter){
            //Use Expression
            CtExpression igsGetter = getFactory().Code().createCodeSnippetExpression("var_a");
            invok.replace(igsGetter);
            getEnvironment().report(this, Level.WARN, invok, "INFO : GETTER on --> " + invok.getPosition());
        }
        else if(is_seter){
            //Use Statement
            CtStatement igsSetter = getFactory().Code().createCodeSnippetStatement("var_b = " + invok.getArguments().get(0));
            invok.replace(igsSetter);
            getEnvironment().report(this, Level.WARN, invok, "INFO : SETTER on --> " + invok.getPosition());
        }

    }
}
