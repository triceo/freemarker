/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.freemarker.core.outputformat.impl.HTMLOutputFormat;
import org.apache.freemarker.core.outputformat.impl.UndefinedOutputFormat;
import org.apache.freemarker.core.outputformat.impl.XMLOutputFormat;
import org.apache.freemarker.core.util._DateUtil;
import org.apache.freemarker.core.util._NullArgumentException;
import org.junit.Test;

public class MutableParsingAndProcessingConfigurationTest {

    @Test
    public void testSetAutoEscaping() throws Exception {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertEquals(AutoEscapingPolicy.ENABLE_IF_DEFAULT, cfgB.getAutoEscapingPolicy());

        cfgB.setAutoEscapingPolicy(AutoEscapingPolicy.ENABLE_IF_SUPPORTED);
        assertEquals(AutoEscapingPolicy.ENABLE_IF_SUPPORTED, cfgB.getAutoEscapingPolicy());

        cfgB.setAutoEscapingPolicy(AutoEscapingPolicy.ENABLE_IF_DEFAULT);
        assertEquals(AutoEscapingPolicy.ENABLE_IF_DEFAULT, cfgB.getAutoEscapingPolicy());

        cfgB.setAutoEscapingPolicy(AutoEscapingPolicy.DISABLE);
        assertEquals(AutoEscapingPolicy.DISABLE, cfgB.getAutoEscapingPolicy());

        cfgB.setSetting(Configuration.ExtendableBuilder.AUTO_ESCAPING_POLICY_KEY_CAMEL_CASE, "enableIfSupported");
        assertEquals(AutoEscapingPolicy.ENABLE_IF_SUPPORTED, cfgB.getAutoEscapingPolicy());

        cfgB.setSetting(Configuration.ExtendableBuilder.AUTO_ESCAPING_POLICY_KEY_CAMEL_CASE, "enable_if_supported");
        assertEquals(AutoEscapingPolicy.ENABLE_IF_SUPPORTED, cfgB.getAutoEscapingPolicy());

        cfgB.setSetting(Configuration.ExtendableBuilder.AUTO_ESCAPING_POLICY_KEY_CAMEL_CASE, "enableIfDefault");
        assertEquals(AutoEscapingPolicy.ENABLE_IF_DEFAULT, cfgB.getAutoEscapingPolicy());

        cfgB.setSetting(Configuration.ExtendableBuilder.AUTO_ESCAPING_POLICY_KEY_CAMEL_CASE, "enable_if_default");
        assertEquals(AutoEscapingPolicy.ENABLE_IF_DEFAULT, cfgB.getAutoEscapingPolicy());

        cfgB.setSetting(Configuration.ExtendableBuilder.AUTO_ESCAPING_POLICY_KEY_CAMEL_CASE, "disable");
        assertEquals(AutoEscapingPolicy.DISABLE, cfgB.getAutoEscapingPolicy());
    }

    @Test
    public void testSetOutputFormat() throws Exception {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertEquals(UndefinedOutputFormat.INSTANCE, cfgB.getOutputFormat());
        assertFalse(cfgB.isOutputFormatSet());

        try {
            cfgB.setOutputFormat(null);
            fail();
        } catch (_NullArgumentException e) {
            // Expected
        }

        assertFalse(cfgB.isOutputFormatSet());

        cfgB.setSetting(Configuration.ExtendableBuilder.OUTPUT_FORMAT_KEY_CAMEL_CASE, XMLOutputFormat.class.getSimpleName());
        assertEquals(XMLOutputFormat.INSTANCE, cfgB.getOutputFormat());

        cfgB.setSetting(Configuration.ExtendableBuilder.OUTPUT_FORMAT_KEY_SNAKE_CASE, HTMLOutputFormat.class.getSimpleName());
        assertEquals(HTMLOutputFormat.INSTANCE, cfgB.getOutputFormat());

        cfgB.unsetOutputFormat();
        assertEquals(UndefinedOutputFormat.INSTANCE, cfgB.getOutputFormat());
        assertFalse(cfgB.isOutputFormatSet());

        cfgB.setOutputFormat(UndefinedOutputFormat.INSTANCE);
        assertTrue(cfgB.isOutputFormatSet());
        cfgB.setSetting(Configuration.ExtendableBuilder.OUTPUT_FORMAT_KEY_CAMEL_CASE, "default");
        assertFalse(cfgB.isOutputFormatSet());

        try {
            cfgB.setSetting(Configuration.ExtendableBuilder.OUTPUT_FORMAT_KEY, "null");
        } catch (InvalidSettingValueException e) {
            assertThat(e.getCause().getMessage(), containsString(UndefinedOutputFormat.class.getSimpleName()));
        }
    }

    @Test
    public void testSetRecognizeStandardFileExtensions() throws Exception {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertTrue(cfgB.getRecognizeStandardFileExtensions());
        assertFalse(cfgB.isRecognizeStandardFileExtensionsSet());

        cfgB.setRecognizeStandardFileExtensions(false);
        assertFalse(cfgB.getRecognizeStandardFileExtensions());
        assertTrue(cfgB.isRecognizeStandardFileExtensionsSet());

        cfgB.unsetRecognizeStandardFileExtensions();
        assertTrue(cfgB.getRecognizeStandardFileExtensions());
        assertFalse(cfgB.isRecognizeStandardFileExtensionsSet());

        cfgB.setRecognizeStandardFileExtensions(true);
        assertTrue(cfgB.getRecognizeStandardFileExtensions());
        assertTrue(cfgB.isRecognizeStandardFileExtensionsSet());

        cfgB.setSetting(Configuration.ExtendableBuilder.RECOGNIZE_STANDARD_FILE_EXTENSIONS_KEY_CAMEL_CASE, "false");
        assertFalse(cfgB.getRecognizeStandardFileExtensions());
        assertTrue(cfgB.isRecognizeStandardFileExtensionsSet());

        cfgB.setSetting(Configuration.ExtendableBuilder.RECOGNIZE_STANDARD_FILE_EXTENSIONS_KEY_SNAKE_CASE, "default");
        assertTrue(cfgB.getRecognizeStandardFileExtensions());
        assertFalse(cfgB.isRecognizeStandardFileExtensionsSet());
    }

    @Test
    public void testSetTabSize() throws Exception {
        String ftl = "${\t}";

        try {
            new Template(null, ftl,
                    new Configuration.Builder(Configuration.VERSION_3_0_0).build());
            fail();
        } catch (ParseException e) {
            assertEquals(9, e.getColumnNumber());
        }

        try {
            new Template(null, ftl,
                    new Configuration.Builder(Configuration.VERSION_3_0_0).tabSize(1).build());
            fail();
        } catch (ParseException e) {
            assertEquals(4, e.getColumnNumber());
        }

        try {
            new Configuration.Builder(Configuration.VERSION_3_0_0).tabSize(0);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            new Configuration.Builder(Configuration.VERSION_3_0_0).tabSize(257);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testTabSizeSetting() throws Exception {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);
        assertEquals(8, cfgB.getTabSize());
        cfgB.setSetting(Configuration.ExtendableBuilder.TAB_SIZE_KEY_CAMEL_CASE, "4");
        assertEquals(4, cfgB.getTabSize());
        cfgB.setSetting(Configuration.ExtendableBuilder.TAB_SIZE_KEY_SNAKE_CASE, "1");
        assertEquals(1, cfgB.getTabSize());

        try {
            cfgB.setSetting(Configuration.ExtendableBuilder.TAB_SIZE_KEY_SNAKE_CASE, "x");
            fail();
        } catch (ConfigurationException e) {
            assertThat(e.getCause(), instanceOf(NumberFormatException.class));
        }
    }

    @Test
    public void testNamingConventionSetSetting() throws ConfigurationException {
        Configuration.Builder cfg = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertEquals(NamingConvention.AUTO_DETECT, cfg.getNamingConvention());

        cfg.setSetting("naming_convention", "legacy");
        assertEquals(NamingConvention.LEGACY, cfg.getNamingConvention());

        cfg.setSetting("naming_convention", "camel_case");
        assertEquals(NamingConvention.CAMEL_CASE, cfg.getNamingConvention());

        cfg.setSetting("naming_convention", "auto_detect");
        assertEquals(NamingConvention.AUTO_DETECT, cfg.getNamingConvention());
    }

    @Test
    public void testLazyImportsSetSetting() throws ConfigurationException {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertFalse(cfgB.getLazyImports());
        assertFalse(cfgB.isLazyImportsSet());
        cfgB.setSetting("lazy_imports", "true");
        assertTrue(cfgB.getLazyImports());
        cfgB.setSetting("lazyImports", "false");
        assertFalse(cfgB.getLazyImports());
        assertTrue(cfgB.isLazyImportsSet());
    }

    @Test
    public void testLazyAutoImportsSetSetting() throws ConfigurationException {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertNull(cfgB.getLazyAutoImports());
        assertFalse(cfgB.isLazyAutoImportsSet());
        cfgB.setSetting("lazy_auto_imports", "true");
        assertEquals(Boolean.TRUE, cfgB.getLazyAutoImports());
        assertTrue(cfgB.isLazyAutoImportsSet());
        cfgB.setSetting("lazyAutoImports", "false");
        assertEquals(Boolean.FALSE, cfgB.getLazyAutoImports());
        cfgB.setSetting("lazyAutoImports", "null");
        assertNull(cfgB.getLazyAutoImports());
        assertTrue(cfgB.isLazyAutoImportsSet());
        cfgB.unsetLazyAutoImports();
        assertNull(cfgB.getLazyAutoImports());
        assertFalse(cfgB.isLazyAutoImportsSet());
    }

    @Test
    public void testLocaleSetting() throws TemplateException, ConfigurationException {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertEquals(Locale.getDefault(), cfgB.getLocale());
        assertFalse(cfgB.isLocaleSet());

        Locale nonDefault = Locale.getDefault().equals(Locale.GERMANY) ? Locale.FRANCE : Locale.GERMANY;
        cfgB.setLocale(nonDefault);
        assertTrue(cfgB.isLocaleSet());
        assertEquals(nonDefault, cfgB.getLocale());

        cfgB.unsetLocale();
        assertEquals(Locale.getDefault(), cfgB.getLocale());
        assertFalse(cfgB.isLocaleSet());

        cfgB.setSetting(Configuration.ExtendableBuilder.LOCALE_KEY, "JVM default");
        assertEquals(Locale.getDefault(), cfgB.getLocale());
        assertTrue(cfgB.isLocaleSet());
    }

    @Test
    public void testSourceEncodingSetting() throws TemplateException, ConfigurationException {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertEquals(Charset.defaultCharset(), cfgB.getSourceEncoding());
        assertFalse(cfgB.isSourceEncodingSet());

        Charset nonDefault = Charset.defaultCharset().equals(StandardCharsets.UTF_8) ? StandardCharsets.ISO_8859_1
                : StandardCharsets.UTF_8;
        cfgB.setSourceEncoding(nonDefault);
        assertTrue(cfgB.isSourceEncodingSet());
        assertEquals(nonDefault, cfgB.getSourceEncoding());

        cfgB.unsetSourceEncoding();
        assertEquals(Charset.defaultCharset(), cfgB.getSourceEncoding());
        assertFalse(cfgB.isSourceEncodingSet());

        cfgB.setSetting(Configuration.ExtendableBuilder.SOURCE_ENCODING_KEY, "JVM default");
        assertEquals(Charset.defaultCharset(), cfgB.getSourceEncoding());
        assertTrue(cfgB.isSourceEncodingSet());
    }

    @Test
    public void testTimeZoneSetting() throws TemplateException, ConfigurationException {
        Configuration.Builder cfgB = new Configuration.Builder(Configuration.VERSION_3_0_0);

        assertEquals(TimeZone.getDefault(), cfgB.getTimeZone());
        assertFalse(cfgB.isTimeZoneSet());

        TimeZone nonDefault = TimeZone.getDefault().equals(_DateUtil.UTC) ? TimeZone.getTimeZone("PST") : _DateUtil.UTC;
        cfgB.setTimeZone(nonDefault);
        assertTrue(cfgB.isTimeZoneSet());
        assertEquals(nonDefault, cfgB.getTimeZone());

        cfgB.unsetTimeZone();
        assertEquals(TimeZone.getDefault(), cfgB.getTimeZone());
        assertFalse(cfgB.isTimeZoneSet());

        cfgB.setSetting(Configuration.ExtendableBuilder.TIME_ZONE_KEY, "JVM default");
        assertEquals(TimeZone.getDefault(), cfgB.getTimeZone());
        assertTrue(cfgB.isTimeZoneSet());
    }

    // ------------

    @Test
    public void testGetSettingNamesAreSorted() throws Exception {
        for (boolean camelCase : new boolean[] { false, true }) {
            List<String> names = new ArrayList<>(MutableParsingAndProcessingConfiguration.getSettingNames(camelCase));
            List<String> inheritedNames = new ArrayList<>(MutableProcessingConfiguration.getSettingNames(camelCase));
            assertStartsWith(names, inheritedNames);

            String prevName = null;
            for (int i = inheritedNames.size(); i < names.size(); i++) {
                String name = names.get(i);
                if (prevName != null) {
                    assertThat(name, greaterThan(prevName));
                }
                prevName = name;
            }
        }
    }

    @Test
    public void testGetSettingNamesNameConventionsContainTheSame() throws Exception {
        MutableProcessingConfigurationTest.testGetSettingNamesNameConventionsContainTheSame(
                new ArrayList<>(MutableParsingAndProcessingConfiguration.getSettingNames(false)),
                new ArrayList<>(MutableParsingAndProcessingConfiguration.getSettingNames(true)));
    }

    @Test
    public void testStaticFieldKeysCoverAllGetSettingNames() throws Exception {
        List<String> names = new ArrayList<>(MutableParsingAndProcessingConfiguration.getSettingNames(false));
        for (String name :  names) {
            assertTrue("No field was found for " + name, keyFieldExists(name));
        }
    }

    @Test
    public void testGetSettingNamesCoversAllStaticKeyFields() throws Exception {
        Collection<String> names = MutableParsingAndProcessingConfiguration.getSettingNames(false);

        for (Class<?> cfgableClass : new Class[] {
                MutableParsingAndProcessingConfiguration.class,
                MutableProcessingConfiguration.class }) {
            for (Field f : cfgableClass.getFields()) {
                if (f.getName().endsWith("_KEY")) {
                    final String name = (String) f.get(null);
                    assertTrue("Missing setting name: " + name, names.contains(name));
                }
            }
        }
    }

    @Test
    public void testKeyStaticFieldsHasAllVariationsAndCorrectFormat() throws IllegalArgumentException, IllegalAccessException {
        MutableProcessingConfigurationTest.testKeyStaticFieldsHasAllVariationsAndCorrectFormat(
                MutableParsingAndProcessingConfiguration.class);
    }

    @SuppressWarnings("boxing")
    private void assertStartsWith(List<String> list, List<String> headList) {
        int index = 0;
        for (String name : headList) {
            assertThat(index, lessThan(list.size()));
            assertEquals(name, list.get(index));
            index++;
        }
    }

    private boolean keyFieldExists(String name) throws Exception {
        Field field;
        try {
            field = MutableParsingAndProcessingConfiguration.class.getField(name.toUpperCase() + "_KEY");
        } catch (NoSuchFieldException e) {
            return false;
        }
        assertEquals(name, field.get(null));
        return true;
    }

}