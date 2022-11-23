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

package io.lat.ctl.common;

/**
 * Object that can check which command user input.
 * @author Erick Yu
 * 
 */
public class CommandCtl {
	public static final String INSTALLER = "INSTALLER";
	public static final String CONFIGURATOR = "CONFIGURATOR";
	public static final String EXECUTOR = "EXECUTOR";
	public static final String CONTROLLER = "CONTROLLER";

	InstallCommandCtl installCommandCtl = null;
	ConfigureCommandCtl configureCommandCtl = null;
	ControllerCommandCtl controllerCommandCtl = null;

	public CommandCtl() {

		installCommandCtl = new InstallCommandCtl();
		configureCommandCtl = new ConfigureCommandCtl();
		controllerCommandCtl = new ControllerCommandCtl();
	}

	/**
	 * Check which command inputed.
	 * @param command
	 * @return Appropriate CommandCtl by user command
	 */
	public String commandChecker(String command) {
		String result = "";
		if (installCommandCtl.containsCommand(command)) {
			result = INSTALLER;
		} else if (configureCommandCtl.containsCommand(command)){
			result = CONFIGURATOR;
		} else if (controllerCommandCtl.containsCommand(command)){
			result = CONTROLLER;
		}

		return result;
	}

	public static void printHelpPage(){
		System.out.println(
				"Usage: latctl.sh COMMAND [SERVER_TYPE]\n" +
						"\n" +
						"Enlena commands are:\n" +
						"\n" +
						"Instance Intallation Commands:\n" +
						"\n" +
						"\tcreate\t\tCreates a new instance\n" +
						"\t\t\tex) latctl.sh create tomcat\n\n" +
						"\tdelete\t\tRemoves an instance\n" +
						"\t\t\tex) latctl.sh delete apache\n" +
						"\n" +
						"OSS Engine Administration Commands:\n" +
						"\n" +
						"\tlist-engines\t\tDisplays available OSS engine versions from LA:T engine repository.\n" +
						"\t\t\t\tex) latctl.sh list-engines tomcat\n\n" +
						"\tdownload-engine\t\tDownloads a new OSS engine version from LA:T engine repository.\n" +
						"\t\t\t\tex) latctl.sh download-engine apache\n\n" +
						"\tswitch-version\t\tSwitches OSS engine version of an instance\n" +
						"\t\t\t\tex) latctl.sh switch-version tomcat\n\n"+
				"Available LA:T OSS [SERVER_TYPE]s:\n\n" +
						"\ttomcat\tWeb Application Server\n\n" +
						"\tapache\tHTTP Server\n\n" +
						"\tcomet\tLA:T Session server\n\n"
		);
	}
}
