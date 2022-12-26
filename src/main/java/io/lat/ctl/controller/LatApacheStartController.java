package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

public class LatApacheStartController extends LatController{
    private static final Logger LOGGER = LoggerFactory.getLogger(LatApacheStartController.class);

    public LatApacheStartController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    protected void execute() throws IOException {

        String instanceId = getInstanceId();

        String runner = System.getProperty("run_user");
        String osName = System.getProperty("os.name");

        Map<String, String> env = EnvUtil.getEnv(instanceId, getInstallerServerType());

        String catalinaPid = env.get("CATALINA_PID");
        String setUser = env.get("USER");
        String dumpHome = env.get("DUMP_HOME");
        String logHome = env.get("LOG_HOME");
        String catalinaOutHome = env.get("CATALINA_OUT_HOME");
        String catalinaHome = env.get("CATALINA_HOME");
        String catalinaBase = env.get("CATALINA_BASE");

        String engnHome = env.get("ENGN_HOME");
        String installPath = env.get("INSTALL_PATH");

        String mpmType = env.get("MPM_TYPE");
        String extModuleDefines = env.get("EXT_MODULE_DEFINES");

        String logDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        if(runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ].");
            return;
        }

        if(!runner.equals(setUser)){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+setUser);
            return;
        }




        String[] logDirs = {"access", "error", "jk"};
        for(String dir:logDirs){
            if(!FileUtil.exists(logHome+"/"+dir)){
                if(!FileUtil.mkdirs(logHome+"/"+dir)){
                    System.out.println("cannot create log directory "+logHome+"/"+dir);
                    System.out.println("Startup failed.");
                    return;
                }
            }
        }

        LOGGER.debug("Log file rotating...");
        File[] files = new File(logHome).listFiles();
        for(File f:files){
            if(f.getName().contains("error_")  && !f.getName().contains(logDate)) {
                f.renameTo(new File(FileUtil.getConcatPath(logHome, "error", f.getName())));
            }else if(f.getName().contains("jk_") && !f.getName().contains(logDate)){
                f.renameTo(new File(FileUtil.getConcatPath(logHome, "jk", f.getName())));
            }else if(f.getName().contains("access_") && !f.getName().contains(logDate)){
                f.renameTo(new File(FileUtil.getConcatPath(logHome, "access", f.getName())));
            }

        }



        LOGGER.debug("library link creating..");

        if(osName.equals("Linux") && !FileUtil.exists(FileUtil.getConcatPath("/lib64","libpcre.so.0")) && !FileUtil.exists(FileUtil.getConcatPath(engnHome,"lib","libpcre.so.0"))){
            Files.createSymbolicLink(Paths.get(engnHome, "lib", "libpcre.so.0"),Paths.get("/lib64","libpcre.so.1"));
        }


        LOGGER.debug("apachectl temporary file creating..");


        BufferedReader reader = null;
        BufferedWriter writer = null;
        ArrayList list = new ArrayList();

        try {
            reader = new BufferedReader(new FileReader(engnHome+"/bin/apachectl"));
            String tmp;
            while ((tmp = reader.readLine()) != null){
                list.add(tmp);
            }
            reader.close();
            //Outil.closeReader(reader);

            list.add(2, ". "+installPath+"/env.sh");

            writer = new BufferedWriter(new FileWriter(engnHome+"/bin/apachectl_temp"));
            for (int i = 0; i < list.size(); i++) {
                writer.write(list.get(i) + "\n");
            }

            writer.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //OUtil.closeReader(reader);
            //OUtil.closeWriter(writer);
        }

        FileUtil.chmod755(engnHome+"/bin/apachectl_temp");
        LOGGER.debug("apachectl temporary file created");


        String[] cmd = {"/bin/sh","-c",engnHome+"/bin/apachectl_temp -f "+installPath+"/conf/httpd.conf -k start -D"+mpmType+" "+extModuleDefines};

        Process p = Runtime.getRuntime().exec(cmd);

        LOGGER.debug("Starting apache...");

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine()) != null){
            System.out.println(s);
            LOGGER.debug(s);

        }

        LOGGER.debug("Deleting temporary file");
        FileUtil.delete(engnHome+"/bin/apachectl_temp");

        LOGGER.debug("Apache started");

    }
}
