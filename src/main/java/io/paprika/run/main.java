package io.paprika.run;

import spoon.Launcher;
import io.paprika.spoon.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mehdi on 16-06-30.
 */
public class main {
    public static void main(String[] args) throws Exception {
        final String app_name = "Soundwaves";
        final String HMU = "csv/"+app_name+"_HMU_filtered";
        final String IGS = "csv/"+app_name+"_IGS_filtered";
        final String MIM = "csv/"+app_name+"_MIM_filtered";


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
//        run.addProcessor(new MethodLogProcessorIGS(IGS));
//        run.addProcessor(new MethodLogProcessorMIM(MIM));
//        run.addProcessor(new MethodLogProcessorHMU(HMU));

        // Correction
        run.addProcessor(new StaticProcessor(MIM));
        run.addProcessor(new InvokMethodProcessor(IGS));
        run.addProcessor(new HashMapProcessor(HMU));

//        run.addProcessor(new ImportPackages());

        // Source project
        //run.addInputResource("C:\\Users\\Twilibri\\Documents\\GitHub\\spoon-processors\\App_Spooned\\org\\bottiger\\podcast\\activities\\downloadmanager\\DownloadViewModel.java");
        run.addInputResource("C:\\Users\\Twilibri\\Java\\SoundWaves_COUNT.ori\\app\\src\\main\\java\\org\\bottiger\\podcast");

        //Process now
        run.run();

    }
}