/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.locale
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.locale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

import org.appwork.storage.ConfigInterface;
import org.appwork.storage.Storage;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

/**
 * This class provides functions to return translated strings
 * 
 * @author Christian
 */
public class Loc {

    public static final Storage CFG = ConfigInterface.getStorage("Locale");
    /**
     * The key (String) under which the saved localization-name is stored.
     */
    public static final String PROPERTY_LOCALE = "PROPERTY_LOCALE2";

    /**
     * The directory, where all localization files are located.
     */
    public static final File LOCALIZATION_DIR = Application.getRessource("languages/");

    /**
     * The name of the default localization file. This is the english language.
     */
    private static final String FALLBACK_LOCALE = "en_GB";

    /**
     * The default localization file. This is the english language.
     */
    private static final File DEFAULT_LOCALIZATION = new File(LOCALIZATION_DIR, getDefaultLocale() + ".loc");

    /**
     * The HashMap which contains all hashcodes of the keys and their translated
     * values.
     * 
     * @see Loc#parseLocalization(RFSFile)
     */
    private static HashMap<Integer, String> data = null;

    private static String locale;
    private static String DEFAULT_LOCALE_CACHE;

    /**
     * Returns the translated value for the translation-key. If the current
     * language file doesn't contain the translated value, the default value
     * will be returned.
     * 
     * @param key
     *            key for the translation in the language file. the key should
     *            <b>always</b> have the following structure
     *            <i>PACKAGE_NAME_FROM_CALLER.CLASS_NAME_FROM_CALLER.key</i>
     * @param def
     *            default value which will be returned if there is no mapping
     *            for the key
     * @return translated value or the def parameter
     * @see Loc#LF(String, String, Object...)
     * @throws IllegalArgumentException
     *             if the key is null or is empty
     */
    public static String L(String key, String def) {
        if (key == null || (key = key.trim()).length() == 0) throw new IllegalArgumentException();
        if (data == null) {
            Log.L.warning("No parsed localization found! Loading now from saved localization file!");
            try {
                Loc.setLocale(CFG.get(PROPERTY_LOCALE, FALLBACK_LOCALE));
            } catch (Exception e) {

                Log.L.severe("Error while loading the stored localization name!");
                Loc.setLocale(FALLBACK_LOCALE);
            }
            if (data == null) return def == null ? "Error in Loc! No loaded data!" : def;
        }

        String loc = data.get(key.toLowerCase().hashCode());
        if (loc == null) {
            data.put(key.toLowerCase().hashCode(), def);
            return def;
        }
        return loc;
    }

    /**
     * Returns the translated value for the translation-key filled with the
     * parameters.
     * 
     * @param key
     *            key for the translation in the language file. the key should
     *            <b>always</b> have the following structure
     *            <i>PACKAGE_NAME_FROM_CALLER.CLASS_NAME_FROM_CALLER.key</i>
     * @param def
     *            default value which will be returned if there is no mapping
     *            for the key
     * @param args
     *            parameters which should be inserted in the translated string
     * @return translated value or the def parameter filled with the parameters
     * @see Loc#L(String, String)
     */
    public static String LF(String key, String def, Object... args) {
        try {
            return String.format(L(key, def), args);
        } catch (Exception e) {
            return "Error: " + key;
        }
    }

    /**
     * Set-up this class by creating the HashMap for the key-string-pairs.
     * 
     * @param loc
     *            name of the localization file
     * @see Loc#parseLocalization(RFSFile)
     */
    public static void setLocale(String loc) {
        try {
            if (loc == null) {

                loc = CFG.get(PROPERTY_LOCALE, getDefaultLocale());
            }
            File file = new File(LOCALIZATION_DIR, loc + ".loc");
            locale = loc;
            if (file != null && file.exists()) {

                String[] locs = loc.split("_");
                if (locs.length == 1) {
                    Locale.setDefault(new Locale(locs[0]));
                } else {
                    Locale.setDefault(new Locale(locs[0], locs[1]));
                }
                CFG.put(PROPERTY_LOCALE, loc);
                Loc.parseLocalization(file);
            } else {
                Log.L.info("The language " + loc + " isn't available! Parsing default (" + FALLBACK_LOCALE + ".loc) one!");
                locale = getDefaultLocale();
                String[] locs = locale.split("_");
                Locale.setDefault(new Locale(locs[0], locs[1]));
                Loc.parseLocalization(DEFAULT_LOCALIZATION);
            }
        } catch (Exception e) {
            org.appwork.utils.logging.Log.exception(e);
        }
    }

    /**
     * @return
     */
    private static String getDefaultLocale() {
        if (DEFAULT_LOCALE_CACHE != null) return DEFAULT_LOCALE_CACHE;
        String sys = System.getProperty("user.language").toLowerCase();
        String cou = System.getProperty("user.country").toUpperCase();

        String[] locs = getLocales();
        if (locs.length == 0) {
            DEFAULT_LOCALE_CACHE = FALLBACK_LOCALE;

        }
        if (DEFAULT_LOCALE_CACHE == null) {
            for (String l : locs) {

                if (l.equals(sys + "_" + cou)) {
                    DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }
        if (DEFAULT_LOCALE_CACHE == null) {
            for (String l : locs) {

                if (l.equals(sys)) {
                    DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }
        if (DEFAULT_LOCALE_CACHE == null) {
            for (String l : locs) {
                if (l.startsWith(sys + "_")) {
                    DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }

        if (DEFAULT_LOCALE_CACHE == null) {
            for (String l : locs) {
                if (l.equals(FALLBACK_LOCALE)) {
                    DEFAULT_LOCALE_CACHE = l;
                    break;
                }
            }
        }
        if (DEFAULT_LOCALE_CACHE == null) DEFAULT_LOCALE_CACHE = locs[0];
        return DEFAULT_LOCALE_CACHE;
    }

    /**
     * Creates a HashMap with the data obtained from the localization file. <br>
     * <b>Warning:</b> Overwrites any previously created HashMap
     * 
     * @param file
     *            {@link RFSFile} object to the localization file
     * @throws IllegalArgumentException
     *             if the parameter is null or doesn't exist
     * @see Loc#data
     */
    public static void parseLocalization(File file) throws IllegalArgumentException {
        if (file == null || !file.exists()) throw new IllegalArgumentException();

        if (data != null) Log.L.finer("Previous HashMap will be overwritten!");
        data = new HashMap<Integer, String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            String line;
            String key;
            String value;
            int split;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;

                if ((split = line.indexOf('=')) <= 0) continue;

                key = line.substring(0, split).toLowerCase().trim();
                value = line.substring(split + 1).trim();
                value = value.replace("\\n", "\n").replace("\\r", "\r");

                data.put(key.hashCode(), value);
            }
        } catch (Exception e) {
            org.appwork.utils.logging.Log.exception(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    org.appwork.utils.logging.Log.exception(e);
                }
            }
        }
    }

    /**
     * Returns a localized regular expression for words that usualy ar present
     * in an error message
     * 
     * @return
     */
    public static String getErrorRegex() {
        return L("system.error", ".*(error|failed).*");
    }

    /**
     * @return
     */
    public static String[] getLocales() {
        String[] files = LOCALIZATION_DIR.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".loc");

            }

        });
        if (files == null) return new String[] {};
        for (int i = 0; i < files.length; i++) {
            files[i] = files[i].replace(".loc", "");
        }
        return files;
    }

    /**
     * @return
     */
    public static String getLocale() {

        return locale;
    }

}
