package io.paprika.run;

import spoon.Launcher;
import io.paprika.spoon.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mehdi on 16-06-30.
 */
public class main {
    public static void main(String[] args) throws Exception {
        Map<String,String> csvFiles = new HashMap<>();
        csvFiles.put("HMU","osmand_HMU_filtered");
        csvFiles.put("MIM","osmand_MIM_filtered");
        csvFiles.put("IGS","osmand_IGS_filtered");


        Launcher run = new Launcher();
        //
        //If we got all sources compiled, we can remove this options.
        run.getEnvironment().setNoClasspath(true);

        //
        run.getEnvironment().setShouldCompile(false);
        //
        run.getEnvironment().setAutoImports(true);

        // Add processor
        //run.addProcessor(new MethodLogProcessorIGS(csvFiles.get("IGS")));
        //run.addProcessor(new MethodLogProcessorMIM(csvFiles.get("MIM")));
        run.addProcessor(new MethodLogProcessorHMU(csvFiles.get("HMU")));
        //run.addProcessor(new StaticProcessor(csvFiles.get("MIM")));
        //run.addProcessor(new InvokMethodProcessor(csvFiles.get("IGS")));
        run.addProcessor(new HashMapProcessor(csvFiles.get("HMU")));

        // Source project
        run.addInputResource("C:\\Users\\Twilibri\\Java\\net.osmand.plus_235_src\\android\\OsmAnd-java\\src\\net\\osmand");
        //run.addInputResource("C:\\Users\\Twilibri\\Java\\org.telegram.messenger_7673_src\\TMessagesProj\\src\\main\\java\\org\\telegram\\messenger\\Animation");

        //Process now
        run.run();

    }
}