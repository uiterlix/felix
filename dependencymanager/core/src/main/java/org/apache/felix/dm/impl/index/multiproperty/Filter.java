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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class Filter {
	
	private boolean m_valid = true;
	private Map /* <String, Property> */ m_properties = new HashMap();
	private Set /* <String> */ m_propertyKeys = new TreeSet(String.CASE_INSENSITIVE_ORDER);
	
	private Filter() {
		
	}
	
	// Sample valid filter string (&(objectClass=OBJECTCLASS)(&(model=MODEL)(concept=CONCEPT)(role=ROLE)(!(context=*))))
	public static Filter parse(String filterString) {
		Filter filter = new Filter();
		StringTokenizer tokenizer = new StringTokenizer(filterString, "(&|=)", true);
		
		String token = null;
		String prevToken = null;
		String key = null;
		StringBuilder valueBuilder = new StringBuilder();
		int flag = Property.NONE;

		while (tokenizer.hasMoreTokens()) {
			prevToken = token;
			token = tokenizer.nextToken();
			if (token.equals("|")) {
				// we're not into OR's
				filter.m_valid = false;
				break;
			}
			if (token.equals("!")) {
				flag = Property.NEGATE;
			} else if (token.equals("#")) {
				flag = Property.MULTI;
			} else if (token.equals("=")) {
				key = prevToken.toLowerCase();
			} else if (key != null) {
				if (!token.equals(")")) {
					valueBuilder.append(token); // might be superseded by a &
				}
				if (token.equals(")")) {
					// set complete
					if (filter.m_properties.containsKey(key)) {
						// set current property to multivalue
						Property property = (Property) filter.m_properties.get(key);
						property.addValue(valueBuilder.toString());
						// just a check whether the filter doesn't mix negate and non-negate attributes
						if (flag != property.getFlag()) {
							property.invalidate();
						}
					} else {
						Property property = new Property(flag, key, valueBuilder.toString());
						filter.m_properties.put(key, property);
						filter.m_propertyKeys.add(key);
					}
					flag = Property.NONE;
					key = null;
					valueBuilder = new StringBuilder();
				}
			} 
		}
		return filter;
	}
	
	public boolean containsProperty(String propertyKey) {
		return m_properties.containsKey(propertyKey);
	}
	
	public Set /* <String> */ getPropertyKeys() {
		return m_properties.keySet();
	}
	
	public Property getProperty(String key) {
		return (Property) m_properties.get(key);
	}
	
	public boolean isValid() {
		if (!m_valid) {
			return m_valid;
		} else {
			// also check the properties
			Iterator propertiesIterator = m_properties.values().iterator();
			while (propertiesIterator.hasNext()) {
				Property property = (Property) propertiesIterator.next();
				if (!property.isValid()) {
					return false;
				}
			}
		}
		return true;
	}

	protected String createKey() {
		StringBuilder builder = new StringBuilder();
		Iterator keys = m_propertyKeys.iterator();
		
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Property prop = (Property) m_properties.get(key);
			if (!prop.isNegate()) {
				Iterator values = prop.getValues().iterator();
				while (values.hasNext()) {
					String value = (String) values.next();
					builder.append(ValueCache.compressValue(value));
					if (values.hasNext()) {
						builder.append(MultiPropertyFilterIndex.VALUE_SEPARATOR);
					}
				}
				if (keys.hasNext()) {
					builder.append(MultiPropertyFilterIndex.KEY_SEPARATOR);
				}
			}
		}
		// strip the final ';' in case the last key was a wildcard property
		if (builder.charAt(builder.length() - 1) == MultiPropertyFilterIndex.KEY_SEPARATOR) {
			return builder.toString().substring(0, builder.length() - 1);
		} else {
			return builder.toString();
		}
	}	
	
}
