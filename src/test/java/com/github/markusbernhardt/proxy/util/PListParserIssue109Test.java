package com.github.markusbernhardt.proxy.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.util.PListParser.Dict;
import com.github.markusbernhardt.proxy.util.PListParser.XmlParseException;

class PListParserIssue109Test {

	private static final String TEST_SETTINGS = TestUtil.TEST_DATA_FOLDER + File.separator + "osx" + File.separator
	        + "osx_issue109.plist";

	private static Dict pList;

	/*************************************************************************
	 * Setup the dictionary from the test data file.
	 ************************************************************************/
	@BeforeAll
	public static void setupClass() throws XmlParseException, IOException {
		pList = PListParser.load(new File(TEST_SETTINGS));
	}

	/**
	 * Test method for {@link com.btr.proxy.util.PListParser#load(java.io.File)}
	 * .
	 */
	@Test
	public void testLoadFile() {
		assertTrue(pList.size() > 0);
	}

	/*************************************************************************
	 * Test method
	 ************************************************************************/

	@Test
	public void testStructure() {
		String currentSet = (String) pList.get("CurrentSet");
		assertNotNull(currentSet);
		Object networkServices = pList.get("NetworkServices");
		assertTrue(networkServices instanceof Dict);
	}


}
