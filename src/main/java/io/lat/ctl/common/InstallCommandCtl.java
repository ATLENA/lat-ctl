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

import org.apache.commons.cli.Options;

/**
 * Object that define install command.
 * @author Erick Yu
 * 
 */
public class InstallCommandCtl {
	final String CREATE = "CREATE";
	final String DELETE = "DELETE";

	Options options = null;

	public InstallCommandCtl() {
		initCommandOptions();
	}

	/**
	 * Initialize the command option
	 */
	public void initCommandOptions() {
		options = new Options();
		options.addOption(CREATE, true, "create server");
	}

	/**
	 * @param command
	 * @return true if user's input is a command defined.
	 */
	public boolean containsCommand(String command) {
		boolean result = false;
		if(CREATE.toLowerCase().equals(command.toLowerCase())) {
			result = true;
		}
		else if(DELETE.toLowerCase().equals(command.toLowerCase())) {
			result = true;
		}

		return result;
	}

	/**
	 * @return Option object
	 */
	public Options getCommandOption() {
		return options;
	}

}
