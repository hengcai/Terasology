/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.i18n;

import java.util.Locale;

import org.terasology.engine.Uri;

/**
 * TODO Type description
 */
public interface TranslationSystem {

    /**
     * @param name
     * @return the project or <code>null</code>.
     */
    TranslationProject getProject(Uri name);

    /**
     * @param id must match the pattern <code>${module:project#id}</code>
     * @return
     */
    String translate(String id);

    /**
     * @param string
     * @param locale
     * @return
     */
    String translate(String string, Locale locale);

    /**
     * @param locale
     */
    void setLocale(Locale locale);

    Locale getLocale();
}
