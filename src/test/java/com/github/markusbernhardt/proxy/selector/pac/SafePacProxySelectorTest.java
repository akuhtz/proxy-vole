package com.github.markusbernhardt.proxy.selector.pac;

import com.github.markusbernhardt.proxy.TestUtil;
import org.junit.jupiter.api.Test;

import java.net.Proxy;
import java.net.URI;
import java.util.List;

import static com.github.markusbernhardt.proxy.selector.pac.PacProxySelectorTest.getTestPacScriptSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SafePacProxySelectorTest {
    SafePacProxySelector pacProxySelector = new SafePacProxySelector(getTestPacScriptSource("testSafeSelector.pac"));

    @Test
    public void testStrippedHttpsGetsProxied() {
        assertGetsProxied("https://sub.domain1.invalid");
        assertGetsProxied("https://sub.domain1.invalid/path/?query#fragment");
        assertGetsProxied("https://user:password@sub.domain1.invalid/");

        assertDoesntGetProxied("https://sub.different-domain1.invalid/");
    }

    @Test
    public void testStrippedWssGetsProxied() {
        assertGetsProxied("wss://sub.domain1.invalid");
        assertGetsProxied("wss://sub.domain1.invalid/path/?query#fragment");
        assertGetsProxied("wss://user:password@sub.domain1.invalid/");

        assertDoesntGetProxied("wss://sub.different-domain1.invalid/");
    }

    @Test
    public void testStrippedHttpGetsProxied() {
        assertGetsProxied("http://sub.domain2.invalid/path/something?q=a");
        assertGetsProxied("http://user:password@sub.domain2.invalid/path/?q=a");

        assertDoesntGetProxied("http://sub.domain2.invalid/path/?q=different");
        assertDoesntGetProxied("http://sub.domain2.invalid");
        assertDoesntGetProxied("http://sub.domain2.invalid/different");
    }

    @Test
    public void sanitizeWorksAsExpectedForCryptographicSchemes() {
        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("https://u:p@sub.domain.com:99/path?q=a#f")), URI.create("https://sub.domain.com:99/"));
        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("https://sub.domain.com")), URI.create("https://sub.domain.com/"));

        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("wss://u:p@sub.domain.com:99/path?q=a#f")), URI.create("wss://sub.domain.com:99/"));
        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("wss://sub.domain.com")), URI.create("wss://sub.domain.com/"));
    }

    @Test
    public void sanitizeWorksAsExpectedForNonCryptographicSchemes() {
        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("http://u:p@sub.domain.com:99/path?q=a#f")), URI.create("http://sub.domain.com:99/path?q=a"));
        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("http://sub.domain.com")), URI.create("http://sub.domain.com"));

        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("ftp://u:p@sub.domain.com:99/path?q=a#f")), URI.create("ftp://sub.domain.com:99/path?q=a"));
        assertEquals(SafePacProxySelector.sanitizeURI(URI.create("ftp://sub.domain.com")), URI.create("ftp://sub.domain.com"));
    }

    void assertGetsProxied(String url) {
        List<Proxy> results = pacProxySelector.select(URI.create(url));
        assertEquals(1, results.size());
        assertEquals(TestUtil.HTTP_TEST_PROXY, results.get(0));
    }

    void assertDoesntGetProxied(String url) {
        List<Proxy> results = pacProxySelector.select(URI.create(url));
        assertEquals(1, results.size());
        assertEquals(Proxy.NO_PROXY, results.get(0));
    }
}
