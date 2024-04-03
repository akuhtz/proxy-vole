package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.ex.ConfigurationException;

import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;
import java.io.*;

/*****************************************************************************
 * Parser for the Firefox settings file. Will extract all relevant proxy settings form the configuration file.
 *
 * @author Markus Bernhardt, Copyright 2016
 * @author Bernd Rosstauscher, Copyright 2009
 ****************************************************************************/

class FirefoxSettingParser {

    /*************************************************************************
     * Constructor
     ************************************************************************/

    public FirefoxSettingParser() {
        super();
    }

    /*************************************************************************
     * Parse the settings file and extract all network.proxy.* settings from it.
     * 
     * @param source
     *            of the Firefox profiles.
     * @return the parsed properties.
     * @throws IOException
     *             on read error.
     ************************************************************************/

    public Properties parseSettings(FirefoxProfileSource source) throws IOException, ConfigurationException {
        File settingsFile = getSettingsFile(source);

        Properties result = new Properties();
        if (settingsFile == null) {
            return result;
        }

        try (BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(settingsFile)))) {
            String line;
            while ((line = fin.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("user_pref(\"network.proxy")) {
                    line = line.substring(10, line.length() - 2);
                    int index = line.indexOf(",");
                    String key = removeDoubleQuotes(line.substring(0, index).trim());
                    String value = removeDoubleQuotes(line.substring(index + 1).trim());
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    /**
     * Removes leading and trailing double quotes.
     * 
     * @param string
     * @return
     */
    private String removeDoubleQuotes(String string) {
        if (string.startsWith("\"")) {
            string = string.substring(1);
        }
        if (string.endsWith("\"")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    /**
     * Reads the profile.ini, searches for the profiles directory and returns a file object pointing to the settings
     * file.
     * 
     * @param source
     *            of the Firefox profiles.
     * @return {@link File} object pointing to the settings file
     * @throws IOException
     *             on read error.
     */
    protected File getSettingsFile(FirefoxProfileSource source) throws IOException, ConfigurationException {
        // Read profiles.ini
        File profilesIniFile = source.getProfilesIni();
        if (profilesIniFile.exists()) {
            INIConfiguration profilesIni = new INIConfiguration();
            try (FileReader fileReader = new FileReader(profilesIniFile)) {
                profilesIni.read(fileReader);

                final List<String> keysFF67 =
                    profilesIni.getSections().stream().filter(s -> s.startsWith("Install")).collect(Collectors.toList());
                if (!keysFF67.isEmpty()) {
                    Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings for FF67+ detected.");

                    for (String keyFF67 : keysFF67) {

                        Logger.log(getClass(), LogLevel.DEBUG, "Current FF67+ section key is: {}", keysFF67);
                        SubnodeConfiguration section = profilesIni.getSection(keyFF67);

                        Object propLocked = section.getProperty("Locked");
                        if ((propLocked!=null)&&("1".equals(propLocked.toString()))) {
                            Object propDefault = section.getProperty("Default");
                            if (propDefault!=null) {
                              File profileFolder =
                                  new File(profilesIniFile.getParentFile().getAbsolutePath(), propDefault.toString());
                              Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder is {}", profileFolder);

                              File settingsFile = new File(profileFolder, "prefs.js");
                              return settingsFile;
                            }
                        }
                    }
                }
                else {  //FIXME - does this mean we have no sections in pre FF67 ini files? or just no sections starting "Install"?
                    for (String section : profilesIni.getSections()) {
                        SubnodeConfiguration confSection = profilesIni.getSection(section);
                        
                        if (confSection!=null) {
                            Logger
                                .log(getClass(), LogLevel.TRACE, "Current entry, key: {}, value: {}", section,
                                    confSection.toString());

                            Object propName = confSection.getProperty("Name");
                            Object propRelative = confSection.getProperty("IsRelative");
                            if ((propName!=null)&&(propRelative!=null)) {
                                if ("default".equals(propName.toString())
                                    && "1".equals(propRelative.toString())) {
                                    Object propPath = confSection.getProperty("Path");
                                    if (propPath!=null) {
                                        File profileFolder =
                                            new File(profilesIniFile.getParentFile().getAbsolutePath(), propPath.toString());
                                        Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder is {}", profileFolder);

                                        File settingsFile = new File(profileFolder, "prefs.js");
                                        return settingsFile;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder not found!");
        return null;
    }

}
