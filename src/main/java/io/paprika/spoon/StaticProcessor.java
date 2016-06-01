package io.paprika.spoon;

import org.apache.log4j.Level;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import utils.CsvReader;

import java.util.ArrayList;

/**
 * Created by geoffrey on 08/04/16
 * Edited by mehdi on 30/05/16.
 */
public class StaticProcessor extends AbstractProcessor<CtMethod> {

    ArrayList<String> meth_toStatic = CsvReader.csv("mim");

    @Override
    public boolean isToBeProcessed(CtMethod candidate) {
        for (String e: meth_toStatic
             ) {
            if (e.equals(candidate.getReference().toString())){
                return true;
            }
        }
        return false;
    }

    public void process(CtMethod element) {
        element.addModifier(ModifierKind.STATIC);
        getEnvironment().report(this, Level.WARN, element, "INFO :" + element.getReference());
        System.out.println("----------- Static processor end --------------");
    }
}