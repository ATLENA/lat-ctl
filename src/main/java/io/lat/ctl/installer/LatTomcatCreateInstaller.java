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
import io.lat.ctl.util.CipherUtil;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;
import io.lat.ctl.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Installer that can create LA:T TOMCAT WAS.
 * @author Erick Yu
 *
 */

@Slf4j
public class LatTomcatCreateInstaller extends LatInstaller {

	public LatTomcatCreateInstaller(InstallerCommandType installerCommandType, InstallerServerType installerServerType) {
		super(installerCommandType, installerServerType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Logic that actually creates the server
	 */
	public void execute() throws IOException {
		HashMap<String, String> commandMap = getServerInfoFromUser();
		String instanceId = commandMap.get("INSTANCE_ID");
		String servicePort = getParameterValue(commandMap.get("SERVICE_PORT"), getDefaultValue(getServerType() + ".service-port"));
		String runUser = getParameterValue(commandMap.get("RUN_USER"), EnvUtil.getRunuser());
		String installRootPath = FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", getServerType());
		String ajpAddress = getParameterValue(commandMap.get("AJP_ADDRESS"), getDefaultValue(getServerType() + ".ajp-address"));
		String ajpSecret = CipherUtil.md5(instanceId);
		String targetPath = FileUtil.getConcatPath(installRootPath, instanceId);
		String logHome = getParameterValue(commandMap.get("LOG_HOME"), FileUtil.getConcatPath(targetPath, "logs"));
		String jvmRoute = getParameterValue(commandMap.get("JVM_ROUTE"), getDefaultValue(getServerType() + ".jvm-route"));



		if(FileUtil.exists(targetPath)){
			log.error("["+targetPath+"] directory already exists. Remove the directory and try again.");
			throw new LatException("["+targetPath+"] directory already exists. Remove the directory and try again.");
		}

		//FileUtil.copyDirectory(FileUtil.getConcatPath(getDepotPath(), "module"), targetPath);
		FileUtil.copyDirectory(getDepotPath(), targetPath);

		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "JAVA_HOME", EnvUtil.getUserJavahome());
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LAT_HOME", EnvUtil.getLatHome());
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTANCE_ID", instanceId);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "SERVICE_PORT", servicePort);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "INSTALL_PATH", targetPath);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "WAS_USER", runUser);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "JVM_ROUTE", jvmRoute);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "ENGN_VERSION", getEngineVersion("tomcat"));

		if (!logHome.equals(FileUtil.getConcatPath(targetPath, "logs"))) {
			FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LOG_HOME", logHome + "/${INSTANCE_ID}");
		}

		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "AJP_ADDRESS", ajpAddress);
		FileUtil.setShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "AJP_SECRET", ajpSecret);

		// Change directory authorization to 700 and files under the directory to 600
		FileUtil.chmodF600OD700(new File(FileUtil.getConcatPath(targetPath, "conf")));

		setSampleApplicationDocBase(targetPath);

		// update install-info.xml
		addInstallInfo(instanceId, servicePort, targetPath);
	}

	/**
	 * @param targetPath
	 */
	public void setSampleApplicationDocBase(String targetPath) {
		String rootXmlPath = FileUtil.getConcatPath(targetPath, "conf", "Catalina", "localhost", "ROOT.xml");

		if (FileUtil.exists(rootXmlPath)) {
			Document document = XmlUtil.createDocument(rootXmlPath);
			XPath xpath = XPathFactory.newInstance().newXPath();
			try {
				Element element = (Element) XmlUtil.xpathEvaluate("//Context", document, XPathConstants.NODE, xpath);

				String docBase = element.getAttribute("docBase");

				String defaultDocBase = FileUtil.getConcatPath(EnvUtil.getLatManagementHome(), "depot", "lat-application", "ROOT");
				if (!defaultDocBase.equals(docBase)) {
					element.setAttribute("docBase", defaultDocBase);
					XmlUtil.writeXmlDocument(document, rootXmlPath);
				}
			}
			catch (XPathExpressionException e) {
				log.debug("fail in setting sample application docbase");
			}
		}
	}

	/**
	 * @return Server information to be created
	 */
	public HashMap<String, String> getServerInfoFromUser() {
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
		System.out.println("|    (default : "+EnvUtil.getRunuser()+")");
		System.out.print("|: ");
		commandMap.put("RUN_USER", scan.nextLine());
		System.out.println("|");
		System.out.println("| 4. INSTALL_ROOT_PATH is server root directory in filesystem.                        ");
		System.out.println("|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", "tomcat")+")");
		System.out.print("|: ");
		commandMap.put("INSTALL_ROOT_PATH", scan.nextLine());
		System.out.println("|");
		System.out.println("| 5. AJP_ADDRESS is IP addresss used for listening on the specified port.             ");
		System.out.println("|    (default : 127.0.0.1)                                                              ");
		System.out.print("|: ");
		commandMap.put("AJP_ADDRESS", scan.nextLine());
		System.out.println("|");
		System.out.println("| 6. LOG_HOME is LA:T Server's log directory in filesystem.                          ");
		System.out.println("|    If you don't want to use default log directory input your custom log home prefix.");
		System.out.println("|    (default : " + FileUtil.getConcatPath(EnvUtil.getLatHome(), "instances", "tomcat", commandMap.get("INSTANCE_ID"), "logs")+")");
		System.out.print("|: ");
		commandMap.put("LOG_HOME", scan.nextLine());
		System.out.println("|");
		System.out.println("| 7. JVM_ROUTE is the name of a balanced worker for web-server.                       ");
		System.out.println("|    (default : host1_8180)                                                             ");
		System.out.print("|: ");
		commandMap.put("JVM_ROUTE", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");

		return commandMap;
	}

//	public static String getEngineVersion() throws IOException {
//		String[] cmd;
//		if(System.getProperty("os.name").indexOf("Windows") > -1){
//			cmd=new String[]{"cmd","/c","ls -1r --sort=version "+FileUtil.getConcatPath(EnvUtil.getLatHome(),"engines", "tomcat")};
//		}else{
//			cmd=new String[]{"/bin/sh","-c","ls -1r --sort=version "+FileUtil.getConcatPath(EnvUtil.getLatHome(),"engines","tomcat")};
//		}
//
//		Process p = Runtime.getRuntime().exec(cmd);
//		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//		String s=br.readLine();
//
//		if(s==null){
//			throw new LatException("Tomcat engine is not installed");
//		}else{
//			return s;
//		}
//	}

}
