package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LatCometStartController extends LatController{
    public LatCometStartController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    @Override
    protected void execute() throws IOException {
        String instanceName = getInstanceName();
        String runner = System.getProperty("run_user");

        Map<String, String> env = getEnv(instanceName);
        String engnHome = env.get("ENGN_HOME");
        String runUser = env.get("RUN_USER");
        String logHome = env.get("LOG_HOME");
        String dumpHome = env.get("DUMP_HOME");
        String logOutput = env.get("LOG_OUTPUT");
        String javaHome = env.get("JAVA_HOME");
        String javaOpts = env.get("JAVA_OPTS");
        String startClass = env.get("START_CLASS");
        String latHome = env.get("LAT_HOME");
        String cometHome = env.get("COMET_HOME");
        String cometPrimaryPort = env.get("ZODIAC_PRIMARY_PORT");

        String classPath = setClasspath(engnHome, "lena-session");

        if(psCheckComet(instanceName, classPath) != null){
            System.out.println("##### ERROR. "+instanceName+" is already running. exiting.. #####");
            return;
        }

        if(runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ].");
            return;
        }

        if(!runner.equals(runUser)){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+runUser);
            return;
        }



        if(!FileUtil.exists(logHome)){
            if(!FileUtil.mkdirs(logHome)){
                System.out.println("cannot create log directory "+logHome);
                System.out.println("Startup failed.");
                return;
            }
        }

        String[] dumpDirs = {"hdump"};
        for(String dir:dumpDirs){
            if(!FileUtil.exists(dumpHome+"/"+dir)){
                if(!FileUtil.mkdirs(dumpHome+"/"+dir)){
                    System.out.println("cannot create dump directory "+dumpHome+"/"+dir);
                    System.out.println("Startup failed.");
                    return;
                }
            }
        }

        if(logOutput.equals("console")){
            printCometServerInfo("start", latHome, engnHome, cometHome, instanceName, cometPrimaryPort, javaHome);

            String[] cmd = {"/bin/sh","-c",FileUtil.getConcatPath(javaHome,"bin","java")+" "+javaOpts+" -cp .:"+classPath+" "+startClass+" &"};
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s="";

            while((s=br.readLine()) != null){
                System.out.println(s);
            }
        }else{
            String[] cmd = {"/bin/sh","-c",FileUtil.getConcatPath(javaHome,"bin","java")+" "+javaOpts+" -cp .:"+classPath+" "+startClass+" >> /dev/null 2>&1 &"};
            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s="";

            while((s=br.readLine()) != null){
                System.out.println(s);
            }

            printCometServerInfo("start", latHome, engnHome, cometHome, instanceName, cometPrimaryPort, javaHome);

        }

    }
}
