package io.paprika.spoon;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Mehdi on 16-07-13.
 */

public class ImportPackageSpooned extends AbstractProcessor<CtType>{
    int count = 0;
    HashMap<String, ArrayList<String>> getImport;

    public ImportPackageSpooned(HashMap<String, ArrayList<String>> toImport){
        getImport = toImport;
    }

    @Override
    public void process(CtType element) {
        if ((element instanceof CtClass) || (element instanceof CtInterface)) {
            String[] filePath = element.getQualifiedName().split("\\$");
            if (filePath.length > 1) {
                return;
            }

            if (getImport.containsKey(filePath[0])) {
                ArrayList<String> classImport = getImport.get(filePath[0]);
                ArrayList<String> classFile = new ArrayList<>();
                String packageName = "";
                count++;

                try {
                    BufferedReader readFile = new BufferedReader(new FileReader(element.getPosition().getFile()));
                    String line = "";
                    while ((line = readFile.readLine()) != null) {
                        classFile.add(line);
                    }

                    ArrayList<String> classFileTmp = new ArrayList<>(classFile);
                    for (String p : classFileTmp) {
                        if (p.matches("package.*")) {
                            packageName = p;
                            classFile.remove(classFile.indexOf(p));
                        }
                    }

                    BufferedWriter writer = new BufferedWriter(new FileWriter(element.getPosition().getFile()));

                    // Write the package name
                    writer.write(packageName);
                    writer.newLine();

                    for (String s : classImport) {
                        writer.write(s);
                        writer.newLine();
                    }

                    // Re-Write the file
                    for (String s : classFile) {
                        writer.write(s);
                        writer.newLine();
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void processingDone() {
        System.out.println("Count from source : "+getImport.size()+" // Count spoon : " + count);
    }
}