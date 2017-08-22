package io.paprika.spoon;

import java.util.ArrayList;
import java.util.List;
//import java.util.Set;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

public class ProcessorDBI extends AbstractProcessor<CtClass<?>>{

	//private Set<String> dbiOccurences;

	public ProcessorDBI(String file){
        System.out.println("Processor DBI Start ... ");
		// Get applications information from the CSV - output
        //dbiOccurences = lib.csvToSetString(file);
    }
	
	/**
	 * @author Alexis Merceron
	 * @param class
	 * @return true
	 * */
	@Override
    public boolean isToBeProcessed(CtClass<?> invok) {
        return true;
    }
	
	/**
	 * Process for correct DBI
	 * @author Alexis Merceron
	 * @param class
	 * */
	@Override
	public void process(CtClass<?> invok) {
		for (CtMethod<?> method : invok.getAllMethods()){
			// Detect no empty method
			if (method.getBody() != null && method.getBody().toString().length() > 4){
				
				// Split method for detect HashMap initialization and save variable name
				String[] methodSplit = method.toString().split(";");
				List<String> hashMapInit = new ArrayList<String>();
				for (int i = 0; i < methodSplit.length ; i++)
					if (methodSplit[i].indexOf("new HashMap") != -1)
						if (methodSplit[i].indexOf("=") != -1)
							hashMapInit.add(methodSplit[i].split(" =")[0].split(" ")[methodSplit[i].split(" =")[0].split(" ").length-1]);
						else
							hashMapInit.add(null);
					
				
				// Get all HashMap constructor
				List<CtConstructorCall<?>> listConstrCall = method.getElements(new
		    			TypeFilter<CtConstructorCall<?>>(CtConstructorCall.class) {
		    			@Override
		    			public boolean matches(CtConstructorCall<?> element) {
		    				return element.getType().getSimpleName().equals("HashMap");
		    		}
		    	});
				
				for(int i = 0; i < listConstrCall.size(); i++){
					listConstrCall.get(i).replace(getFactory().Code().createCodeSnippetExpression(listConstrCall.get(i).toString().split(" \\{")[0]));
					
					String constructorCleaned = listConstrCall.get(i).toString().replace(" ", "").replace("\r", "").replace("\n", "");
					if (constructorCleaned.indexOf("{{") != -1)
						for (String element : constructorCleaned.substring(constructorCleaned.toString().indexOf("{{"), constructorCleaned.toString().indexOf("}}")-1)
								.replace("{{", "").replace("}}", "").split(";")){
							listConstrCall.get(i).insertAfter(getFactory().Code().createCodeSnippetStatement(hashMapInit.get(i)+ "." + element + ";"));
							System.out.println(hashMapInit.get(i)+ "." + element + ";");
						}
				}
			}
		}
	}

}
