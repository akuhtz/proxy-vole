package com.github.markusbernhardt.proxy.selector.misc;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListProxySelector extends ProxySelector {
  private final List<ProxySelector> selectors;
  private final ProxySelector fallbackSelector;

  public ListProxySelector(List<ProxySelector> selectors, ProxySelector fallbackSelector) {
    this.selectors = selectors;
    this.fallbackSelector = fallbackSelector;
  }

  @Override
  public List<Proxy> select(URI uri) {
    List<Proxy> proxies = new ArrayList<Proxy>();
    for (ProxySelector selector : selectors) {
      List<Proxy> l = selector.select(uri);
      if (l != null && l.size() > 0) {
        proxies.addAll(l);
      }
    }

    if (proxies.isEmpty() && fallbackSelector != null) {
      proxies.addAll(fallbackSelector.select(uri));
    }

    proxies.add(Proxy.NO_PROXY);

    return removeDuplicates(proxies);
  }

  private List<Proxy> removeDuplicates(List<Proxy> proxies) {
    List<Proxy> l = new ArrayList<Proxy>(proxies.size());
    Set<Proxy> s = new HashSet<Proxy>();
    for (Proxy proxy : proxies) {
      if (s.contains(proxy)) continue;
      l.add(proxy);
      s.add(proxy);
    }
    return l;
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    for (ProxySelector selector : selectors) {
      selector.connectFailed(uri, sa, ioe);
    }
    if (fallbackSelector != null){
      fallbackSelector.connectFailed(uri, sa, ioe);
    }
  }
}
