package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.File;

import com.github.markusbernhardt.proxy.util.PlatformUtil;

/*****************************************************************************
 * Searches for Firefox profile on an Linux / Unix base system. This will scan the <i>.mozilla</i> folder in the users
 * home directory to find the profiles.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

class LinuxFirefoxProfileSource implements FirefoxProfileSource {

    /*************************************************************************
     * Get profiles.ini for the Linux Firefox profile
     ************************************************************************/

    @Override
    public File getProfilesIni() {
        File userDir = new File(PlatformUtil.getUserHomeDir());
        return new File(userDir, ".mozilla" + File.separator + "firefox" + File.separator + "profiles.ini");
    }

}
