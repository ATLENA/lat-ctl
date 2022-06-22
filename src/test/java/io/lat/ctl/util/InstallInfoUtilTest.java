package io.lat.ctl.util;

import io.lat.ctl.common.vo.Server;
import io.lat.ctl.exception.LatException;
import io.lat.ctl.resolver.XpathVariable;
import io.lat.ctl.util.testtools.FileBasedTestCase;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test InstallInfoUtil Class
 *
 * @author Pinepond
 * @see InstallInfoUtil
 */
public class InstallInfoUtilTest extends FileBasedTestCase {

	private String latHome;
	private String latManagementHome;
	final File top = getLocalTestDirectory();

	public InstallInfoUtilTest(String name) {
		super(name);
	}

	private File getLocalTestDirectory() {
		return new File(getTestDirectory(), "test-installinfo-util");
	}

	@Override
	protected void setUp() throws Exception {
		top.mkdirs();
		latHome = top.getParent();
		latManagementHome = latHome + "/lat";
		System.setProperty("lat.home", latHome);
		System.setProperty("lat.management.home", latManagementHome);
	}

	@Override
	protected void tearDown() throws Exception {
		chmod(top, 775, true);
		CustomFileUtils.deleteDirectory(top);
	}

	@Test
	public void testAddInstallInfo() {

		// create temp install info xml
		File installInfoFile = new File(InstallInfoUtil.getInstallInfoFilePath());
		String defaultXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<install>\n"
				+ "  <servers>\n"
				+ "  </servers>\n"
				+ "        <modules>\n"
				+ "                <module>\n"
				+ "                        <id>lena-ctlr</id>\n"
				+ "                        <version>1.0.0</version>\n"
				+ "                </module>\n"
				+ "        </modules>\n"
				+ "</install>";

		FileUtil.writeStringToFile(installInfoFile, defaultXml);

		// set server info
		String serverId = "test_server";
		String serverPort = "8080";
		String serverType = "was";
		String serverPath = top.getPath() + File.separator + "server";

		// set server object
		Server server = new Server();
		server.setId(serverId);
		server.setPort(serverPort);
		server.setType(serverType);
		server.setPath(serverPath);

		// add server
		InstallInfoUtil.addInstallInfo(server);

		// read server from install-info.xml file
		String argoInstallFilePath = InstallInfoUtil.getInstallInfoFilePath();
		Document document = XmlUtil.createDocument(argoInstallFilePath);
		XPath xpath = XPathFactory.newInstance().newXPath();
		Element element = null;
		try {
			element = (Element) XmlUtil.xpathEvaluate("//install/servers/server[id=$id]", document, XPathConstants.NODE, xpath, new XpathVariable("id", serverId));
		}
		catch (XPathExpressionException e) {
			throw new LatException("Errors in release xml file", e);
		}

		String resultServerId = XmlUtil.getValueByTagName(element, "id");
		String resultServerPort = XmlUtil.getValueByTagName(element, "port");
		String resultServerType = XmlUtil.getValueByTagName(element, "type");
		String resultServerPath = XmlUtil.getValueByTagName(element, "path");

		// check result
		assertEquals(serverId, resultServerId);
		assertEquals(serverPort, resultServerPort);
		assertEquals(serverType, resultServerType);
		assertEquals(serverPath, resultServerPath);

	}

	@Test
	public void testGetInstallInfoFilePath() throws IOException {
		File temp = new File(InstallInfoUtil.getInstallInfoFilePath());
		assertTrue(temp.getCanonicalPath().startsWith(latHome));

	}

	private boolean chmod(File file, int mode, boolean recurse) throws InterruptedException {
		// TODO: Refactor this to FileSystemUtils
		List<String> args = new ArrayList<String>();
		args.add("chmod");

		if (recurse) {
			args.add("-R");
		}

		args.add(Integer.toString(mode));
		args.add(file.getAbsolutePath());

		Process proc;

		try {
			proc = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
		}
		catch (IOException e) {
			return false;
		}
		int result = proc.waitFor();
		return result == 0;
	}
}