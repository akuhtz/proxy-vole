package com.github.markusbernhardt.proxy.selector.whitelist;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*****************************************************************************
 * Some unit tests for the IP subnet mask checker.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IPv4WithSubnetCheckerTest {

	/*************************************************************************
	 * Test method.
	 ************************************************************************/
	@Test
	public void testIsValidIP4() {
		assertTrue(IPWithSubnetChecker.isValidIP4Range("127.0.0.1/8"), "Accept 127.0.0.1/8");
		assertTrue(IPWithSubnetChecker.isValidIP4Range("127.0.0.1/32"), "Accept 127.0.0.1/32");
		assertTrue(IPWithSubnetChecker.isValidIP4Range("255.255.255.255/32"), "Accept 255.255.255.255/32");
		assertTrue(IPWithSubnetChecker.isValidIP4Range("0.0.0.0/0"), "Accept 0.0.0.0/0");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("127.0.0.1"), "Reject 127.0.0.1");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("localhost"), "Reject localhost");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("http://www.sick.de"), "Reject http://www.sick.de");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("test.sick.de"), "Reject test.sick.de");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("400.400.400.400"), "Reject 400.400.400.400");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("127.0.0.1/33"), "Reject 127.0.0.1/33");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("127.0.0.*"), "Reject 127.0.0.*");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("127.0.0.*/8"), "Reject 127.0.0.*/8");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("www.test.com/8"), "Reject www.test.com/8");
		assertFalse(IPWithSubnetChecker.isValidIP4Range("127.0.0.1/33.html"), "Reject 127.0.0.1/33.html");
	}

	/*************************************************************************
	 * Test method.
	 ************************************************************************/
	@Test
	public void testIsValidIP6() {
		assertTrue(IPWithSubnetChecker.isValidIP6Range("2001:db8::/32"), "Accept 2001:db8::/32");
		assertTrue(IPWithSubnetChecker.isValidIP6Range("0::0/0"), "Accept 0::0/0");
		assertTrue(IPWithSubnetChecker.isValidIP6Range("2001:db8::/128"), "Accept 2001:db8::/128");

		assertFalse(IPWithSubnetChecker.isValidIP6Range("2001:zb8::/32"), "Reject 2001:zb8::/32");
		assertFalse(IPWithSubnetChecker.isValidIP6Range("localhost"), "Reject localhost");
	}

}
