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
package org.apache.felix.dm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods for invoking callbacks. Lookups of callbacks are accellerated by using a LRU cache.
 * 
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class InvocationUtil {
    private static final Map /* <Key, Method> */ m_methodCache;
    static {
        int size = 2048;
        try {
            String value = System.getProperty(DependencyManager.METHOD_CACHE_SIZE);
            if (value != null) {
                size = Integer.parseInt(value);
            }
        }
        catch (Exception e) {}
        m_methodCache = new LRUMap(Math.max(size, 64));
    }
    
    /**
     * Invokes a callback method on an instance. The code will search for a callback method with
     * the supplied name and any of the supplied signatures in order, invoking the first one it finds.
     * 
     * @param instance the instance to invoke the method on
     * @param methodName the name of the method
     * @param signatures the ordered list of signatures to look for
     * @param parameters the parameter values to use for each potential signature
     * @return whatever the method returns
     * @throws MethodNotFoundException when no method could be found
     * @throws IllegalArgumentException when illegal values for this methods arguments are supplied 
     * @throws IllegalAccessException when the method cannot be accessed
     * @throws InvocationTargetException when the method that was invoked throws an exception
     */
    public static Object invokeCallbackMethod(Object instance, String methodName, Class[][] signatures, Object[][] parameters) throws MethodNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class currentClazz = instance.getClass();
        while (currentClazz != null && currentClazz != Object.class) {
            try {
                return invokeMethod(instance, currentClazz, methodName, signatures, parameters, false);
            }
            catch (MethodNotFoundException nsme) {
                // ignore
            }
            currentClazz = currentClazz.getSuperclass();
        }
        throw new MethodNotFoundException(methodName);
    }

    /**
     * Invoke a method on an instance.
     * 
     * @param object the instance to invoke the method on
     * @param clazz the class of the instance
     * @param name the name of the method
     * @param signatures the signatures to look for in order
     * @param parameters the parameter values for the signatures
     * @param isSuper <code>true</code> if this is a superclass and we should therefore not look for private methods
     * @return whatever the method returns
     * @throws MethodNotFoundException when no method could be found
     * @throws IllegalArgumentException when illegal values for this methods arguments are supplied 
     * @throws IllegalAccessException when the method cannot be accessed
     * @throws InvocationTargetException when the method that was invoked throws an exception
     */
    public static Object invokeMethod(Object object, Class clazz, String name, Class[][] signatures, Object[][] parameters, boolean isSuper) throws MethodNotFoundException, InvocationTargetException, IllegalArgumentException, IllegalAccessException {
        if (object == null) {
            throw new IllegalArgumentException("Instance cannot be null");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        
        // if we're talking to a proxy here, dig one level deeper to expose the
        // underlying invocation handler (we do the same for injecting instances)
        if (Proxy.isProxyClass(clazz)) {
            object = Proxy.getInvocationHandler(object);
            clazz = object.getClass();
        }
        
        Method m = null;
        for (int i = 0; i < signatures.length; i++) {
            Class[] signature = signatures[i];
            m = getDeclaredMethod(clazz, name, signature, isSuper);
            if (m != null) {
                return m.invoke(object, parameters[i]);
            }
        }
        throw new MethodNotFoundException(name);
    }
    
    private static Method getDeclaredMethod(Class clazz, String name, Class[] signature, boolean isSuper) {
        // first check our cache
        Key key = new Key(clazz, name, signature);
        Method m = null;
        synchronized (m_methodCache) {
            m = (Method) m_methodCache.get(key);
            if (m != null) {
                return m;
            }
            else if (m_methodCache.containsKey(key)) {
                // the key is in our cache, it just happens to have a null value
                return null;
            }
        }
        // then do a lookup
        try {
            m = clazz.getDeclaredMethod(name, signature);
            if (!(isSuper && Modifier.isPrivate(m.getModifiers()))) {
                m.setAccessible(true);
            }
        }
        catch (NoSuchMethodException e) {
            // ignore
        }
        synchronized (m_methodCache) {
            m_methodCache.put(key, m);
        }
        return m;
    }
    
    public static class Key {
        private final Class m_clazz;
        private final String m_name;
        private final Class[] m_signature;

        public Key(Class clazz, String name, Class[] signature) {
            m_clazz = clazz;
            m_name = name;
            m_signature = signature;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((m_clazz == null) ? 0 : m_clazz.hashCode());
            result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
            result = prime * result + Arrays.hashCode(m_signature);
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (m_clazz == null) {
                if (other.m_clazz != null)
                    return false;
            }
            else if (!m_clazz.equals(other.m_clazz))
                return false;
            if (m_name == null) {
                if (other.m_name != null)
                    return false;
            }
            else if (!m_name.equals(other.m_name))
                return false;
            if (!Arrays.equals(m_signature, other.m_signature))
                return false;
            return true;
        }
    }
    
    public static class LRUMap extends LinkedHashMap {
        private final int m_size;
        
        public LRUMap(int size) {
            m_size = size;
        }
        
        protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
            return size() > m_size;
        }
    }
}
