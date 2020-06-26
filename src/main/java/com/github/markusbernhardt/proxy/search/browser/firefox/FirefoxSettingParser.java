package com.github.markusbernhardt.proxy.search.browser.firefox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Properties;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import com.github.markusbernhardt.proxy.util.Logger;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/*****************************************************************************
 * Parser for the Firefox settings file. Will extract all relevant proxy
 * settings form the configuration file.
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
	 * Reads the profile.ini, searches for the profiles directory and returns a file
	 * object pointing to the settings file.
	 * 
	 * @param source of the Firefox profiles.
	 * @return {@link File} object pointing to the settings file
	 * @throws IOException on read error.
	 */
	private File getSettingsFile(FirefoxProfileSource source) throws IOException {
		// Read profiles.ini
		File profilesIniFile = source.getProfilesIni();
		if (profilesIniFile.exists()) {
			Ini profilesIni = new Ini(profilesIniFile);
			for (Entry<String, Section> entry : profilesIni.entrySet()) {
				if ("default".equals(entry.getValue().get("Name")) && "1".equals(entry.getValue().get("IsRelative"))) {
					File profileFolder = new File(profilesIniFile.getParentFile().getAbsolutePath(), entry.getValue().get("Path"));
					Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder is {}", profileFolder);

					File settingsFile = new File(profileFolder, "prefs.js");
					return settingsFile;
				}
			}
		}
		Logger.log(getClass(), LogLevel.DEBUG, "Firefox settings folder not found!");
		return null;
	}

}
