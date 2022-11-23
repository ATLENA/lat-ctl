package io.lat.ctl.installer;

import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class LatNginxCreateInstaller extends LatInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(LatNginxCreateInstaller.class);

    /**
     * @param installerCommandType command
     * @param installerServerType  serverType
     */
    public LatNginxCreateInstaller(InstallerCommandType installerCommandType, InstallerServerType installerServerType) {
        super(installerCommandType, installerServerType);
    }

    @Override
    protected void execute() throws IOException {

        HashMap<String, String> commandMap = getServerInfoFromUser();

        String instanceId = commandMap.get("INSTANCE_ID");
        String installRootPath = getParameterValue(commandMap.get("INSTALL_ROOT_PATH"), FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", "nginx"));
        String targetPath = FileUtil.getConcatPath(installRootPath, instanceId);

        String servicePort = getParameterValue(commandMap.get("SERVICE_PORT"), getDefaultValue(getServerType() + ".service-port"));
        String protocol = getParameterValue(commandMap.get("PROTOCOL"), "HTTP");
        String documentRootPath = getParameterValue(commandMap.get("DOCUMENT_ROOT_PATH"), FileUtil.getConcatPath(targetPath, "html"));

        String logHome = getParameterValue(commandMap.get("LOG_HOME"), FileUtil.getConcatPath(targetPath, "logs"));

        String errorLog = FileUtil.getConcatPath(logHome,"error.log");
        //String pid =  logHome + "/nginx.pid";
        String pid = FileUtil.getConcatPath(targetPath, instanceId+".pid");
        String runUser = getParameterValue(commandMap.get("RUN_USER"), EnvUtil.getRunuser());

        String proxyPassName = commandMap.get("PROXY_PASS_NAME");
        String upstreamServer = commandMap.get("UPSTREAM_SERVER");


        if(FileUtil.exists(targetPath)){
        	LOGGER.error("["+targetPath+"] directory already exists. Remove the directory and try again.");
            throw new LatException("["+targetPath+"] directory already exists. Remove the directory and try again.");
        }

        FileUtil.copyDirectory(getDepotPath(), targetPath);

        FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LAT_HOME", EnvUtil.getLatHome());
        FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVER_ID", instanceId);
        FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTALL_PATH", targetPath);

        FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "ENGN_VERSION", getEngineVersion("nginx"));

        FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "WAS_USER", runUser);


        //String conf = "user "+runUser+";\n";
        String conf = "error_log "+errorLog+";\n" +
                "pid "+pid+";\n\n" +
                "events {\n" +
                "    worker_connections 1024;"+
                "}";


        if(protocol.equals("TCP") || protocol.equals("UDP")){

            String isUdp = "";
            if(protocol.equals("UDP")){
                isUdp = "udp";
            }

            conf = conf + "stream {\n"+
                    upstreamServer+
                    "    server {\n"+
                    "        listen "+servicePort+" "+isUdp+";\n"+
                    "        proxy_pass "+proxyPassName+";\n"+
                    "    }\n"+
                    "}\n";

        }else {

            conf = conf + "http {\n" ;
            if(proxyPassName!=null){

                conf = conf + upstreamServer;

            }

            conf = conf+"    server {\n" +
                    "        listen "+servicePort+";\n" +
                    "\n"+
                    "        location / {\n" ;

            if(commandMap.get("SERVICE_TYPE").equals("1")) {
                conf = conf + "            root " + documentRootPath + ";\n";
            }else if(commandMap.get("SERVICE_TYPE").equals("2")) {
                conf = conf + "            proxy_pass http://"+proxyPassName+";\n";
            }

            conf = conf +"        }\n"+
                    "    }\n"+
                    "}\n";

        }

        FileUtil.writeStringToFile(FileUtil.getConcatPath(targetPath,"conf","nginx.conf"), conf);

        addInstallInfo(instanceId, servicePort, targetPath);

    }

    private HashMap<String, String> getServerInfoFromUser() {
        HashMap<String, String> commandMap = new HashMap<String, String>();
        Scanner scan = new Scanner(System.in);

        System.out.println("+-------------------------------------------------------------------------------------");
        System.out.println("| 1. INSTANCE_ID means business code of system and its number of letter is from 3 to 5. ");
        System.out.println("|    (ex : lat_was-8080)                                                          ");
        System.out.print("|: ");
        commandMap.put("INSTANCE_ID", checkEmpty(scan.nextLine()));
        System.out.println("|");
        System.out.println("| 2. SERVICE_PORT is the port number used by HTTP Connector.                          ");
        System.out.println("|    (default : 8080)                                                                   ");
        System.out.print("|: ");
        commandMap.put("SERVICE_PORT", scan.nextLine());
        System.out.println("|");
        System.out.println("| 3. RUN_USER is user running LA:T Server                                            ");
        System.out.println("|    (default : "+ EnvUtil.getRunuser()+")");
        System.out.print("|: ");
        commandMap.put("RUN_USER", scan.nextLine());
        System.out.println("|");
        System.out.println("| 4. INSTALL_ROOT_PATH is server root directory in filesystem.                        ");
        System.out.println("|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", "nginx")+")");
        System.out.print("|: ");
        commandMap.put("INSTALL_ROOT_PATH", scan.nextLine());
        System.out.println("|");
        System.out.println("| 5. LOG_HOME is NGINX Server's log directory in filesystem.                         ");
        System.out.println("|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", "nginx", commandMap.get("INSTANCE_ID"), "logs")+")");
        System.out.print("|: ");
        commandMap.put("LOG_HOME", scan.nextLine());
        System.out.println("|");
        System.out.println("| 6. PROTOCOL HTTP or TCP or UDP");
        System.out.println("|    (default : HTTP)");
        System.out.print("|: ");
        commandMap.put("PROTOCOL", scan.nextLine());

        switch (commandMap.get("PROTOCOL")) {
            case "TCP":
            case "UDP":
                System.out.println("|");
                System.out.println("| 7. PROXY_PASS_NAME");
                System.out.println("|    (default : backend)");
                System.out.println("|: ");
                commandMap.put("PROXY_PASS_NAME", scan.nextLine());
                if(commandMap.get("PROXY_PASS_NAME")!=null) {
                    commandMap.put("UPSTREAM_SERVER", getProxyPass(commandMap.get("PROXY_PASS_NAME")));
                }
                break;

            case "HTTP":
            default:
                System.out.println("|");
                System.out.println("| 7. Static contents service or Proxy pass?");
                System.out.println("|   - Static contents service : Press 1 ");
                System.out.println("|   - Proxy Pass : Press 2");
                System.out.print("|: ");
                commandMap.put("SERVICE_TYPE", checkEmpty(scan.nextLine()));

                if(commandMap.get("SERVICE_TYPE").equals("1")) {
                    System.out.println("|");
                    System.out.println("| 8. DOCUMENT_ROOT_PATH is NGINX Server's contents directory in filesystem.          ");
                    System.out.println("|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", "nginx", commandMap.get("INSTANCE_ID"), "html")+")");
                    System.out.print("|: ");
                    commandMap.put("DOCUMENT_ROOT_PATH", scan.nextLine());
                }else if(commandMap.get("SERVICE_TYPE").equals("2")) {
                    System.out.println("|");
                    System.out.println("| 8. PROXY_PASS_NAME");
                    System.out.println("|    (ex : backend)");
                    System.out.print("|: ");
                    commandMap.put("PROXY_PASS_NAME", scan.nextLine());

                    commandMap.put("UPSTREAM_SERVER", getProxyPass(commandMap.get("PROXY_PASS_NAME")));

                }else{

                    System.out.println("Invalid input");
                    throw new LatException("Invalid input");
                }
                break;

        }

        System.out.println("+-------------------------------------------------------------------------------------");

        return commandMap;
    }

    private String getProxyPass(String proxyPassName){

        int count=1;
        Scanner scan = new Scanner(System.in);

        System.out.println("|  -"+ count++ +". UPSTREAM_SERVER");
        System.out.println(" IP:PORT if null, end ex) 127.0.0.1:3306");
        System.out.print("|: ");
        String backend = scan.nextLine();

        if(backend==null){
            return null;
        }

        String upstream = "upstream "+proxyPassName+" {\n"
                + "    server "+backend+"\n";

        while(backend != null){
            System.out.println("|  -"+ count++ +". UPSTREAM_SERVER");
            System.out.println(" IP:PORT if null, end ex) 127.0.0.1:3306");
            System.out.print("|: ");
            backend = scan.nextLine();
            if(backend != null) {
                upstream = upstream + "    server " + backend + ";\n";
            }
        }

        upstream = upstream + "}\n";

        return upstream;
    }

}
