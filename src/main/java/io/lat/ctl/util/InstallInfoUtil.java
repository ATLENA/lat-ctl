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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.lat.ctl.common.vo.Server;
import io.lat.ctl.exception.LatException;
import io.lat.ctl.installer.LatApacheCreateInstaller;
import io.lat.ctl.resolver.XpathVariable;
import io.lat.ctl.type.InstallerServerType;

/**
 * Install info utilities.
 *
 * @author Pinepond
 */
public class InstallInfoUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(InstallInfoUtil.class);

	/**
	 * Write Server installation information in install-info.xml file.
	 *
	 * @param server server object
	 */
	public static void addInstallInfo(Server server) {
		String argoInstallFilePath = getInstallInfoFilePath();

		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			if (existsServer(server.getId(), server.getType())) {
				throw new LatException("Instance id alreay exists. '" + server.getId() + "'");
			}

			Element serversElement = (Element) xpath.evaluate("//install/servers", document, XPathConstants.NODE);

			Element serverElement = document.createElement("server");
			serverElement.appendChild(XmlUtil.createNode(document, "id", server.getId()));
			serverElement.appendChild(XmlUtil.createNode(document, "port", server.getPort()));
			serverElement.appendChild(XmlUtil.createNode(document, "type", server.getType()));
			serverElement.appendChild(XmlUtil.createNode(document, "path", server.getPath()));

			String timestamp = SystemUtil.getTimestamp();
			serverElement.appendChild(XmlUtil.createNode(document, "cdate", timestamp));
			serverElement.appendChild(XmlUtil.createNode(document, "udate", timestamp));

			serversElement.appendChild(serverElement);

			XmlUtil.writeXmlDocument(document, argoInstallFilePath);
		}
		catch (Throwable e) {
			//throw new LatException("An error occured when saving install-info.xml file", e);
			LOGGER.error("An error occured when saving install-info.xml file.\n"+e.getMessage());
		}
	}
	
	/**
	 * install-info.xml파일에서 서버 설치정보를 삭제한다. 
	 * @param id 서버ID
	 */
	public static void removeInstallInfo(String id, InstallerServerType serverType){
		String argoInstallFilePath = getInstallInfoFilePath();
		
		if(!existsServer(id, serverType.getServerType())){
			throw new LatException("Instance id doesn't exist. '" +  id + "'");
		}
		
		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		XpathVariable[] variables = {new XpathVariable("id", id), new XpathVariable("type", serverType.getServerType())};
		
		try {
			Element serverElement = (Element)XmlUtil.xpathEvaluate("//install/servers/server[id=$id and type=$type]", document, XPathConstants.NODE, xpath, variables);
			
			serverElement.getParentNode().removeChild(serverElement);
			
			XmlUtil.writeXmlDocument(document, argoInstallFilePath);
		} catch (Throwable e) {
			throw new LatException("An error occured when saving install-info.xml file", e);
		}
	}

	/**
	 * Returns install-info file path
	 *
	 * @return install -info file path
	 */
	public static String getInstallInfoFilePath() {
		return FileUtil.getConcatPath(EnvUtil.getLatManagementHome(), "etc", "info", "install-info.xml");
	}

	/**
	 * The Server exist or not
	 *
	 * @param instanceId the instance id
	 * @return true if the server exists , otherwise false
	 */
	public static boolean existsServer(String instanceId, String serverType) {
		if (!StringUtil.isBlank(getServerInstallPath(instanceId, InstallerServerType.getInstallServerType(serverType)))) {
			return true;
		}

		return false;
	}

	/**
	 * Search installation path of the server
	 *
	 * @param instanceId the instance id
	 * @return server install path
	 */
	public static String getServerInstallPath(String instanceId, InstallerServerType serverType) {
		return XmlUtil.getValueByTagName(getServerElement(instanceId, serverType), "path");
	}
	
	public static ArrayList<Element> getServerByType(InstallerServerType serverType) {
	
		String argoInstallFilePath = getInstallInfoFilePath();

		//Element element = document.createElement(nodeName);
		//NodeList nodeList = element.getElementsByTagName(tagName);
		
		Document document = XmlUtil.createDocument(argoInstallFilePath);
		
		
		//File inputFile = new File("/employee.xml");
        //DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        //DocumentBuilder dBuilder;
        //dBuilder = dbFactory.newDocumentBuilder();
        //Document doc = dBuilder.parse(inputFile);
        document.getDocumentElement().normalize();
        XPath xPath =  XPathFactory.newInstance().newXPath();
        String expression = "/install/servers/server";      
        ArrayList<Element> element = new ArrayList<Element> ();
        NodeList nodeList;
		try {
			nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);
            //System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            	
                Element eElement = (Element) nNode;
                /*
                System.out.println("Id : " 
                        + eElement
                        .getElementsByTagName("id")
                        .item(0)
                        .getTextContent());
                System.out.println("Type : " 
                        + eElement
                        .getElementsByTagName("type")
                        .item(0)
                        .getTextContent());
                System.out.println("Path : " 
                        + eElement
                        .getElementsByTagName("path")
                        .item(0)
                        .getTextContent());
                        */
                if(eElement.getElementsByTagName("type").item(0).getTextContent().equals(serverType.getServerType())) {
                	element.add(eElement);
                }
            }
        }
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*
		NodeList nodeList = document.getElementsByTagName("server");
		ArrayList<Element> element = null;
		System.out.println("LENGTH="+nodeList.getLength());
		for(int i=0; i<nodeList.getLength(); i++) {
			Element e = (Element) nodeList.item(i);
			
			NamedNodeMap nnm = nodeList.item(i).getAttributes();
			System.out.println("ID");
			System.out.println(nnm.item(0));
			
			
			System.out.println("GET TEXT CONTENT");
			System.out.println(nodeList.item(i).getTextContent());
			System.out.println("GET NODE VALUE");
			System.out.println(nodeList.item(i).getNodeValue());
			
			
			System.out.println("getAttribute type");
			System.out.println(e.getAttribute("type"));
			//if(e.getAttribute("type").equals(serverType.getServerType())){
				element.add(e);
			//}
			
		}
		*/
		
		return element;
	}

	/**
	 * Search element of the server in install-info.xml
	 * 
	 * @param instanceId the instance Id
	 * @return element object
	 */
	private static Element getServerElement(String instanceId, InstallerServerType serverType) {
		String argoInstallFilePath = getInstallInfoFilePath();

		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		Element element = null;
		XpathVariable[] variables = {new XpathVariable("id", instanceId), new XpathVariable("type", serverType.getServerType())};
		
		try {
			element = (Element) XmlUtil.xpathEvaluate("//install/servers/server[id=$id and type=$type]", document, XPathConstants.NODE, xpath, variables);
		}
		catch (XPathExpressionException e) {
			throw new LatException("Errors in release xml file", e);
		}

		return element;
	}
	
	/**
	 * Return service port of the instance id
	 * @param instanceId
	 * @return port
	 */
	public static String getServicePort(String instanceId, InstallerServerType serverType) {
		return XmlUtil.getValueByTagName(getServerElement(instanceId, serverType), "port");
	}
	
	/**
	 * Return Server object of instance id
	 * @param instanceId
	 * @return
	 */
	public static Server getServer(String instanceId, InstallerServerType serverType){
		Element serverElement = getServerElement(instanceId, serverType);
		if(serverElement == null){
			throw new LatException("There is no installed server '" + instanceId + "'");
		}
		return getServerByElement(serverElement);
	}
	
	/**
	 * Return Server object of server element
	 * @param serverElement
	 * @return
	 */
	private static Server getServerByElement(Element serverElement){
		Server server = new Server();
		server.setId(XmlUtil.getValueByTagName(serverElement, "id"));
		server.setPort(XmlUtil.getValueByTagName(serverElement, "port"));
		server.setType(XmlUtil.getValueByTagName(serverElement, "type"));
		server.setPath(XmlUtil.getValueByTagName(serverElement, "path"));
        server.setRecovery(XmlUtil.getValueByTagName(serverElement, "recovery"));
		server.setVersion(XmlUtil.getValueByTagName(serverElement, "version"));
		server.setCdate(XmlUtil.getValueByTagName(serverElement, "cdate"));
		server.setUdate(XmlUtil.getValueByTagName(serverElement, "udate"));
		server.setHotfix(XmlUtil.getValueByTagName(serverElement, "hotfix"));
		
		return server;
	}

	/**
	 * 서버타입에 해당하는 서버의 리스트를 가져온다.
	 * @param serverType 서버타입
	 * @return 서버리스트
	 */
	public static List<Server> getServerList(String serverType){
		List<Server> serverList = null;
		String argoInstallFilePath = getInstallInfoFilePath();

		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			NodeList nodeList = (NodeList)XmlUtil.xpathEvaluate("//install/servers/server[type=$type]", document, XPathConstants.NODESET, xpath, new XpathVariable("type", serverType));
			serverList = getServerList(nodeList);
		} catch (Throwable e) {
			throw new LatException("An error occured when reading install-info.xml file", e);
		}

		return serverList;
	}

	/**
	 * 서버타입에 해당하는 서버의 리스트를 가져온다.
	 * @param serverTypes 서버타입
	 * @return 서버리스트
	 */
	public static List<Server> getServerList(String serverTypes[]){
		List<Server> list = new ArrayList<Server>();
		for(String serverType : serverTypes){
			list.addAll(getServerList(serverType));
		}

		return list;
	}
	/**
	 * 전체 서버의 리스트를 가져온다.
	 * @return 서버리스트
	 */
	public static List<Server> getAllServerList(){
		List<Server> serverList = null;
		String argoInstallFilePath = getInstallInfoFilePath();

		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			NodeList nodeList = (NodeList)XmlUtil.xpathEvaluate("//install/servers/server", document, XPathConstants.NODESET, xpath);

			serverList = getServerList(nodeList);
		} catch (Throwable e) {
			throw new LatException("An error occured when reading install-info.xml file", e);
		}

		return serverList;
	}

	/**
	 * Server객체의 List를 가져온다.
	 * @param serverNodeList
	 * @return
	 */
	private static List<Server> getServerList(NodeList serverNodeList){
		List<Server> serverList = new ArrayList<Server>();

		for(int i=0; serverNodeList !=null && i<serverNodeList.getLength(); i++){
			Element element = (Element)serverNodeList.item(i);
			serverList.add(getServerByElement(element));
		}

		return serverList;
	}
}
