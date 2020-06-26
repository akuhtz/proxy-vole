package com.github.markusbernhardt.proxy.search.desktop;

import static org.junit.Assert.fail;

import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Unit tests for the desktop facade search strategy.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class DesktopProxySearchTest {

	/*************************************************************************
	 * Test method.
	 * 
	 * @throws ProxyException
	 *             on error.
	 ************************************************************************/
	@Test
	public void testDesktopStrategsIsWorking() throws ProxyException {
		try {
			new DesktopProxySearchStrategy().getProxySelector();	
		} catch (ProxyException e) {
			fail();
		}
	}

	/*************************************************************************
	 * Test method.
	 * 
	 * @throws URISyntaxException
	 *             on error parsing the URI.
	 * @throws ProxyException
	 *             on selector error.
	 ************************************************************************/
	@Test
	public void emptyURIShouldNotRaiseNPE() throws URISyntaxException, ProxyException {
		ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
		ProxySelector myProxySelector = proxySearch.getProxySelector();
		if (myProxySelector != null) {
			try {
				myProxySelector.select(new URI(""));
			} catch (NullPointerException e) {
				fail("NullPointerException should not be thrown if using an empty URI.");
			}
		}
	}

}
