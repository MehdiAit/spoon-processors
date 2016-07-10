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
        // Log
        //run.addProcessor(new MethodLogProcessorIGS("Packlist_IGS_filtered"));
        //run.addProcessor(new MethodLogProcessorMIM("Packlist_MIM_filtered"));
        //run.addProcessor(new MethodLogProcessorHMU("Packlist_HMU_filtered"));

        // Corrections
        run.addProcessor(new InvokMethodProcessor());

        // Source project
        //run.addInputResource("/home/antonin/Documents/internship/spoon/Packlist_Version/packlist/app/src/main/java");
        run.addInputResource("/home/antonin/Documents/internship/spoon/Packlist_Version/packlist/app/src/main/groovy");

        //Process now
        run.run();

    }
}