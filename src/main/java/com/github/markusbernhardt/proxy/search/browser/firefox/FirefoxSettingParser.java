package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.sshtools.jini.INI;
import com.sshtools.jini.INI.Section;
import com.sshtools.jini.INIReader;
import com.sshtools.jini.INIParseException;

import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

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

    public Properties parseSettings(FirefoxProfileSource source) throws IOException {
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
    protected File getSettingsFile(FirefoxProfileSource source) throws IOException {
        // Read profiles.ini
        File profilesIniFile = source.getProfilesIni();
        if (profilesIniFile.exists()) {

            try (FileReader fileReader = new FileReader(profilesIniFile)) {
                final INI profilesIni = new INIReader.Builder().build().read(fileReader);

                final List<String> keysFF67 =
                    profilesIni.sections().keySet().stream().filter(s -> s.startsWith("Install")).collect(Collectors.toList());
                if (!keysFF67.isEmpty()) {
                    Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings for FF67+ detected.");

                    for (String keyFF67 : keysFF67) {

                        Logger.log(getClass(), LogLevel.DEBUG, "Current FF67+ section key is: {}", keysFF67);
                        Section section = profilesIni.section(keyFF67);

                        String propLocked = section.getOr("Locked").orElse(null);
                        if (propLocked != null && "1".equals(propLocked)) {
                            String propDefault = section.getOr("Default").orElse(null);
                            if (propDefault != null) {
                              File profileFolder =
                                  new File(profilesIniFile.getParentFile().getAbsolutePath(), propDefault);
                              Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder is {}", profileFolder);

                              File settingsFile = new File(profileFolder, "prefs.js");
                              return settingsFile;
                            }
                        }
                    }
                }
                else {  // no sections starting "Install" found, older version than FF67+ detected
                    for (Map.Entry<String, Section[]> entry : profilesIni.sections().entrySet()) {
                        String section = entry.getKey();
                        Section confSection = entry.getValue()[0];

                        Logger
                            .log(getClass(), LogLevel.TRACE, "Current entry, key: {}, value: {}", section,
                                confSection.asString());

                        String propName = confSection.getOr("Name").orElse(null);
                        String propRelative = confSection.getOr("IsRelative").orElse(null);
                        if (propName != null && propRelative != null) {
                            if ("default".equals(propName)
                                && "1".equals(propRelative)) {
                                String propPath = confSection.getOr("Path").orElse(null);
                                if (propPath != null) {
                                    File profileFolder =
                                        new File(profilesIniFile.getParentFile().getAbsolutePath(), propPath);
                                    Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder is {}", profileFolder);

                                    File settingsFile = new File(profileFolder, "prefs.js");
                                    return settingsFile;
                                }
                            }
                        }
                    }
                }
            } catch (INIParseException e) {
                throw new IOException("Error parsing the Firefox profiles.ini file", e);
            }
        }
        Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder not found!");
        return null;
    }

}
