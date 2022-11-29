package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class LatController implements Controller {

	private ControllerCommandType controllerCommandType;
	private InstallerServerType installerServerType;
    private String instanceId;

    public LatController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceId){
        this.controllerCommandType = controllerCommandType;
        this.setInstallerServerType(installerServerType);
        this.instanceId = instanceId;
    }

    protected String getInstanceId(){
        return instanceId;
    }

    public void execute(String[] args) throws IOException {
        execute();
    }
    protected abstract void execute() throws IOException;



    protected String psCheckTomcat(String catalinaBase) throws IOException {

        String osName = System.getProperty("os.name");

        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep java | grep was_cname="+instanceId+" | grep -v grep"};
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

    protected String psCheckComet(String instanceId, String classPath) throws IOException {

        String osName = System.getProperty("os.name");

        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep zodiac.name="+instanceId+" | grep \""+classPath+"\" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            return s;
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep zodiac.name="+instanceId+" | grep \""+classPath+"\" | grep -v grep"};

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

	protected InstallerServerType getInstallerServerType() {
		return installerServerType;
	}

	protected void setInstallerServerType(InstallerServerType installerServerType) {
		this.installerServerType = installerServerType;
	}
}
