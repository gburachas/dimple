/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.test;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.Model;


public class VersionTest {

	@Test
	public void test_version()
	{
		String str = Model.getVersion();
//		System.out.println(str);
		
		// <version> <branch> <iso-date>
		String pattern = "^\\d+\\.\\d+ [^\\s]+ (.*)$";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		assertTrue(m.find());
	
		// Validate the date portion
		String dateStr = m.group(1);
		DateFormat dateParser = new SimpleDateFormat("y-M-d h:m:s Z");
		try
		{
			Date date = dateParser.parse(dateStr);
			assertTrue(date.before(new Date()));
		}
		catch (ParseException ex)
		{
			fail(ex.getMessage());
		}
	}
}
