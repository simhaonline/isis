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
package org.apache.isis.subdomains.base.applib.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

public final class MessageUtils {

    private MessageUtils(){}
    private static final Pattern pattern = Pattern.compile(".*Reason: (.+?)[ ]*Identifier:.*");

    public static String normalize(final Exception ex) {
        if(ex == null || Strings.isNullOrEmpty(ex.getMessage())) {
            return null;
        }
        String message = ex.getMessage();
        final Matcher matcher = pattern.matcher(message);
        if(matcher.matches()) {
            return matcher.group(1);
        }
        return message;
    }
}
