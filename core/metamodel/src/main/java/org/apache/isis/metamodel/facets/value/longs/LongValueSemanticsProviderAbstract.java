/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.metamodel.facets.value.longs;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

import org.apache.isis.config.IsisConfiguration.Value.FormatIdentifier;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;


public abstract class LongValueSemanticsProviderAbstract extends ValueSemanticsProviderAndFacetAbstract<Long> implements LongValueFacet {

    public static Class<? extends Facet> type() {
        return LongValueFacet.class;
    }

    private static final Long DEFAULT_VALUE = Long.valueOf(0L);
    private static final int MAX_LENGTH = 18;
    private static final int TYPICAL_LENGTH = MAX_LENGTH;

    private final NumberFormat format;

    public LongValueSemanticsProviderAbstract(final FacetHolder holder, final Class<Long> adaptedClass) {
        super(type(), holder, adaptedClass, TYPICAL_LENGTH, MAX_LENGTH, Immutability.IMMUTABLE, EqualByContent.HONOURED, DEFAULT_VALUE);
        format = determineNumberFormat(FormatIdentifier.LONG);
    }

    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    protected Long doParse(final Object context, final String entry) {
        try {
            return Long.valueOf(format.parse(entry).longValue());
        } catch (final ParseException e) {
            throw new TextEntryParseException("Not a whole number " + entry, e);
        }
    }

    @Override
    public String titleString(final Object value) {
        return titleString(format, value);
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(new DecimalFormat(usingMask), value);
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        return object.toString();
    }

    @Override
    protected Long doRestore(final String data) {
        return new Long(data);
    }

    // //////////////////////////////////////////////////////////////////
    // LongValueFacet
    // //////////////////////////////////////////////////////////////////

    @Override
    public Long longValue(final ObjectAdapter object) {
        return (Long) (object == null ? null : object.getPojo());
    }

    @Override
    public ObjectAdapter createValue(final Long value) {
        return value == null ? null : getObjectAdapterProvider().adapterFor(value);
    }

    // // ///// toString ///////
    @Override
    public String toString() {
        return "LongValueSemanticsProvider: " + format;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("format", format);
    }
}