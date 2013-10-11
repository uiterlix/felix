/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.dm.impl.index.multiproperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 * 
 * Simple String cache to limit the size of keys used in the multiproperty filter index. Can be disabled
 * through a system property for debugging purposes.
 * The cache basically assigns a number to every item provided.
 */
public class ValueCache {
	
	protected static final boolean enabled = Boolean.parseBoolean(System.getProperty("org.apache.felix.dependencymanager.filterindex.valuecache", "true"));
	private static final long MAX_BASE = Character.MAX_VALUE - 3; // exclude for other characters we need (65535, 65534)

	private static Map /* <String, String> */ valueMap = new HashMap /* <String, String> */();
	private static long valueIndex = 0;
	
	public static String compressValue(String value) {
		if (enabled) {
			synchronized (valueMap) {
				String compressedValue = (String) valueMap.get(value);
				if (compressedValue == null) {
					if (valueIndex < Long.MAX_VALUE) {
						valueIndex ++;
						compressedValue = compressLongToString(valueIndex);
						valueMap.put(value, compressedValue);
					} else {
						compressedValue = value;
					}
				}
				return compressedValue;
			}
		} else {
			return value;
		}
	}
	
	private static String compressLongToString(long value) {
		int maxPower = getMaximumPower(value, 4);
		int remainder = (int) (value - (maxPower > 0 ? Math.pow(MAX_BASE, maxPower) : 0));
		
		char[] chars = new char[maxPower + (remainder > 0 ? 1 : 0)];
		for (int i = 0; i < maxPower; i++) {
			chars[i] = (char) MAX_BASE;
		}
		if (remainder > 0) {
			chars[maxPower] = (char)remainder;
		}
		return new String(chars);
	}
	
	private static int getMaximumPower(long value, int currentPower) {
		if (Math.pow(MAX_BASE, currentPower) > value) {
			return getMaximumPower(value, currentPower - 1);
		} else {
			// value fits
			return currentPower;
		}
	}

}
