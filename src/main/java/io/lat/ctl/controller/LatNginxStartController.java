package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LatNginxStartController extends LatController{
    public LatNginxStartController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    @Override
    protected void execute() throws IOException {


        String instanceId = getInstanceId();

        String runner = System.getProperty("run_user");
        String osName = System.getProperty("os.name");

        Map<String, String> env = EnvUtil.getEnv(instanceId, getInstallerServerType());


        String setUser = env.get("WAS_USER");

        String engnHome = env.get("ENGN_HOME");
        String installPath = env.get("INSTALL_PATH");
        String logHome = env.get("LOG_HOME");

        if(runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ].");
            return;
        }

        if(!runner.equals(setUser)){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+setUser);
            return;
        }


        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep nginx | grep master | grep "+instanceId+" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            if(s!=null){
                System.out.println("#### ERROR. "+instanceId+" is already running. exiting.. ####");
                return;
            }
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep nginx | grep master | grep "+installPath+" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            if(s!=null){
                System.out.println("#### ERROR. "+instanceId+" is already running. exiting.. ####");
                return;
            }
        }
        
        if(logHome != null && !FileUtil.exists(logHome)){
            if(!FileUtil.mkdirs(logHome)){
                System.out.println("cannot create log directory "+logHome);
                System.out.println("Startup failed.");
                return;
            }
        }

        String[] cmd = {"/bin/sh","-c",engnHome+"/sbin/nginx -c "+installPath+"/conf/nginx.conf"};

        //System.out.println(cmd[2]);
        Process p = Runtime.getRuntime().exec(cmd);

        //BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s="";

        while((s=br.readLine())!=null){
            System.out.println(s);
        }

        if(psCheckNginx(engnHome, installPath) == null){
            System.out.println("##### Fail to start "+ instanceId +"!!  Check Again!! ###### ");
        }


    }

}
