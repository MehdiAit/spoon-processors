package io.paprika.spoon;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import utils.CsvReader;
import java.util.HashSet;

/**
 * Created by antonin on 16-07-18.
 */

public class ListClass extends AbstractProcessor<CtClass> {

    private HashSet<String> allSmell;
    private HashSet<String> igsList;
    private HashSet<String> mimList;
    private HashSet<String> hmuList;

    private static HashSet<String> sourcePath;

    public static HashSet<String> listPath(){return sourcePath;}

    public ListClass(int loead, String appName)
    {
        allSmell = new HashSet<>();
        sourcePath = new HashSet<>();

        switch (loead)
        {
            case 1 :
                igsList = fillSourcePath(appName,"IGS",false);
                allSmell.addAll(igsList);
                break;
            case 2 :
                mimList = fillSourcePath(appName,"MIM",false);
                allSmell.addAll(mimList);
                break;
            case 3 :
                hmuList = fillSourcePath(appName,"HMU",false);
                allSmell.addAll(hmuList);
                break;
            default :
                allSmell.addAll(fillSourcePath(appName,"",true));
                break;

        }
    }

    private HashSet<String> fillSourcePath(String appName, String smellName, boolean isAll)
    {
        HashSet<String> tmp;

        if(!isAll){
            tmp = new HashSet<>(CsvReader.formatCsvHahs(appName+"_"+smellName+"_filtered"));
            return tmp;
        }else
        {
            tmp = new HashSet<>(CsvReader.formatCsvHahs(appName+"_IGS_filtered"));
            tmp.addAll(CsvReader.formatCsvHahs(appName+"_MIM_filtered"));
            tmp.addAll(CsvReader.formatCsvHahs(appName+"_HMU_filtered"));
            return tmp;
        }
    }

    @Override
    public void process(CtClass element) {
        String classQlName = element.getQualifiedName();
        for (String e : allSmell) {
            if (e.equals(classQlName)) {
                String absPath = element.getPosition().getFile().getAbsolutePath();
                sourcePath.add(absPath);
            }
        }
    }

    @Override
    public void processingDone() {
        System.out.println("The number of class to correct is : " + sourcePath.size());
        for (String e:sourcePath
             ) {
            System.out.println(e);
        }
    }
}
