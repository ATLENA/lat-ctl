package io.lat.ctl.controller;

import io.lat.ctl.type.ControllerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class LatTomcatStopController extends LatController{

    public LatTomcatStopController(ControllerCommandType controllerCommandType, InstallerServerType installerServerType, String instanceName){
        super(controllerCommandType, installerServerType, instanceName);
    }

    @Override
    protected void execute() throws IOException {

        String instanceName = getInstanceName();
        String runner = System.getProperty("run_user");
        String osName = System.getProperty("os.name");

        Map<String, String> env = getEnv(instanceName);

        String instName = env.get("INST_NAME");
        String catalinaPid = env.get("CATALINA_PID");
        String setUser = env.get("USER");
        String dumpHome = env.get("DUMP_HOME");
        String logHome = env.get("LOG_HOME");
        String catalinaOut = env.get("CATALINA_OUT");
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


        /*
        if(osName.toLowerCase().contains("hp-ux")){
            String[] cmd = {"/bin/sh","-c","ps -efx | grep java | grep was_cname="+instanceName+" | grep -v grep"};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();
            if(s==null){
                System.out.println("#### ERROR. "+instanceName+" is not running. There is nothing to stop.#######");
                return;
            }
        }else{
            String[] cmd = {"/bin/sh","-c","ps -ef | grep bin/java | grep catalina.base="+catalinaBase+" | grep -v grep"};

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s=br.readLine();

            if(s==null){
                System.out.println("#### ERROR. "+instanceName+" is not running. There is nothing to stop.#######");
                return;
            }
        }

         */


        if(psCheckTomcat(catalinaBase)==null){
            System.out.println("#### ERROR. "+instanceName+" is not running. There is nothing to stop.#######");
            return;
        }






        BufferedReader reader = null;
        BufferedWriter writer = null;
        ArrayList list = new ArrayList();

        try {
            reader = new BufferedReader(new FileReader(catalinaHome+"/bin/shutdown.sh"));
            String tmp;
            while ((tmp = reader.readLine()) != null){
                list.add(tmp);
            }
            reader.close();
            //Outil.closeReader(reader);

            list.add(2, ". "+catalinaBase+"/env.sh");
            list.add(3, "JAVA_OPTS=\" ${JAVA_OPTS} -Xms64m -Xmx64m\"");

            writer = new BufferedWriter(new FileWriter(catalinaHome+"/bin/shutdown_temp.sh"));
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

        FileUtil.chmod755(catalinaHome+"/bin/shutdown_temp.sh");


        String[] cmd = {"/bin/sh","-c",catalinaHome+"/bin/shutdown_temp.sh"};
        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s="";

        while((s=br.readLine()) != null){
            System.out.println(s);
            /*
            if(s.equals("Tomcat started.")){
                break;
            }

             */
        }

        FileUtil.delete(catalinaHome+"/bin/shutdown_temp.sh");



        if(new File(catalinaOut).exists()){
            FileUtil.delete(catalinaOutHome);
        }


        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(nowDate);

        BufferedWriter logWriter = new BufferedWriter(new FileWriter(FileUtil.getConcatPath(logHome, instName+"_"+date+".log"), true));

        logWriter.write("Execute ./stop.sh by [ "+runner+" ]");

        logWriter.close();



        if(psCheckTomcat(catalinaBase) == null){
            System.out.println("##### "+instName+" successfully shut down ###### ");

        }else{
            System.out.println("##### Fail to stop "+instName+"!!  Check Again!! ###### ");
        }




        return;
    }


}
