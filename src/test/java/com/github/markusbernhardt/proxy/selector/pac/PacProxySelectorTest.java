package com.github.markusbernhardt.proxy.selector.pac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.markusbernhardt.proxy.TestUtil;

/*****************************************************************************
 * Tests for the Pac script parser and proxy selector.
 * 
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

public class PacProxySelectorTest {

    /*************************************************************************
     * Test method
     *
     ************************************************************************/
    @Test
    public void testScriptExecution() {
        List<Proxy> result = new PacProxySelector(getTestPacScriptSource("test1.pac")).select(TestUtil.HTTP_TEST_URI);

        assertEquals(TestUtil.HTTP_TEST_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test method
     *
     ************************************************************************/
    @Test
    public void testScriptExecution2() {
        PacProxySelector pacProxySelector = new PacProxySelector(getTestPacScriptSource("test2.pac"));
        List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));

        result = pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
        assertEquals(Proxy.NO_PROXY, result.get(0));
    }

    /*************************************************************************
     * Test download fix to prevent infinite loop.
     *
     ************************************************************************/
    @Test
    public void pacDownloadFromURLShouldNotUseProxy() {
        ProxySelector oldOne = ProxySelector.getDefault();
        try {
            ProxySelector.setDefault(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    throw new IllegalStateException("Should not download via proxy");
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    // Not used
                }
            });

            PacProxySelector pacProxySelector =
                new PacProxySelector(new UrlPacScriptSource("http://www.test.invalid/wpad.pac"));
            pacProxySelector.select(TestUtil.HTTPS_TEST_URI);
        }
        finally {
            ProxySelector.setDefault(oldOne);
        }
    }

    /*************************************************************************
     * Test method
     *
     ************************************************************************/
    @Test
    public void testScriptMuliProxy() {
        PacProxySelector pacProxySelector = new PacProxySelector(getTestPacScriptSource("testMultiProxy.pac"));
        List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
        assertEquals(4, result.size());
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy.com", 80)), result.get(0));
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy2.com", 8080)), result.get(1));
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy3.com", 8080)), result.get(2));
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy4.com", 80)), result.get(3));
    }

    /*************************************************************************
     * Test method
     *
     ************************************************************************/
    @Test
    public void testScriptProxyTypes() {
        PacProxySelector pacProxySelector = new PacProxySelector(getTestPacScriptSource("testProxyTypes.pac"));
        List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
        assertEquals(6, result.size());
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy.com", 80)), result.get(0));
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy2.com", 80)), result.get(1));
        assertEquals(new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("my-proxy3.com", 486)), result.get(2));
        assertEquals(new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved("my-proxy4.com", 80)), result.get(3));
        assertEquals(new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved("my-proxy5.com", 80)), result.get(4));
        assertEquals(new Proxy(Type.SOCKS, InetSocketAddress.createUnresolved("my-proxy6.com", 80)), result.get(5));
    }

    /*************************************************************************
     * Test method for the override local IP feature.
     *
     ************************************************************************/
    @Test
    public void testLocalIPOverride() {
        System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "123.123.123.123");
        try {
            PacProxySelector pacProxySelector = new PacProxySelector(getTestPacScriptSource("testLocalIP.pac"));
            List<Proxy> result = pacProxySelector.select(TestUtil.HTTP_TEST_URI);
            assertEquals(result.get(0),
                new Proxy(Type.HTTP, InetSocketAddress.createUnresolved("123.123.123.123", 8080)));
        }
        finally {
            System.setProperty(PacScriptMethods.OVERRIDE_LOCAL_IP, "");
        }

    }

    @Test
    void testBuildProxyFromPacResultIpv4() {
        final PacProxySelector pacProxySelector = new PacProxySelector(getTestPacScriptSource("testProxyTypes.pac"));
        String url = "PROXY my-proxy.com:80";
        Proxy proxyFromPacResult = pacProxySelector.buildProxyFromPacResult(url);

        InetSocketAddress addr = (InetSocketAddress) proxyFromPacResult.address();

        assertEquals("my-proxy.com", addr.getHostString());
        assertEquals(80, addr.getPort());
    }

    @Test
    void testBuildProxyFromPacResultIpv6() {
        final PacProxySelector pacProxySelector = new PacProxySelector(getTestPacScriptSource("testProxyTypes.pac"));
        String url = "PROXY [::1]:8080";
        Proxy proxyFromPacResult = pacProxySelector.buildProxyFromPacResult(url);
        InetSocketAddress addr = (InetSocketAddress) proxyFromPacResult.address();

        assertEquals("[::1]", addr.getHostString());
        assertEquals(8080, addr.getPort());
    }

    /*************************************************************************
     * Helper method to build the url to the given test file
     * 
     * @param testFile
     *            the name of the test file.
     * @return the URL.
     ************************************************************************/
    private static String toUrl(String testFile) throws MalformedURLException {
        return new File(TestUtil.TEST_DATA_FOLDER + "pac", testFile).toURI().toURL().toString();
    }

    static UrlPacScriptSource getTestPacScriptSource(String testFile) {
        try {
            return new UrlPacScriptSource(toUrl(testFile));
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e); // not expected to be thrown
        }
    }
}
