package com.github.markusbernhardt.proxy.jna.win;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * WinHttp.dll Interface.
 * 
 * @author Markus Bernhardt, Copyright 2016
 */
public interface WinHttp extends StdCallLibrary {
	/**
	 * The WinHttpDetectAutoProxyConfigUrl function finds the URL for the Proxy
	 * Auto-Configuration (PAC) file. This function reports the URL of the PAC
	 * file, but it does not download the file.
	 * 
	 * @param dwAutoDetectFlags
	 *            A data type that specifies what protocols to use to locate the
	 *            PAC file. If both the DHCP and DNS auto detect flags are set,
	 *            DHCP is used first; if no PAC URL is discovered using DHCP,
	 *            then DNS is used. Set {@code WINHTTP_AUTO_DETECT_TYPE_DHCP},
	 *            {@code WINHTTP_AUTO_DETECT_TYPE_DNS_A} or both.
	 * @param ppwszAutoConfigUrl
	 *            A data type that returns a pointer to a null-terminated
	 *            Unicode string that contains the configuration URL that
	 *            receives the proxy data. You must free the string pointed to
	 *            by ppwszAutoConfigUrl using the GlobalFree function.
         * 
	 * @return {@code true} if successful; otherwise, {@code false}.
         * @see WinHttpHelpers#detectAutoProxyConfigUrl
	 */
	boolean WinHttpDetectAutoProxyConfigUrl(
                WinDef.DWORD dwAutoDetectFlags, 
            WTypes2.LPWSTRByReference ppwszAutoConfigUrl) throws LastErrorException;

	/**
	 * The WinHttpGetDefaultProxyConfiguration function retrieves the default
	 * WinHTTP proxy configuration from the registry.
	 * 
	 * @param pProxyInfo
	 *            A pointer to a variable of type WINHTTP_PROXY_INFO that
	 *            receives the default proxy configuration.
	 * @return {@code true} if successful; otherwise, {@code false}.
	 */
	boolean WinHttpGetDefaultProxyConfiguration(WinHttpProxyInfo pProxyInfo);

	/**
	 * The WinHttpGetIEProxyConfigForCurrentUser function retrieves the Internet
	 * Explorer proxy configuration for the current user.
	 * 
	 * @param pProxyConfig
	 *            A pointer, on input, to a WINHTTP_CURRENT_USER_IE_PROXY_CONFIG
	 *            structure. On output, the structure contains the Internet
	 *            Explorer proxy settings for the current active network
	 *            connection (for example, LAN, dial-up, or VPN connection).
	 * @return {@code true} if successful; otherwise, {@code false}.
	 */
	boolean WinHttpGetIEProxyConfigForCurrentUser(WinHttpCurrentUserIEProxyConfig pProxyConfig);

}
