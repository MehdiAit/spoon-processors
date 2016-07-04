package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Mehdi on 16-05-31.
 */
public class CsvReader {

    public static ArrayList csv(String name){
        Scanner scanner = null;
        ArrayList<String> smell_list = new ArrayList<String>();
        try {
            scanner = new Scanner(new File(name+".csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        scanner.next();

        while (scanner.hasNext())
        {
            String a = scanner.next();
            smell_list.add(a);
        }

        scanner.close();
        return smell_list;
    }

}
