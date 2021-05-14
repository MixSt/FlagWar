/*
 * Copyright (c) 2021 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar.i18n;

import io.github.townyadvanced.flagwar.FlagWar;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class LocaleUtil {
    /** Holds the currentLocale. */
    private static Locale currentLocale;
    /** Holds the messages ResourceBundle. */
    private static ResourceBundle messages;

    private LocaleUtil() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Method to load FlagWar's Locale and the appropriate ResourceBundle. Also handles fallback to en_US.
     * <p>
     * Capable of handling various formats of locales, segmented by locale, region, and variant.
     * ie: en, en_US, en_US-POSIX
     * <p>
     * locale regex: [a-zA-Z]{2,8}
     * region regex: [a-zA-Z]{2} (-OR-) [0-9]{3} (Always preceded with underscore.)
     * variant regex: [0-9][0-9a-zA-Z]{3} (-OR-) [0-9a-zA-Z]{5,8} (Preceded with either underscore or hyphen.)
     *
     * @param localeString the String to be parsed for loading the locale.
     */
    public static void setUpLocale(final String localeString) {
        var logger = FlagWar.getInstance().getLogger();
        var defaultLocale = new Locale("en", "US");
        String language;
        String region;
        String variant;
        Locale locale;

        // Regular Expressions for localeString parsing.
        var localeRegEx = "[a-zA-Z]{2,8}";
        String regionRegEx = localeRegEx + "_([a-zA-Z]{2}|[0-9]{3})";
        String variantRegEx = regionRegEx + "[_-]([0-9][0-9a-zA-Z]{3}|[0-9a-zA-Z]{5,8})";

        // RegEx Evaluation and variable assignment (Fallback >> Most Complex >> Most Simple)
        if (localeString.isEmpty() || !fileInJar(localeString)) {
            locale = defaultLocale;
            logger.severe("Locale is undefined or is not in FlagWar. Defaulting!");
        } else if (localeString.matches(variantRegEx)) { // Locale + Region + Variant (en_US-POSIX)
            language = localeString.substring(0, localeString.indexOf("_"));
            region = localeString.substring(localeString.indexOf("_"));
            // Parse the variant out of the region, and realign region. Well this doesn't sound racist at all...
            variant = parseVariant(region);
            region = parseRegion(region);
            locale = new Locale(language, region, variant);
        } else if (localeString.matches(regionRegEx)) { // Locale + Region (en_US, fr_CA)
            language = localeString.substring(0, localeString.indexOf("_"));
            region = localeString.substring(localeString.indexOf("_"));
            locale = new Locale(language, region);
        } else if (localeString.matches(localeRegEx)) { // Locale Only (en / english)
            locale = new Locale(localeString);
        } else {
            logger.severe("Defaulting because something went wrong while assigning the locale!");
            locale = defaultLocale;
        }
        finalizeSetup(logger, locale);
    }

    private static void finalizeSetup(final Logger logger, final Locale locale) {
        setLocale(locale);
        var msg = ResourceBundle.getBundle("Translation", getLocale());
        setMessages(msg);
        var usingLocale = String.format("Using locale: %s - %s",
            getMessages().getString("locale"), getMessages().getString("locale-version"));
        logger.info(usingLocale);
    }

    @NotNull
    private static String parseVariant(final String region) {
        String variant;
        if (region.contains("_")) {
            variant = region.substring(region.indexOf("_"));
        } else if (region.contains("-")) {
            variant = region.substring(region.indexOf("-"));
        } else {
            variant = "";
        }
        return variant;
    }

    @NotNull
    private static String parseRegion(final String region) {
        String newRegion;
        if (region.contains("_")) {
            newRegion = region.substring(0, region.indexOf("_"));
        } else if (region.contains("-")) {
            newRegion = region.substring(0, region.indexOf("-"));
        } else {
            newRegion = region;
        }
        return newRegion;
    }

    private static boolean fileInJar(final String localeString) {
        var localeFile = String.format("/Translation_%s.properties", localeString);
        URL u = FlagWar.class.getResource(localeFile);
        return u != null;
    }

    /** @return the {@link #currentLocale}. */
    public static Locale getLocale() {
        return currentLocale;
    }
    /**
     * Set the {@link #currentLocale}.
     * @param locale the {@link Locale} to use.
     */
    private static void setLocale(final Locale locale) {
        currentLocale = locale;
    }
    /** @return the {@link #messages} {@link ResourceBundle}. */
    public static ResourceBundle getMessages() {
        return messages;
    }

    /**
     * Set the {@link #messages} {@link ResourceBundle}.
     * @param resourceBundle the ResourceBundle to use.
     */
    private static void setMessages(final ResourceBundle resourceBundle) {
        messages = resourceBundle;
    }
}
