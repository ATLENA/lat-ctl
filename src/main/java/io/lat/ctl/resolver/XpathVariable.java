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

package io.lat.ctl.resolver;

/**
 * Object for passing key/value of variable to resolver for xpath variable processing.
 * @author Erick Yu
 *
 */
public class XpathVariable {
	private final String key;
	private String value;

	public XpathVariable(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
