package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class LatNginxStopController extends LatController{
    public LatNginxStopController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    @Override
    protected void execute() throws IOException {

        String instanceId = getInstanceId();
        String runner = System.getProperty("run_user");

        Map<String, String> env = EnvUtil.getEnv(instanceId, getInstallerServerType());
        String engnHome = env.get("ENGN_HOME");
        String installPath = env.get("INSTALL_PATH");
        String setUser = env.get("WAS_USER");



        if(psCheckNginx(engnHome, installPath) == null){
            System.out.println("##### "+ instanceId +" is not running. There is nothing to stop.#######");
            return;
        }

        if(!runner.equals(setUser) && !runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+setUser);
            return;
        }

        System.out.println("Stopping LA:T [NGINX] ... "+ instanceId);



        String[] cmd = {"/bin/sh","-c",engnHome+"/sbin/nginx -c "+installPath+"/conf/nginx.conf -s stop"};

        //System.out.println(cmd[2]);
        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine())!=null){
            System.out.println(s);
        }

        if(psCheckNginx(engnHome, installPath) == null){
            System.out.println("##### "+ instanceId +" successfully shut down ###### ");

        }else{
            System.out.println("##### Fail to stop "+ instanceId +"!!  Check Again!! ###### ");
        }


        return;

    }
}
