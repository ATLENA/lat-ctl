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

package io.lat.ctl.util;

import io.lat.ctl.exception.LatException;
import io.lat.ctl.installer.LatNginxCreateInstaller;
import io.lat.ctl.installer.LatTomcatCreateInstaller;
import io.lat.ctl.installer.LatApacheCreateInstaller;
import io.lat.ctl.installer.LatCometCreateInstaller;
import io.lat.ctl.resolver.XpathVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * Release info utilities.
 *
 * @author Pinepond
 */
public class ReleaseInfoUtil {

	/**
	 * Search the depot path corresponding to the server type
	 *
	 * @param serverType the server type
	 * @return depot path
	 */
	public static String getDepotPath(String serverType) throws IOException {
    //TODO refactoring required
		String[] split = new String[]{"",""};

		if(serverType.equals("apache")){
			split = LatApacheCreateInstaller.getEngineVersion("apache").split("\\.");
		}else if(serverType.equals("tomcat")){
			split = LatTomcatCreateInstaller.getEngineVersion("tomcat").split("\\.");
		}else if(serverType.equals("comet")){
			split = LatCometCreateInstaller.getEngineVersion("comet").split("\\.");
		}else if(serverType.equals("nginx")){
			split = LatNginxCreateInstaller.getEngineVersion("nginx").split("\\.");
		}

		return FileUtil.getConcatPath(EnvUtil.getLatManagementHome(), "depot", "template", serverType, "base-"+serverType+"-"+split[0]+"."+split[1]);
	}

	/**
	 * Search Module version of the server type
	 *
	 * @param serverType the server type
	 * @return module version
	 */
	public static String getModuleVersion(String serverType) {
		String version = XmlUtil.getValueByTagName(getModuleElement(serverType), "version");

		if (version == null || "".equals(version.trim())) {
			throw new LatException(serverType + " is incorrect");
		}

		return version;
	}

	/**
	 * Returns the module element in the release-info.xml file.
	 *
	 * @param serverType Server Type
	 * @return Element Object
	 */
	private static Element getModuleElement(String serverType) {
		Document document = XmlUtil.createDocument(getReleaseInfoFilePath());
		XPath xpath = XPathFactory.newInstance().newXPath();
		Element element = null;
		try {
			element = (Element) XmlUtil.xpathEvaluate("//release/depot/modules/module[id=$id]", document, XPathConstants.NODE, xpath, new XpathVariable("id", serverType));
		}
		catch (XPathExpressionException e) {
			throw new LatException("An error occured when reading release-info.xml file", e);
		}

		return element;
	}

	/**
	 * Search the path of the release-info.xml file.
	 *
	 * @param latManagementHome the lat home
	 * @return release info file path
	 */
	public static String getReleaseInfoFilePath(String latManagementHome) {
		String argoReleaseFilePath = FileUtil.getConcatPath(latManagementHome, "etc", "info", "release-info.xml");
		if (!FileUtil.exists(argoReleaseFilePath)) {
			throw new LatException("There is no release-info.xml file");
		}
		return argoReleaseFilePath;
	}

	/**
	 * Search the path of the release-info.xml file.
	 *
	 * @return release info file path
	 */
	public static String getReleaseInfoFilePath() {
		return getReleaseInfoFilePath(EnvUtil.getLatManagementHome());
	}
}
