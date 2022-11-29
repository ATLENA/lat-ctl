package io.lat.ctl.installer;

import io.lat.ctl.type.InstallerCommandType;
import io.lat.ctl.type.InstallerServerType;
import io.lat.ctl.util.CustomFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LatApacheCreateInstallerTest {

	final File top = new File("test-installer/");

	@Rule public final TextFromStandardInputStream systemInMock = TextFromStandardInputStream.emptyStandardInputStream();

	@Before
	public void setUp() throws Exception {
		top.mkdirs();
	}

	@After
	public void tearDown() throws Exception {
		chmod(top, 775, true);
		CustomFileUtils.deleteDirectory(top);
	}

	@Test
	public void getServerInfoFromUser() throws IOException {
		String id = "server_id";
		String port = "7080";
		String user = "lenaw";
		//String engnPath = top.getAbsolutePath() + File.separator + "modules" + File.separator + "lena-web-pe";
		String installPath = top.getAbsolutePath() + File.separator + "instances";
		String logPath = installPath + File.separator + "logs";
		String docPath = installPath + File.separator + "htdocs";

		systemInMock.provideLines(id, port, user, installPath, logPath, docPath);
		LatApacheCreateInstaller installer = new LatApacheCreateInstaller(InstallerCommandType.CREATE, InstallerServerType.APACHE);
		HashMap<String, String> result = installer.getServerInfoFromUser();

		assertEquals(id, result.get("INSTANCE_ID"));
		assertEquals(port, result.get("SERVICE_PORT"));
		assertEquals(user, result.get("RUN_USER"));
		assertEquals(installPath, result.get("INSTALL_ROOT_PATH"));
		assertEquals(logPath, result.get("LOG_HOME"));
		assertEquals(docPath, result.get("DOCUMENT_ROOT_PATH"));

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