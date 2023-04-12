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

import io.lat.ctl.common.CommandCtl;
import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper class for mapping Web Installer or WAS Insatller.
 * @author Erick Yu
 *
 */

@Slf4j
public class InstallerMapper {

	/**
	 * @param commandList
	 * @return Server create Installer
	 */
	public static Installer getInstaller(List<String> commandList) {
		String command = commandList.get(0);
		String serverType = commandList.get(1);
		
		log.debug("Start [latctl.sh "+command+" "+serverType+"]");

		InstallerServerType installerServerType = null;
		InstallerCommandType installerCommandType = null;

		installerServerType = InstallerServerType.getInstallServerType(serverType);
		installerCommandType = InstallerCommandType.valueOf(command.toUpperCase());

		if(installerServerType==null){
			CommandCtl.printHelpPage();
			System.exit(1);
		}

		switch (installerServerType) {
			case APACHE:
				switch (installerCommandType) {
					case CREATE:
						return new LatApacheCreateInstaller(installerCommandType, installerServerType);
					case DELETE:
						return new LatInstanceDeleteInstaller(installerCommandType, installerServerType);
				}
			case TOMCAT:
				switch (installerCommandType) {
					case CREATE:
						return new LatTomcatCreateInstaller(installerCommandType, installerServerType);
					case DELETE:
						return new LatInstanceDeleteInstaller(installerCommandType, installerServerType);
				}
			case COMET:
				switch (installerCommandType) {
					case CREATE:
						return new LatCometCreateInstaller(installerCommandType, installerServerType);
					case DELETE:
						return new LatInstanceDeleteInstaller(installerCommandType, installerServerType);
				}
			case NGINX:
				switch (installerCommandType){
					case CREATE:
						return new LatNginxCreateInstaller(installerCommandType, installerServerType);
					case DELETE:
						return new LatInstanceDeleteInstaller(installerCommandType, installerServerType);
				}
		}
		return null;
	}
}
