package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LatCometStopController extends LatController{
    public LatCometStopController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    @Override
    protected void execute() throws IOException {

        String instanceName = getInstanceName();
        String runner = System.getProperty("run_user");

        Map<String, String> env = getEnv(instanceName);
        String engnHome = env.get("ENGN_HOME");
        String runUser = env.get("RUN_USER");
        String javaHome = env.get("JAVA_HOME");
        String latHome = env.get("LAT_HOME");
        String cometHome = env.get("COMET_HOME");
        String cometPrimaryPort = env.get("ZODIAC_PRIMARY_PORT");
        String classPath = setClasspath(engnHome, "lena-session");


        if(!runner.equals(runUser) && !runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+runUser);
            return;
        }

        if(psCheckComet(instanceName, classPath) == null){
            System.out.println("##### "+instanceName+" is not running. exiting.. #####");
            return;
        }

        String pid = psCheckComet(instanceName, classPath).split("\\s+")[1];



        String[] cmd = {"/bin/sh","-c", "rm -f "+ FileUtil.getConcatPath(cometHome,pid+".zodiac")};
        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine()) != null){
            System.out.println(s);
        }

        printCometServerInfo("stop", latHome, engnHome, cometHome, instanceName, cometPrimaryPort, javaHome);
    }
}
