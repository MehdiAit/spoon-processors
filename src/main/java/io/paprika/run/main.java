package io.paprika.run;

import spoon.Launcher;
import io.paprika.spoon.*;

/**
 * Created by Mehdi on 16-06-30.
 */
public class main {
    public static void main(String[] args) throws Exception {
        Launcher run = new Launcher();
        //
        //If we got all sources compiled, we can remove this options.
        run.getEnvironment().setNoClasspath(true);

        //
        run.getEnvironment().setShouldCompile(false);
        //
        run.getEnvironment().setAutoImports(true);

        // Add processor
        run.addProcessor(new MethodLogProcessorIGS());
        run.addProcessor(new MethodLogProcessorMIM());

        // Source project
        run.addInputResource("/home/antonin/Documents/internship/spoon/SoundWaves/app/src/main/java/org/bottiger/podcast/views/dialogs");

        //Process now
        run.run();

    }
}