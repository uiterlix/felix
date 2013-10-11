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

import java.util.Set;
import java.util.TreeSet;
/**
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */

public class Property {
	
	public static int NONE = 0;
	public static int NEGATE = 1;
	public static int MULTI = 2;

	int m_flag = -1;
	boolean m_valid = true;
	String m_key;
	String m_value;
	Set m_values = new TreeSet(String.CASE_INSENSITIVE_ORDER);
	
	public Property() {
	}
	
	public Property(int flag, String key, String value) {
		super();
		this.m_flag = flag;
		this.m_key = key.toLowerCase();
		this.m_values.add(value);
		this.m_value = value;
	}

	public void setNegate(boolean negate) {
		this.m_flag = NEGATE;
	}
	
	public int getFlag() {
		return m_flag;
	}
	
	public void setKey(String key) {
		this.m_key = key.toLowerCase();
	}
	
	public void addValue(String value) {
		if (this.m_value == null) {
			// value has not bee set yet
			this.m_value = value;
		}
		if (value != null) {
			m_values.add(value);
		}
	}
	
	public boolean isNegate() {
		return m_flag == NEGATE;
	}
	
	public String getKey() {
		return m_key;
	}
	
	public String getValue() {
		return m_value;
	}
	
	public Set getValues() {
		return m_values;
	}

	public String toString() {
		return "Property [flag=" + m_flag + ", key=" + m_key + ", values="
				+ m_values + "]";
	}
	
	public void invalidate() {
		m_valid = false;
	}
	
	public boolean isValid() {
		return m_valid;
	}

	public void setMulti(boolean multi) {
		m_flag = MULTI;
	}
	
	public boolean isMulti() {
		return m_flag == MULTI;
	}

	public boolean hasMultipleValues() {
		return m_values.size() > 1;
	}
}