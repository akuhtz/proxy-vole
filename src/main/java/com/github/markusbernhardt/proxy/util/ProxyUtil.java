package com.github.markusbernhardt.proxy.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.github.markusbernhardt.proxy.selector.fixed.FixedProxySelector;
import com.github.markusbernhardt.proxy.selector.fixed.FixedSocksSelector;
import com.github.markusbernhardt.proxy.selector.misc.ProtocolDispatchSelector;
import com.github.markusbernhardt.proxy.selector.pac.PacProxySelector;
import com.github.markusbernhardt.proxy.selector.pac.PacScriptSource;
import com.github.markusbernhardt.proxy.selector.pac.UrlPacScriptSource;
import com.github.markusbernhardt.proxy.selector.whitelist.ProxyBypassListSelector;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Small helper class for some common utility methods.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class ProxyUtil {

	public static final int DEFAULT_PROXY_PORT = 80;

	private static List<Proxy> noProxyList;

	/*************************************************************************
	 * Parse host and port out of a proxy variable.
	 * 
	 * @param proxyVar
	 *            the proxy string. example: http://192.168.10.9:8080/
	 * @return a FixedProxySelector using this settings, null on parse error.
	 ************************************************************************/

	public static FixedProxySelector parseProxySettings(String proxyVar) {
		if (proxyVar == null || proxyVar.trim().length() == 0) {
			return null;
		}

		try {
			// Protocol missing then assume http and provide it
			if (proxyVar.indexOf(":/") == -1) {
				proxyVar = "http://" + proxyVar;
			}

			URL url = new URL(proxyVar);
			String host = cleanIPv6(url.getHost());

			int port = url.getPort();
			if (port == -1) {
				port = DEFAULT_PROXY_PORT;
			}
			return new FixedProxySelector(host.trim(), port);
		} catch (MalformedURLException e) {
			Logger.log(ProxyUtil.class, LogLevel.WARNING, "Cannot parse Proxy Settings {}", proxyVar);
			return null;
		}
	}

	/*************************************************************************
	 * Gets an unmodifiable proxy list that will have as it's only entry an
	 * DIRECT proxy.
	 * 
	 * @return a list with a DIRECT proxy in it.
	 ************************************************************************/

	public static synchronized List<Proxy> noProxyList() {
		if (noProxyList == null) {
			ArrayList<Proxy> list = new ArrayList<Proxy>(1);
			list.add(Proxy.NO_PROXY);
			noProxyList = Collections.unmodifiableList(list);
		}
		return noProxyList;
	}

	/*************************************************************************
	 * Build a PAC proxy selector for the given URL.
	 * 
	 * @param url
	 *            to fetch the PAC script from.
	 * @return a PacProxySelector or null if it is not possible to build a
	 *         working selector.
	 ************************************************************************/

	public static PacProxySelector buildPacSelectorForUrl(String url) {
		PacProxySelector result = null;
		PacScriptSource pacSource = new UrlPacScriptSource(url);
		if (pacSource.isScriptValid()) {
			result = new PacProxySelector(pacSource);
		}
		return result;
	}

	/*************************************************************************
	 * This method can be used to cleanup an IPv6 address. It will remove the
	 * surrounding square brackets if found. e.g. [2001:4860:0:2001::68] will be
	 * returned as 2001:4860:0:2001::68
	 * 
	 * @param hostOrIP
	 *            to cleanup
	 * @return the raw host or IP without any IPv6 brackets.
	 ************************************************************************/

	public static String cleanIPv6(String hostOrIP) {
		if (hostOrIP == null) {
			return null;
		}
		hostOrIP = hostOrIP.trim();
		if (hostOrIP.startsWith("[")) {
			hostOrIP = hostOrIP.substring(1);
		}
		if (hostOrIP.endsWith("]")) {
			hostOrIP = hostOrIP.substring(0, hostOrIP.length() - 1);
		}
		return hostOrIP;
	}

	/*************************************************************************
	 * Installs the proxy exclude list on the given selector.
	 *
	 * @param bypassList
	 *            the list of urls / hostnames to ignore.
	 * @param ps
	 *            the proxy selector to wrap.
	 * @return a wrapped proxy selector that will handle the bypass list.
	 ************************************************************************/

	public static ProxySelector setByPassListOnSelector(String bypassList, ProtocolDispatchSelector ps) {
		if (bypassList != null && bypassList.trim().length() > 0) {
			return new ProxyBypassListSelector(bypassList.replace(';', ','), ps);
		}
		return ps;
	}

	/*************************************************************************
	 * Installs a fallback selector that is used whenever no protocol specific
	 * selector is defined.
	 *
	 * @param settings
	 *            to take the proxy settings from.
	 * @param ps
	 *            to install the created selector on.
	 ************************************************************************/

	private static void addFallbackSelector(Properties settings, ProtocolDispatchSelector ps) {
		String proxy = settings.getProperty("default");
		if (proxy != null) {
			ps.setFallbackSelector(ProxyUtil.parseProxySettings(proxy));
		}
	}

	/*************************************************************************
	 * Creates a selector for a given protocol. The proxy will be taken from the
	 * settings and installed on the dispatch selector.
	 *
	 * @param settings
	 *            to take the proxy settings from.
	 * @param protocol
	 *            to create a selector for.
	 * @param ps
	 *            to install the created selector on.
	 ************************************************************************/

	private static void addSelectorForProtocol(Properties settings, String protocol, ProtocolDispatchSelector ps) {
		String proxy = settings.getProperty(protocol);
		if (proxy != null) {
			FixedProxySelector protocolSelector = ProxyUtil.parseProxySettings(proxy);
			ps.setSelector(protocol, protocolSelector);
		}
	}


	/*************************************************************************
	 * Parses the proxy list and splits it by protocol.
	 *
	 * @param proxyString
	 *            the proxy list string
	 * @return Properties with separated settings.
	 ************************************************************************/

	public static Properties parseProxyList(String proxyString){
		Properties p = new Properties();
		if (proxyString.indexOf('=') == -1) {
			p.setProperty("default", proxyString);
		} else {
			try {
				proxyString = proxyString.replace(';', '\n');
				p.load(new ByteArrayInputStream(proxyString.getBytes("ISO-8859-1")));
			} catch (IOException e) {
				Logger.log(ProxyUtil.class, LogLevel.ERROR, "Error reading IE settings as properties: {0}", e);
			}
		}
		return p;
	}

	public static ProtocolDispatchSelector buildProtocolDispatchSelector(Properties properties) {
		ProtocolDispatchSelector ps = new ProtocolDispatchSelector();
		addSelectorForProtocol(properties, "http", ps);
		addSelectorForProtocol(properties, "https", ps);
		addSelectorForProtocol(properties, "ftp", ps);
		addSelectorForProtocol(properties, "gopher", ps);

		// these are the "default" settings, which may be overridden by "socks" (below)
		addFallbackSelector(properties, ps);

		// "socks" is a special case: it can be used as a fallback for the protocols above (e.g. http),
		// so it must be specified as such (URLs won't specify socks:// that addSelectorForProtocol() would
		// use as lookup key).
		String socksProperties = properties.getProperty("socks");
		if (socksProperties != null) {
			String[] hostAndPort = socksProperties.split(":");
			String host = "";
			int port = 0;
			if (hostAndPort.length > 0) {
				host = hostAndPort[0];
			}
			if (hostAndPort.length > 1) {
				try {
					port = Integer.parseInt(hostAndPort[1]);
				} catch (NumberFormatException e) {
					Logger.log(ProxyUtil.class, LogLevel.WARNING, "Cannot parse SOCKS proxy port {0}", hostAndPort[1]);
				}
			}
			ps.setFallbackSelector(new FixedSocksSelector(host, port));
		}

		return ps;
	}

}
