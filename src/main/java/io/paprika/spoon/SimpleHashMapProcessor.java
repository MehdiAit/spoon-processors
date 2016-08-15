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
public class SimpleHashMapProcessor extends AbstractProcessor<CtClass<?>> {

    public SimpleHashMapProcessor(){
        System.out.println("Processor HashMapProcessor Start ... ");
    }

    /**
     * check if the method name is present in the reference csv file
     *
     * @param invok a method of a class
     * @return is present or not ?
     */
    @Override
    public boolean isToBeProcessed(CtClass<?> invok) {
        return true;
    }

    public void process(CtClass<?> ctClass){
        List<CtConstructorCall<?>> listConstrCall = ctClass.getElements(new AbstractFilter<CtConstructorCall<?>>(CtConstructorCall.class) {
            @Override
            public boolean matches(CtConstructorCall<?> element) {
                return element.getType().getSimpleName().equals("HashMap");
            }
        });

        // Ignore cases with just HashMap to ArrayMap conversion, the next list will do it anyway.
        for (CtConstructorCall<?> constructorCall : listConstrCall){
            convertConstructor(constructorCall);
        }

        List<CtTypeReference<?>> listTypeRefs = ctClass.getElements(new AbstractFilter<CtTypeReference<?>>(CtTypeReference.class) {
            @Override
            public boolean matches(CtTypeReference<?> element) {
                return element.getSimpleName().equals("HashMap");
            }
        });

        for (CtTypeReference<?> typeRef : listTypeRefs){
            HashMapToArrayMap(typeRef);
        }
    }

    private void convertConstructor(CtConstructorCall<?> constructorCall){
        CtExpression arg;

        switch (constructorCall.getArguments().size()){
            case 1:
                arg = constructorCall.getArguments().get(0) instanceof CtLiteral ?
                        (CtLiteral)constructorCall.getArguments().get(0) :
                        (CtVariableRead)constructorCall.getArguments().get(0);

                if (!arg.getType().getSimpleName().equals("HashMap")) {
                    constructorCall.removeArgument(arg);
                    constructorCall.insertAfter(getFactory().Code().createCodeSnippetStatement(
                            constructorCall.getParent(CtAssignment.class).getAssigned()+".putAll("+arg+")")
                    );

                }

                break;
            case 2:
                arg = (CtVariableRead)constructorCall.getArguments().get(1);
                constructorCall.removeArgument(arg);
                break;
            default:
                break;
        }
    }

    private void HashMapToArrayMap(CtTypeReference<?> ref){
        List<CtTypeReference<?>> types = ref.getActualTypeArguments();
        ref.replace(getFactory().Code().createCtTypeReference(ArrayMap.class));
        ref.setActualTypeArguments(types);
    }
}