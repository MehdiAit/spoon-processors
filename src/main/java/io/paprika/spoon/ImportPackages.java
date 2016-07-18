package io.paprika.spoon;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import spoon.Launcher;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;


/**
 * Created by Mehdi on 16-07-13.
 */
public class ImportPackages extends AbstractProcessor<CtType> {
    int count = 0;
    HashMap<String, ArrayList<String>> appImport = new HashMap<>();

    @Override
    public void process(CtType element) {
        if ((element instanceof CtClass) || (element instanceof CtInterface)) {
            String[] filePath = element.getQualifiedName().split("\\$");
            ArrayList<String> importList = new ArrayList<>();

            if (filePath.length > 1) {
                return;
            }

            try {
                BufferedReader readFile = new BufferedReader(new FileReader(element.getPosition().getFile()));
                try {

                    String line = "";
                    while ((line = readFile.readLine()) != null) {
                        if (line.matches("import.*")) {
                            importList.add(line);
                        }
                    }
                    appImport.put(filePath[0], importList);
                    count++;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processingDone() {
        System.out.println(count);
        Launcher runImport = new Launcher();

        runImport.getEnvironment().setNoClasspath(true);
        runImport.getEnvironment().setShouldCompile(false);
        runImport.getEnvironment().setAutoImports(true);

        runImport.addProcessor(new ImportPackageSpooned(appImport));
        runImport.addInputResource("C:\\Users\\Twilibri\\Documents\\GitHub\\spoon-processors\\App_Spooned\\org\\bottiger\\podcast");

        runImport.run();
        System.out.println("End Import processor");

    }
}
