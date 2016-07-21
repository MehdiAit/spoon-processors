package io.paprika.run;

import spoon.Launcher;
import io.paprika.spoon.*;

import java.util.HashSet;

/**
 * Created by Mehdi on 16-06-30.
 */
public class main {
    public static void main(String[] args) throws Exception {
        final String HMU = "recep_HMU_filtered";
        final String IGS = "recep_IGS_filtered";
        final String MIM = "recep_MIM_filtered";

        Launcher getClass = new Launcher();
        Launcher run = new Launcher();


        // getClass conf
        getClass.getEnvironment().setNoClasspath(true);
        //getClass.getEnvironment().setSourceClasspath(sourceClassPatch);

        getClass.getEnvironment().setShouldCompile(false);
        getClass.getEnvironment().setAutoImports(true);
        getClass.setSourceOutputDirectory("List_class");

        getClass.addProcessor(new ListClass(0, "recep"));

        getClass.addInputResource("The source project");

        getClass.run();
        HashSet<String> classPath = new HashSet<>(ListClass.listPath());

        //
        run.getEnvironment().setNoClasspath(true);
        //run.getEnvironment().setSourceClasspath(sourceClassPatch);

        run.getEnvironment().setShouldCompile(false);
        run.getEnvironment().setAutoImports(false);


        /************* Add processor **************/

        // Log
        //run.addProcessor(new MethodLogProcessorIGS(IGS));
        //run.addProcessor(new MethodLogProcessorMIM(MIM));
        //run.addProcessor(new MethodLogProcessorHMU(HMU));

        // Correction
        //run.addProcessor(new StaticProcessor(MIM));
        //run.addProcessor(new InvokMethodProcessor(IGS));
        //run.addProcessor(new HashMapProcessor(HMU));

        // Import processor
        run.addProcessor(new ImportPackages());

        // Source project
        for (String e : classPath)
        {
            run.addInputResource(e);
        }

        //Process now
        run.run();
    }
}