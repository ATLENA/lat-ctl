package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.FileUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

public class LatTomcatStartController extends LatController{

    public LatTomcatStartController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName) {
        super(controllerCommandType, installerServerType, instanceName);
    }

    protected void execute() throws IOException {

        String instanceName = getInstanceName();
        String runner = System.getProperty("run_user");
        String osName = System.getProperty("os.name");

        Map<String, String> env = getEnv(instanceName);

        String catalinaPid = env.get("CATALINA_PID");
        String setUser = env.get("USER");
        String dumpHome = env.get("DUMP_HOME");
        String logHome = env.get("LOG_HOME");

        String catalinaOutHome = env.get("CATALINA_OUT_HOME");
        String catalinaHome = env.get("CATALINA_HOME");
        String catalinaBase = env.get("CATALINA_BASE");

        String logDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        if(runner.equals("root")){
            System.out.println("Deny Access : [ "+runner+" ].");
            return;
        }

        if(!runner.equals(setUser)){
            System.out.println("Deny Access : [ "+runner+" ]. Not "+setUser);
            return;
        }

        //System.out.println(osName);
        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep java | grep was_cname="+instanceName+" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            if(s!=null){
                System.out.println("#### ERROR. "+instanceName+" is already running. exiting.. ####");
                return;
            }
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep bin/java | grep catalina.base="+catalinaBase+" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            if(s!=null){
                System.out.println("#### ERROR. "+instanceName+" is already running. exiting.. ####");
                return;
            }
        }

        if(FileUtil.exists(catalinaPid)){
            System.out.println("Existing PID file found during start.");
            System.out.println("Removing/clearing stale PID file.");
            FileUtil.delete(catalinaPid);

        }

        String[] logDirs = {"access", "gclog", "nohup"};
        for(String dir:logDirs){
            if(!FileUtil.exists(logHome+"/"+dir)){
                if(!FileUtil.mkdirs(logHome+"/"+dir)){
                    System.out.println("cannot create log directory "+logHome+"/"+dir);
                    System.out.println("Startup failed.");
                    return;
                }
            }
        }

        String[] dumpDirs = {"hdump", "hdump/oom", "sdump", "tdump"};
        for(String dir:dumpDirs){
            if(!FileUtil.exists(dumpHome+"/"+dir)){
                if(!FileUtil.mkdirs(dumpHome+"/"+dir)){
                    System.out.println("cannot create dump directory "+dumpHome+"/"+dir);
                    System.out.println("Startup failed.");
                    return;
                }
            }
        }

        if(!new File(catalinaOutHome).isDirectory()){
            if(!FileUtil.mkdirs(catalinaOutHome)){
                System.out.println("cannot catalina out pipe directory "+catalinaOutHome);
                System.out.println("Startup failed.");
                return;
            }
        }else if(catalinaOutHome!=null && instanceName != null){
            FileUtil.deleteFilesByWildcard(catalinaOutHome,instanceName+"_*");
        }


        File[] files = new File(logHome).listFiles();
        for(File f:files){
            if(f.getName().contains("gc_") && f.getName().contains(".log")) {
                f.renameTo(new File(FileUtil.getConcatPath(logHome, "gclog", f.getName())));
            }else if(f.getName().contains(instanceName) && !f.getName().contains(logDate)){
                f.renameTo(new File(FileUtil.getConcatPath(logHome, "nohup", f.getName())));
            }else if(f.getName().contains("access_") && !f.getName().contains(logDate)){
                f.renameTo(new File(FileUtil.getConcatPath(logHome, "access", f.getName())));
            }

        }




        BufferedReader reader = null;
        BufferedWriter writer = null;
        ArrayList list = new ArrayList();

        try {
            reader = new BufferedReader(new FileReader(catalinaHome+"/bin/startup.sh"));
            String tmp;
            while ((tmp = reader.readLine()) != null){
                list.add(tmp);
            }
            reader.close();
            //Outil.closeReader(reader);

            list.add(2, ". "+catalinaBase+"/env.sh");

            writer = new BufferedWriter(new FileWriter(catalinaHome+"/bin/startup_temp.sh"));
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

        FileUtil.chmod755(catalinaHome+"/bin/startup_temp.sh");


        String[] cmd = {"/bin/sh","-c",catalinaHome+"/bin/startup_temp.sh"};
        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine()) != null){
            System.out.println(s);
            if(s.equals("Tomcat started.")){
                break;
            }
        }

        FileUtil.delete(catalinaHome+"/bin/startup_temp.sh");

        return;
    }
}
