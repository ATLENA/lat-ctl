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

package io.lat.ctl.type;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Erick Yu
 *
 */

@Slf4j
public enum InstallerServerType {
	APACHE("apache"), TOMCAT("tomcat"), COMET("comet"), NGINX("nginx");

	private String serverType;

	InstallerServerType(String serverType) {
		this.serverType = serverType;
	}

	/**
	 * @return serverType
	 */
	public String getServerType() {
		return serverType;
	}

	/**
	 * @param serverType
	 * @return appropriate server type
	 */
	public static InstallerServerType getInstallServerType(String serverType) {
		InstallerServerType type = null;
		try {
			type = InstallerServerType.valueOf(serverType.replace("-", "_").toUpperCase());
		}
		catch (Exception e) {
			log.error("Fail in getting install server type");
		}
		return type;
	}
}
