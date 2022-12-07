package io.lat.ctl.installer;

import io.lat.ctl.common.vo.Server;
import io.lat.ctl.exception.LatException;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.EnvUtil;
import io.lat.ctl.util.FileUtil;
import io.lat.ctl.util.InstallInfoUtil;
import io.lat.ctl.util.StringUtil;
import io.lat.ctl.util.XmlUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * lat 인스턴스를 삭제하는 공통 Installer
 */
public class LatInstanceDeleteInstaller extends LatInstaller {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LatInstanceDeleteInstaller.class);

	public LatInstanceDeleteInstaller(InstallerCommandType installerCommandType,
			InstallerServerType installerServerType) {
		super(installerCommandType, installerServerType);
	}

	public void execute() {

		HashMap<String, String> commandMap = getServerInfoFromUser();

		String instanceId = commandMap.get("INSTANCE_ID");
		String logHomeDeleteFlag = getParameterValue(commandMap.get("LOG_HOME_DELETE_FLAG"), "D");

		if(!confirm(instanceId)){
			LOGGER.error("INSTANCE_ID doesn't match.");
			throw new LatException("INSTANCE_ID doesn't match.");
		}

		String targetPath = InstallInfoUtil.getServerInstallPath(instanceId);
		LOGGER.debug("targetPath = "+targetPath);
		
		if (StringUtil.isBlank(targetPath)) {
			LOGGER.error(instanceId + " doesn't exist.");
			throw new LatException(instanceId + " doesn't exist.");
		}

		// 서버가 기동중인 경우 삭제할 수 없음
		if (isRunning(targetPath, "ps")) {
			LOGGER.error(instanceId + " is running. Retry after the instance is stopped.");
			throw new LatException(instanceId + " is running.");
		}

		Server srcServer = InstallInfoUtil.getServer(instanceId);
		if (srcServer.getType().equals(getServerType())) {
			try {
				//String loghome = EnvUtil.getLogHome();
				Map<String, String> env = EnvUtil.getEnv(instanceId, getInstallerServerType());
		        String loghome = env.get("LOG_HOME");
				//String loghome = FileUtil.getShellVariable(FileUtil.getConcatPath(targetPath, "env.sh"), "LOG_HOME");
				System.out.println("LOG_HOME = "+loghome);
				
				// 'D' 일경우 기본 모드로 동작
				// log home 이 server home 내부에 위치한경우 삭제되며
				// log home 이 server home 외부에 위치한경우 삭제하지 않는다.
				if (!("Y".equals(logHomeDeleteFlag)) && !("N".equals(logHomeDeleteFlag)) && !("D".equals(logHomeDeleteFlag))){
//					// TODO
					throw new LatException("must select Y, N or D");
				} else if ("D".equals(logHomeDeleteFlag)) {
					if (loghome.contains(targetPath)) {
						logHomeDeleteFlag = "Y";
					} else {
						logHomeDeleteFlag = "N";
					}
				} 

				// 로그홈 삭제 && 내부
				// 1. server home 삭제
				// 로그홈 유지 && 외부
				// 1. server home 삭제
				// 로그홈 삭제 && 외부
				// 1. server home 삭제
				// 2. log home 삭제
				// 로그홈 유지 && 내부
				// 3. server home 디렉토리중 , log 디렉토리를 제외한 디렉토리 삭제

				if ("N".equalsIgnoreCase(logHomeDeleteFlag) && loghome.contains(targetPath)) {
					// 3. server home 디렉토리중 , log 디렉토리를 제외한 디렉토리 삭제
					FileUtil.deleteDirWithExceptDir(new File(targetPath), new File(loghome));
					
					System.out.println("Delete Target Home : " + targetPath);
					System.out.println("Don't Delete Log Home : " + loghome);
					
				} else {
					// 1. server home 삭제
					FileUtil.delete(targetPath);
					LOGGER.debug("The instance is deleted : " + targetPath);
					System.out.println("The instance is deleted : " + targetPath);
					
					if ("Y".equalsIgnoreCase(logHomeDeleteFlag)) {
						// 2. log home 삭제
						FileUtil.delete(loghome);
						System.out.println("Delete Log : " + loghome);
					} else {
						System.out.println("Don't delete Log Home : " + loghome);
					}
				}

				// update install-info.xml
				InstallInfoUtil.removeInstallInfo(instanceId);
			} catch (Exception e) {
				throw new LatException(e);
			}

		} else {
			LOGGER.error("Server Type matching error");
			throw new LatException("Server Type matching error");
		}
	}

	/**
	 * @return Server information to be created
	 */
	public HashMap<String, String> getServerInfoFromUser() {
		HashMap<String, String> commandMap = new HashMap<String, String>();
		Scanner scan = new Scanner(System.in);
		
		System.out.println("+-------------------------------------------------------------------------------------");
		System.out.println("| 1. Select instance to delete. Enter number of the instance or instance ID");
		ArrayList<Element> elementList = InstallInfoUtil.getServerByType(getInstallerServerType());
		for(int i=0; i<elementList.size(); i++) {
			Element e = elementList.get(i);
			
			System.out.println("| ("+(i+1)+") "+XmlUtil.getValueByTagName(e, "id")+" "+XmlUtil.getValueByTagName(e, "path"));
		}

		
		//System.out.println("| 1. INSTANCE_ID : Instance ID to delete                                                  ");
		System.out.print("|: ");
		commandMap.put("INSTANCE_ID", scan.nextLine());
		
		if(commandMap.get("INSTANCE_ID").compareTo("1")>=0 && commandMap.get("INSTANCE_ID").compareTo(elementList.size()+"")<=0) {
			commandMap.put("INSTANCE_ID", XmlUtil.getValueByTagName(elementList.get(Integer.parseInt(commandMap.get("INSTANCE_ID"))-1),"id"));
		}
		System.out.println("| 2. LOG_HOME_DELETE_FLAG : whether to delete LOG Home ['Y','N','D'] ('D' is default) ");
		System.out.println("| (Default: 'D') ");
		System.out.println("|   - Y : Delete files in LOG_HOME.");
		System.out.println("|   - N : Do NOT delete files in LOG_HOME.");
		System.out.println("|   - D : Delete files in LOG_HOME if LOG_HOME is included in INSTALL_ROOT_PATH, and");
		System.out.println("|         do NOT delete files in LOG_HOME if LOG_HOME is separated from INSTALL_ROOT_PATH.");
		System.out.print("|: ");
		commandMap.put("LOG_HOME_DELETE_FLAG", scan.nextLine());
		System.out.println("+-------------------------------------------------------------------------------------");

		return commandMap;
	}

	private boolean confirm(String instanceId){
		Scanner scan = new Scanner(System.in);
		System.out.println("| [Delete Instance]");
		System.out.println("| ");
		System.out.println("| Are you sure you want to delete "+instanceId+"?");
		System.out.println("| You can't undo this action.");
		System.out.println("| To confirm deletion, enter ["+instanceId+"]");
		System.out.print("|: ");
		//commandMap.put("SERVER_ID", scan.nextLine());
		if( scan.nextLine().equalsIgnoreCase(instanceId)){
			System.out.println("+-------------------------------------------------------------------------------------");
			return true;
		}else {
			System.out.println("| Instance ID does not match. Check the INSTANCE_ID again.");
			System.out.println("+-------------------------------------------------------------------------------------");
			return false;
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see argo.install.installer.ArgoInstaller#getRequiredArgumentNames()
	 */
	protected String[] getRequiredArgumentNames() {
		return new String[] { "INSTANCE_ID" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see argo.install.installer.ArgoInstaller#getOptionalArgumentNames()
	 */
	protected String[] getOptionalArgumentNames() {
		return new String[] { "LOG_HOME_DELETE_FLAG" };
	}
}
