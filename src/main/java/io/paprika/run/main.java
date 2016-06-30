package io.paprika.run;

/**
 * Created by Mehdi on 16-06-30.
 */
public class main {
    public static void main(String[] args) throws Exception {
        spoon.Launcher.main(new String[]{
                "-p", "io.paprika.spoon.MethodLogProcessorIGS",
                "-i", "/home/antonin/Documents/internship/spoon/psychic-octo-palm-tree/app/src"
                //"--source-classpath", "/home/antonin/Documents/internship/spoon/psychic-octo-palm-tree/app/src/main/java/com/example/geoffrey/myapplication"
        });
    }
}
