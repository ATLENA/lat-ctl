/*
 * Copyright 2022 LA:T Development Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.lat.ctl.installer;

import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;
import io.lat.ctl.util.PropertyUtil;
import io.lat.ctl.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Installer that can create LA:T Comet
 * 
 * @author ksseo
 *
 */

@Slf4j
public class LatCometCreateInstaller extends LatInstaller {

	public LatCometCreateInstaller(InstallerCommandType installerCommandType, InstallerServerType installerServerType) {
		super(installerCommandType, installerServerType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Logic that actually creates the server
	 */
	public void execute() throws IOException {
		try {
			HashMap<String, String> commandMap = getServerInfoFromUser();
	
			String instanceId = commandMap.get("INSTANCE_ID");
			String servicePort = getParameterValue(commandMap.get("SERVICE_PORT"), getDefaultValue(getServerType() + ".service-port"));
			String secondaryServerIp = getParameterValue(commandMap.get("SECONDARY_SERVER_IP"), getDefaultValue(getServerType()+".secondary-server-ip"));
			String secondaryServicePort = getParameterValue(commandMap.get("SECONDARY_SERVICE_PORT"), getDefaultValue(getServerType() + ".secondary-service-port"));
			String runUser = getParameterValue(commandMap.get("RUN_USER"), EnvUtil.getRunuser());
			String installRootPath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", getServerType());
			String targetPath = FileUtil.getConcatPath(installRootPath, getTargetDirName(instanceId, servicePort));
			String logHome = getParameterValue(commandMap.get("LOG_HOME"), FileUtil.getConcatPath(targetPath, "logs"));
	
			if(FileUtil.exists(targetPath)){
				//log.error("["+targetPath+"] directory already exists. Remove the directory and try again.");
				throw new LatException("["+targetPath+"] directory already exists. Remove the directory and try again.");
			}
			// validate options
			if (!StringUtil.isNumeric(servicePort)) {
				//log.error("Service Port should be numeric. : "+servicePort);
				throw new LatException("Service Port should be numeric.");
			}
			if (!StringUtil.isNumeric(secondaryServicePort)) {
				//log.error("Service Port should be numeric. : "+secondaryServicePort);
				throw new LatException("Service Port should be numeric.");
			}
	
			// installPath check
			if (FileUtil.exists(targetPath)) {
				//log.error(targetPath + " already exists.");
				throw new LatException(targetPath + " already exists.");
			}
	
			// run user check
			if ("root".equals(runUser) && !EnvUtil.isRootUserAllowed()) {
				//log.error(getServerType() + " can't run as root user.");
				throw new LatException(getServerType() + " can't run as root user.");
			}
	
			FileUtil.copyDirectory(getDepotPath(), targetPath);
			
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "JAVA_HOME", EnvUtil.getUserJavahome());
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LAT_HOME", EnvUtil.getLatHome());
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTANCE_ID", instanceId);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "ENGN_VERSION", getEngineVersion("comet"));
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "COMET_HOME", targetPath);
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "RUN_USER", runUser);
			if (!logHome.equals(FileUtil.getConcatPath(targetPath, "logs"))) {
				FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LOG_HOME", logHome + "/${INSTANCE_ID}");
			}
			PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "conf", "session.conf"), "server.name", instanceId);
			PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "conf", "session.conf"), "primary.port", servicePort);
			PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "conf", "session.conf"), "secondary.host", secondaryServerIp);
			PropertyUtil.setProperty(FileUtil.getConcatPath(targetPath, "conf", "session.conf"), "secondary.port", secondaryServicePort);
	
			// update install-info.xml
			addInstallInfo(instanceId, servicePort, targetPath);
		}catch(Throwable e) {
			log.error(e.getMessage());
		}

	}

	/**
	 * @return Server information to be created
	 */
	public HashMap<String, String> getServerInfoFromUser() {
		HashMap<String, String> commandMap = new HashMap<String, String>();
		Scanner scan = new Scanner(System.in);

		System.out.println("+-------------------------------------------------------------------------------------");
		System.out.println("| 1. INSTANCE_ID means business code of system and its maximum number of letters is 20. ");
		System.out.println("|    (ex :  session-5100)                                                               ");
		System.out.print("|: ");
		commandMap.put("INSTANCE_ID", checkEmpty(scan.nextLine()));
		System.out.println("|");
		System.out.println("| 2. SERVICE_PORT is the port number used by Session Instance.                          ");
		System.out.println("|    (default : 5100)                                                                        ");
		System.out.print("|: ");
		commandMap.put("SERVICE_PORT", scan.nextLine());
		System.out.println("|");
		System.out.println("| 3. SECONDARY_SERVER_IP is the ip number communicate with Secondary Session Instance   ");
		System.out.println("|    (default : 127.0.0.1)                                                                   ");
		System.out.print("|: ");
		commandMap.put("SECONDARY_SERVER_IP", scan.nextLine());
		System.out.println("|");
		System.out.println("| 4. SECONDARY_SERVICE_PORT is the port number used by Secondary Session Instance.      ");
		System.out.println("|    (default : 5200)                                                                        ");
		System.out.print("|: ");
		commandMap.put("SECONDARY_SERVICE_PORT", scan.nextLine());
		System.out.println("|");
		System.out.println("| 5. RUN_USER is user running Session Instance                                          ");
		System.out.println("|    (default : " + EnvUtil.getRunuser()+")");
		System.out.print("|: ");
		commandMap.put("RUN_USER", scan.nextLine());
		System.out.println("|");
		System.out.println("| 6. INSTALL_ROOT_PATH is instance root directory in filesystem.                     ");
		System.out.println(
				"|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", getServerType())+")");
		System.out.print("|: ");
		commandMap.put("INSTALL_ROOT_PATH", scan.nextLine());
		System.out.println("|");
		System.out.println("| 7. LOG_HOME is LA:T Session Instance's log directory in filesystem.                   ");
		System.out.println("|    If you don't want to use default log directory input your custom log home prefix.");
		System.out.println("|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances",
				getServerType(), commandMap.get("INSTANCE_ID"), "logs")+")");
		System.out.print("|: ");
		commandMap.put("LOG_HOME", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");

		return commandMap;
	}

//	public static String getEngineVersion() throws IOException {
//		String[] cmd;
//		if (System.getProperty("os.name").indexOf("Windows") > -1) {
//			cmd = new String[] { "cmd", "/c",
//					"ls -1r --sort=version " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", "zodiac") };
//		} else {
//			cmd = new String[] { "/bin/sh", "-c",
//					"ls -1r --sort=version " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "engines", "zodiac") };
//		}
//
//		Process p = Runtime.getRuntime().exec(cmd);
//		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//		String s = br.readLine();
//
//		if (s == null) {
//			throw new LatException("Tomcat engine is not installed");
//		} else {
//			return s;
//		}
//	}
}
