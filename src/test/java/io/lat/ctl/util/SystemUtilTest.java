package io.lat.ctl.util;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import io.lat.ctl.util.SystemUtil;

import static org.junit.Assert.*;

public class SystemUtilTest {

	@Test
	public void getTimestamp() {
		Calendar cal = Calendar.getInstance();
		String year = Integer.toString(cal.get(Calendar.YEAR));
		String timeStamp = SystemUtil.getTimestamp();
		// check year
		assertTrue(SystemUtil.getTimestamp().startsWith(year));
		// check length
		assertTrue(timeStamp.length() == 17);
	}
}