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

import java.io.IOException;

/**
 * Interface for Installer
 * @author Erick Yu
 *
 */
public interface Installer {

	/**
	 * Execute installation
	 * @param args command
	 * @return Map Object which is installation result.
	 */
	public void execute(String[] args) throws IOException;
}
