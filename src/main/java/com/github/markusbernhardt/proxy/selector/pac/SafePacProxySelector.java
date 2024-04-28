package com.github.markusbernhardt.proxy.selector.pac;

import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * {@link SafePacProxySelector} strips sensitive parts of the URI provided for selection for security reasons.
 * <p/>
 * <a href="https://www.cve.org/CVERecord?id=CVE-2016-5134">CVE-2016-5134</a>
 */
public class SafePacProxySelector extends PacProxySelector {
    /*************************************************************************
     * Constructor
     *
     * @param pacSource
     *            the source for the PAC file.
     ***********************************************************************
     */
    public SafePacProxySelector(PacScriptSource pacSource) {
        super(pacSource);
    }

    @Override
    public List<Proxy> select(URI uri) {
        URI safeUri = sanitizeURI(uri);
        return super.select(safeUri);
    }

    public static URI sanitizeURI(URI uri) {
        try {
            if ("https".equals(uri.getScheme()) || "wss".equals(uri.getScheme())) {
                return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), "/", null, null);
            } else {
                return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // never expected to be thrown
        }
    }
}
