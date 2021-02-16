package com.github.markusbernhardt.proxy.search.desktop.win;

import java.net.ProxySelector;

import com.github.markusbernhardt.proxy.util.ProxyException;

/*****************************************************************************
 * Extracts the proxy settings from the windows registry. This will read the
 * windows system proxy settings.
 *
 * @author Victor Kropp, Copyright 2020
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class WinProxySearchStrategy extends CommonWindowsSearchStrategy {
	/**
	 * Resolves all host names directly without a proxy.
	 */
	static final int WINHTTP_ACCESS_TYPE_DEFAULT_PROXY = 0;

	/**
	 * Retrieves the static proxy or direct configuration from the registry.
	 * WINHTTP_ACCESS_TYPE_DEFAULT_PROXY does not inherit browser proxy
	 * settings. WinHTTP does not share any proxy settings with Internet
	 * Explorer.
	 * <p>
	 * The WinHTTP proxy configuration is set by one of these mechanisms.
	 * <ul>
	 * <li>The proxycfg.exe utility on Windows XP and Windows Server 2003 or
	 * earlier.</li>
	 * <li>The netsh.exe utility on Windows Vista and Windows Server 2008 or
	 * later.</li>
	 * <li>WinHttpSetDefaultProxyConfiguration on all platforms.</li>
	 * </ul>
	 */
	static final int WINHTTP_ACCESS_TYPE_NO_PROXY = 1;

	/**
	 * Passes requests to the proxy unless a proxy bypass list is supplied and
	 * the name to be resolved bypasses the proxy. In this case, this function
	 * uses WINHTTP_ACCESS_TYPE_NAMED_PROXY.
	 */
	static final int WINHTTP_ACCESS_TYPE_NAMED_PROXY = 3;


	/*************************************************************************
	 * Constructor
	 ************************************************************************/

	public WinProxySearchStrategy() {
		super();
	}

	/*************************************************************************
	 * getProxySelector
	 *
	 * @see com.github.markusbernhardt.proxy.ProxySearchStrategy#getProxySelector()
	 ************************************************************************/

	@Override
	public ProxySelector getProxySelector() throws ProxyException {
		return new WinProxySelector();
	}

	/*************************************************************************
	 * Gets the printable name of the search strategy.
	 *
	 * @return the printable name of the search strategy
	 ************************************************************************/

	@Override
	public String getName() {
		return "windows";
	}
}
