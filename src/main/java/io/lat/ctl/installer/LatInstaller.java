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

import io.lat.ctl.common.vo.Server;
import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Abstract class for server create installer
 * @author Erick Yu
 *
 */

@Slf4j
public abstract class LatInstaller implements Installer {
	private InstallerCommandType installerCommandType;
	private InstallerServerType installerServerType;

	protected InstallerServerType getInstallerServerType() {
		return installerServerType;
	}

	protected void setInstallerServerType(InstallerServerType installerServerType) {
		this.installerServerType = installerServerType;
	}

	private String depotPath;

	private Map<String, String> resultMap;
	private Map<String, String> defaultValueMap;

	/**
	 * @param installerCommandType command
	 * @param installerServerType serverType
	 */
	public LatInstaller(InstallerCommandType installerCommandType, InstallerServerType installerServerType) {
		this.installerCommandType = installerCommandType;
		this.installerServerType = installerServerType;
	}

	/**
	 * @param serverId
	 * @param servicePort
	 * @param path install path
	 */
	protected void addInstallInfo(String serverId, String servicePort, String path) {
		addInstallInfo(serverId, servicePort, path, "");
	}

	/**
	 * @param serverId
	 * @param servicePort
	 * @param path install path
	 * @param hotfix version
	 */
	protected void addInstallInfo(String serverId, String servicePort, String path, String hotfix) {
		Server server = new Server();
		server.setId(serverId);
		server.setPort(servicePort);
		server.setPath(path);
		server.setType(getServerType());
		InstallInfoUtil.addInstallInfo(server);
	}
	
	/**
	 * install-info.xml파일에서 서버 설치정보를 삭제한다. 
	 * @param serverId 서버ID
	 */
	protected void removeInstallInfo(String serverId, InstallerServerType serverType) {
		InstallInfoUtil.removeInstallInfo(serverId, serverType);
	}
	
	/**
	 * @param serverId
	 * @param servicePort
	 * @param path install path
	 * @param version
	 * @param hotfix version
	 */
	protected void addInstallInfo(String serverId, String servicePort, String path, String version, String hotfix){
		Server server = new Server();
		server.setId(serverId);
		server.setPort(servicePort);
		server.setPath(path);
		server.setType(getServerType());
		server.setVersion(version);
		server.setHotfix(hotfix);
		InstallInfoUtil.addInstallInfo(server);
	}

	/**
	 * @return serverType
	 */
	protected String getServerType() {
		return installerServerType.getServerType();
	}
	
	/**
	 * Return install path.
	 * @param serverId Server ID
	 * @param servicePort Service Port
	 * @return Install Directory Name
	 */
	public String getTargetDirName(String serverId, String servicePort){
		//return serverId + "_" + servicePort;
		return serverId;
	}

	/**
	 * @return depot path
	 */
	public String getDepotPath() {
		return this.depotPath;
	}

	/**
	 * execute server creation logic
	 */
	public void execute(String[] args) throws IOException {
		// TODO Auto-generated method stub
		load(args);
		execute();
	}

	/**
	 * @param args arguments
	 */
	private void load(String args[]) throws IOException {
		this.depotPath = ReleaseInfoUtil.getDepotPath(getServerType());

		resultMap = new LinkedHashMap<String, String>();
		defaultValueMap = getDefaultValueMap();
		resultMap.put("LAT_HOME", EnvUtil.getLatHome());
		resultMap.put("JAVA_HOME", EnvUtil.getUserJavahome());
	}

	/**
	 * @param key
	 * @return default value already defined
	 */
	protected String getDefaultValue(String key) {
		return defaultValueMap.get(key);
	}

	/**
	 * @return map that is having default value
	 */
	private Map<String, String> getDefaultValueMap() {
		Map<String, String> map = new HashMap<String, String>();

		// lena-was default value
		map.put("tomcat.service-port", InstallConfigUtil.getProperty("tomcat.service-port.default", "8080"));
		map.put("tomcat.run-user", InstallConfigUtil.getProperty("tomcat.run-user.default", "lat"));
		map.put("tomcat.ajp-address", InstallConfigUtil.getProperty("tomcat.ajp-address.default", "127.0.0.1"));
		map.put("tomcat.jvm-route", InstallConfigUtil.getProperty("tomcat.jvm-route.default", "host1_8180"));
		map.put("tomcat.template.dirname", InstallConfigUtil.getProperty("tomcat.template.dirname", "base"));

		// lena-web default value
		map.put("apache.service-port", InstallConfigUtil.getProperty("apache.service-port.default", "80"));
		map.put("apache.run-user", InstallConfigUtil.getProperty("apache.run-user.default", "latw"));
		map.put("apache.template.dirname", InstallConfigUtil.getProperty("apache.template.dirname", "base"));

		map.put("nginx.service-port", InstallConfigUtil.getProperty("nginx.service-port.default", "8080"));
		// TODO

		map.put("comet.service-port", InstallConfigUtil.getProperty("comet.service-port.default", "5100"));
		map.put("comet.secondary-service-port", InstallConfigUtil.getProperty("comet.secondary-service-port.default", "5200"));
		map.put("comet.secondary-server-ip",  InstallConfigUtil.getProperty("comet.secondary-server-ip.default","127.0.0.1"));
		
		return map;
	}

	/**
	 * @param value
	 * @param defaultValue
	 * @return default value if value is null
	 */
	protected String getParameterValue(String value, String defaultValue) {
		return StringUtil.isBlank(value) ? defaultValue : value;
	}
	
	/**
	 * 서버가 기동중인지 확인한다
	 * @param targetPath
	 * @param commandFileName
	 */
	protected boolean isRunning(String targetPath, String commandFileName) {
		boolean res = true;
		
		String[] cmd = new String[]{FileUtil.getConcatPath(targetPath) + "/" + commandFileName + ".sh"};
		
		try {
		
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = br.readLine();
			
			if(s == null) {
				res = false;
			}
		} catch (Exception e) {
			throw new LatException(e);
		}
		
		return res;
	}

	public static String getEngineVersion(String serverType) throws IOException {
		String[] cmd;
		if(System.getProperty("os.name").indexOf("Windows") > -1){
			cmd=new String[]{"cmd","/c","dir -1r --sort=version "+FileUtil.getConcatPath(EnvUtil.getLatHome(),"engines", serverType)};
		}else{
			cmd=new String[]{"/bin/sh","-c","ls -1r --sort=version "+FileUtil.getConcatPath(EnvUtil.getLatHome(),"engines", serverType)};
		}

		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s=br.readLine();

		if(s==null){
			log.error(serverType+" engine is not installed");
			throw new LatException(serverType+" engine is not installed");
		} else{
			return s.substring(s.indexOf("-") + 1);
		}
	}

	protected String checkEmpty(String input){

		Scanner scan = new Scanner(System.in);

		while(input.isEmpty()){
			System.out.println("|Invalid input. Please enter again.");
			System.out.print("|: ");
			input = scan.nextLine();
		}

		return input;
	}

	protected abstract void execute() throws IOException;



}
