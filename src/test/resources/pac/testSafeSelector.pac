function FindProxyForURL(url, host) {
    if (shExpMatch(url, "https://sub.domain1.invalid/")) {
        return "PROXY http_proxy.unit-test.invalid:8090";
    }
    if (shExpMatch(url, "wss://sub.domain1.invalid/")) {
        return "PROXY http_proxy.unit-test.invalid:8090";
    }

    if (shExpMatch(url, "http://sub.domain2.invalid/path/*?q=a")) {
        return "PROXY http_proxy.unit-test.invalid:8090";
    }

    return "DIRECT";
}