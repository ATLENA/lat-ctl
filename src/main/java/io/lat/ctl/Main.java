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

package io.lat.ctl;

import io.lat.ctl.common.CommandCtl;
import io.lat.ctl.configurator.Configurator;
import io.lat.ctl.configurator.ConfiguratorMapper;
import io.lat.ctl.installer.Installer;
import io.lat.ctl.installer.InstallerMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Main of LA:T CTL
 * @author Erick Yu
 *
 */
public class Main {

	public static void main(String[] args) throws Exception {

		List<String> commandList = new ArrayList<String>();

		for (String tmp : args) {
			commandList.add(tmp);
		}

		if (commandList.size() < 2) {
			System.exit(1);
		}

		String command = commandList.get(0); 

		CommandCtl commandCtl = new CommandCtl();
		String commandMapper = commandCtl.commandChecker(command);

		if (commandMapper.equals(CommandCtl.INSTALLER)) {
			Installer installer = InstallerMapper.getInstaller(commandList); 
			installer.execute(args);
		}else if(commandMapper.equals(CommandCtl.CONFIGURATOR)) {
			Configurator configurator = ConfiguratorMapper.getConfigurator(commandList);
			configurator.execute(args);
		}
	}
}
