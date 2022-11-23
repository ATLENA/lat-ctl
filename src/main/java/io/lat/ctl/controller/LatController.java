package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public abstract class LatController implements Controller {

    private ControllerCommandType controllerCommandType;
    private InstallerServerType installerServerType;
    private String instanceName;

    public LatController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName){
        this.controllerCommandType = controllerCommandType;
        this.installerServerType = installerServerType;
        this.instanceName = instanceName;
    }

    protected String getInstanceName(){
        return instanceName;
    }

    public void execute(String[] args) throws IOException {
        execute();
    }
    protected abstract void execute() throws IOException;

    protected Map<String, String> getEnv(String instanceName) throws IOException {

        String[] cmd = new String[]{EnvUtil.getLatHome()+"/instances/"+ installerServerType.toString().toLowerCase()+"/"+instanceName+"/bin/print-env.sh"};

        Map<String, String> env = new HashMap<String, String>();

        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine())!=null){
            //System.out.println(s);
            int i = s.indexOf('=');
            if(i<=0) continue;
            String key = s.substring(0, i);
            String value = s.substring(i+1);
            env.put(key, value);
            //System.out.println(key+" = "+value);
        }

        return env;

    }

    protected String psCheckTomcat(String catalinaBase) throws IOException {

        String osName = System.getProperty("os.name");

        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep java | grep was_cname="+instanceName+" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            return s;
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep bin/java | grep catalina.base="+catalinaBase+" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            return s;
        }
    }

    protected String psCheckApache(String engnHome, String installPath) throws IOException {

        String osName = System.getProperty("os.name");

        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep "+ FileUtil.getConcatPath(engnHome,"bin","httpd") +" | grep "+FileUtil.getConcatPath(installPath, "conf", "httpd.conf")+" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            return s;
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep "+ FileUtil.getConcatPath(engnHome,"bin","httpd") +" | grep "+FileUtil.getConcatPath(installPath, "conf", "httpd.conf")+" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            return s;
        }
    }


    protected String psCheckNginx(String engnHome, String installPath) throws IOException {

        String osName = System.getProperty("os.name");

        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep "+ FileUtil.getConcatPath(engnHome,"sbin","nginx") +" | grep master | grep "+FileUtil.getConcatPath(installPath, "conf", "nginx.conf")+" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            return s;
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep "+ FileUtil.getConcatPath(engnHome,"sbin","nginx") +" | grep master | grep "+FileUtil.getConcatPath(installPath, "conf", "nginx.conf")+" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            return s;
        }
    }

    protected String psCheckComet(String instanceName, String classPath) throws IOException {

        String osName = System.getProperty("os.name");

        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep zodiac.name="+instanceName+" | grep \""+classPath+"\" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            return s;
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep zodiac.name="+instanceName+" | grep \""+classPath+"\" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            return s;
        }
    }

    protected String setClasspath(String engnHome, String library){
        String path = FileUtil.getConcatPath(engnHome, "server-lib");
        File[] files = new File(path).listFiles();
        String classpath="";
        for(File f:files){
            if(f.getName().contains(library) && f.getName().endsWith(".jar")){
                if(classpath != null){
                    classpath = classpath + ":";
                }
                classpath = classpath+f.getPath();
            }
        }
        return classpath;
    }

    protected void printCometServerInfo(String status, String latHome, String engnHome, String cometHome, String instanceId, String cometPrimaryPort, String javaHome){
        System.out.println("--------------------------------");
        if(status.equals("start")){
            System.out.println("     Start Session Server       ");
        }else if(status.equals("starting")){
            System.out.println("     Starting Session Server       ");
        }else if(status.equals("stop")){
            System.out.println("     Stop Session Server       ");
        }
        System.out.println("--------------------------------");
        System.out.println("Using LAT_HOME    : "+latHome);
        System.out.println("Using ENGN_HOME   : "+engnHome);
        System.out.println("Using SERVER_HOME : "+cometHome);
        System.out.println("Using INSTANCE_ID : "+instanceId);
        System.out.println("Using SERVER_PORT : "+cometPrimaryPort);
        System.out.println("Using JAVA_HOME   : "+javaHome);

        if(status.equals("start")){
            System.out.println("Session Server Started..");
        }else if(status.equals("stop")){
            System.out.println("Session Server Stopped..");
        }
    }
}
