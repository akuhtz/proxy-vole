package com.github.markusbernhardt.proxy.jna.win;

import com.github.markusbernhardt.proxy.util.Logger;
import com.sun.jna.LastErrorException;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.PointerByReference;

/**
 * Static helper methods for Windows {@code WinHttp} functions.
 * 
 * @author phansson
 */
public class WinHttpHelpers {
	
	/**
	 * Returned if WinHTTP was unable to discover the URL of the Proxy
	 * Auto-Configuration (PAC) file using the WPAD method.
	 */
	private static final int ERROR_WINHTTP_AUTODETECTION_FAILED = 12180;
	
    private WinHttpHelpers() {
    }

    /**
     * Finds the URL for the Proxy Auto-Configuration (PAC) file using WPAD.
     *
     * <p>
     * This method is blocking and may take some time to execute.
     * 
     * @param dwAutoDetectFlags flags for auto detection
     * @return the url of the PAC file or {@code null} if it cannot be located
     *         using WPAD method.
     */
    public static String detectAutoProxyConfigUrl(WinDef.DWORD dwAutoDetectFlags) {

        PointerByReference ppwszAutoConfigUrl = new PointerByReference();
        boolean result = false;
        try {
            result = WinHttp.INSTANCE.WinHttpDetectAutoProxyConfigUrl(dwAutoDetectFlags, ppwszAutoConfigUrl);

            if (result) {
                Pointer h = ppwszAutoConfigUrl.getValue();
                return h == null ? null : h.getWideString(0);
            } else {
                return null;
            }

        } catch (LastErrorException ex) {

            Logger.log(WinHttpHelpers.class, Logger.LogLevel.ERROR,
                    "Windows function WinHttpDetectAutoProxyConfigUrl returned error : {}", ex.getErrorCode());

            if (ex.getErrorCode() == ERROR_WINHTTP_AUTODETECTION_FAILED) {
                // This error is to be expected. It just means that the lookup
                // using either DHCP, DNS or both, failed because there wasn't
                // a useful reply from DHCP / DNS. (meaning the site hasn't
                // configured their DHCP Server or their DNS Server for WPAD)
                return null;
            }
            // Something more serious is wrong. There isn't much we can do
            // about it but at least we would like to log it.
            Logger.log(WinHttpHelpers.class, Logger.LogLevel.ERROR,
                    "Windows function WinHttpDetectAutoProxyConfigUrl returned error : {}", ex.getMessage());
            return null;
        }
        finally {
            freeGlobalMemory(ppwszAutoConfigUrl.getValue());
        }
    }

    public static void freeGlobalMemory(Pointer p) {
        if (p != null) {
            Pointer free = Kernel32.INSTANCE.GlobalFree(p);
            if (free != null) {
                // The call to GlobalFree has failed. This should never
                // happen. If it really does happen, there isn't much we
                // can do about it other than logging it.
                Logger.log(WinHttpHelpers.class, Logger.LogLevel.ERROR,
                    "Windows function GlobalFree failed while freeing memory");
            }
        }
    }

    public static String getAndFreeGlobalString(WTypes.LPWSTR h) {
        if (h == null || h.getPointer() == null) {
            return null;
        }

        try {
            return h.getValue();
        } finally {
            freeGlobalMemory(h.getPointer());
            h.setPointer(null);
        }
    }

    @SuppressWarnings("unused")
    private String sanitizeUrl(String urlStr) {
        String u = urlStr.trim();
        int pos = u.indexOf('\n');
        if (pos == 0) {
            return "";
        }
        if (pos > 0) {
            return u.substring(0, pos);
        }
        return u;
    }
}
