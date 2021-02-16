package com.github.markusbernhardt.proxy.search.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.github.markusbernhardt.proxy.TestUtil;
import com.github.markusbernhardt.proxy.search.browser.ie.IELocalByPassFilter;
import com.github.markusbernhardt.proxy.search.desktop.win.WinProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.ProxyException;
import com.github.markusbernhardt.proxy.util.UriFilter;

/*****************************************************************************
 * Unit tests for the InternetExplorer search. Only limited testing as this only
 * runs on windwos and needs a installed IE and IE proxy settings written to the
 * registry.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class IeTest {

	/*************************************************************************
	 * Test method.
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 ************************************************************************/
	@Test
	@EnabledOnOs(OS.WINDOWS)
	public void testInvoke() throws ProxyException {
		WinProxySearchStrategy st = new WinProxySearchStrategy();

		// Try at least to invoke it and test if the dll does not crash
		ProxySelector ps = st.getProxySelector();
		
        List<Proxy> result = ps.select(TestUtil.HTTPS_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
	}

	/*************************************************************************
	 * Test method.
	 * 
	 * @throws ProxyException
	 *             on proxy detection error.
	 * @throws URISyntaxException
	 *             if url syntax is wrong.
	 * @throws MalformedURLException
	 *             on wrong url format.
	 ************************************************************************/
	@Test
	public void testLocalByPassFilter() throws ProxyException, MalformedURLException, URISyntaxException {
		UriFilter filter = new IELocalByPassFilter();
		assertTrue(filter.accept(TestUtil.LOCAL_TEST_URI));
		assertFalse(filter.accept(TestUtil.HTTP_TEST_URI));
		assertFalse(filter.accept(new URL("http://123.45.55.6").toURI()));
	}

}
