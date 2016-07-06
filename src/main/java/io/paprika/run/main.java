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
        //run.addProcessor(new MethodLogProcessorIGS());
        //run.addProcessor(new MethodLogProcessorMIM());
        //run.addProcessor(new MethodLogProcessorHMU());
        //run.addProcessor(new StaticProcessor());
        //run.addProcessor(new InvokMethodProcessor());
        run.addProcessor(new HashMapProcessor());

        // Source project
        //run.addInputResource("C:\\Users\\Twilibri\\Java\\org.bottiger.podcast_292_src\\app\\src\\main\\java\\org\\bottiger\\podcast");
        run.addInputResource("C:\\Users\\Twilibri\\Java\\org.telegram.messenger_7673_src\\TMessagesProj\\src\\main\\java\\org\\telegram\\messenger\\Animation");

        //Process now
        run.run();

    }
}