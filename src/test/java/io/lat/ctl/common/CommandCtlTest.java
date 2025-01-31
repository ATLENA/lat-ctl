package io.lat.ctl.common;

import static org.junit.Assert.*;

import org.junit.Test;

import io.lat.ctl.common.CommandCtl;

public class CommandCtlTest {
	
	public static final String INSTALLER = "INSTALLER";
	public static final String CONFIGURATOR = "CONFIGURATOR";

	@Test
	public void testCommandChecker() {
		CommandCtl commandCtl = new CommandCtl();
		String createCommand = "create";
		String exceptionCommand = "error";
		
		// check create command
		assertEquals(CommandCtl.INSTALLER, commandCtl.commandChecker(createCommand));
		
		// not contain command
		assertEquals("", commandCtl.commandChecker(exceptionCommand));
	}

}
