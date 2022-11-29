package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class LatApacheStopController extends LatController{

    public LatApacheStopController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    @Override
    protected void execute() throws IOException {



        String instanceId = getInstanceId();

        String runner = System.getProperty("run_user");


        Map<String, String> env = EnvUtil.getEnv(instanceId, getInstallerServerType());

        String setUser = env.get("USER");
        String engnHome = env.get("ENGN_HOME");
        String installPath = env.get("INSTALL_PATH");
        String shutdownGraceful = env.get("SHUTDOWN_GRACEFUL");

        String mpmType = env.get("MPM_TYPE");
        String extModuleDefines = env.get("EXT_MODULE_DEFINES");


        if(psCheckApache(engnHome, installPath)==null){
            System.out.println("##### "+instanceId+" is not running. There is nothing to stop.#######");
            return;
        }

        String command = "stop";

        if(shutdownGraceful.equals("true")){
            command = "graceful-stop";
        }


        if(!runner.equals(setUser) && !runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+setUser);
            return;
        }



        System.out.println("Stopping LA:T [Apache] ... "+instanceId);



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


        String[] cmd = {"/bin/sh","-c",engnHome+"/bin/apachectl_temp -f "+installPath+"/conf/httpd.conf -k "+command+" -D"+mpmType+" "+extModuleDefines};

        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine()) != null){
            System.out.println(s);

        }

        FileUtil.delete(engnHome+"/bin/apachectl_temp");


    }
}
