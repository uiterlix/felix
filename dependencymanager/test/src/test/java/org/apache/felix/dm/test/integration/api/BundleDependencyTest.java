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
package org.apache.felix.dm.test.integration.api;

import junit.framework.Assert;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.dm.test.components.Ensure;
import org.apache.felix.dm.test.integration.common.TestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;

@RunWith(PaxExam.class)
public class BundleDependencyTest extends TestBase {
    @Test
    public void testBundleDependencies() {
        DependencyManager m = new DependencyManager(context);
        // create a service provider and consumer
        Consumer c = new Consumer();
        Component consumer = m.createComponent().setImplementation(c).add(m.createBundleDependency().setCallbacks("add", "remove"));
        // add the service consumer
        m.add(consumer);
        // check if at least one bundle was found
        c.check();
        // remove the consumer again
        m.remove(consumer);
        // check if all bundles were removed correctly
        c.doubleCheck();
        
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        Component consumerWithFilter = m.createComponent().setImplementation(new FilteredConsumer(e)).add(m.createBundleDependency().setFilter("(Bundle-SymbolicName=org.apache.felix.dependencymanager)").setCallbacks("add", "remove"));
        // add a consumer with a filter
        m.add(consumerWithFilter);
        e.step(2);
        // remove the consumer again
        m.remove(consumerWithFilter);
        e.step(4);
    }
    
    @Test
    public void testRequiredBundleDependency() {
        DependencyManager m = new DependencyManager(context);
        // create a service provider and consumer
        Consumer c = new Consumer();
        
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        Component consumerWithFilter = m.createComponent()
            .setImplementation(new FilteredConsumerRequired(e))
            .add(m.createBundleDependency()
                .setRequired(true)
                .setFilter("(Bundle-SymbolicName=org.apache.felix.dependencymanager)")
                .setCallbacks("add", "remove")
                );
        // add a consumer with a filter
        m.add(consumerWithFilter);
        e.waitForStep(1, 10000);
        // remove the consumer again
        m.remove(consumerWithFilter);
        e.waitForStep(2, 10000);
    }
    
    static class Consumer {
        private volatile int m_count = 0;

        public void add(Bundle b) {
            Assert.assertNotNull("bundle instance must not be null", b);
            m_count++;
        }
        
        public void check() {
            Assert.assertTrue("we should have found at least one bundle", m_count > 0);
        }
        
        public void remove(Bundle b) {
            m_count--;
        }
        
        public void doubleCheck() {
            Assert.assertEquals("all bundles we found should have been removed again", 0, m_count);
        }
    }
    
    static class FilteredConsumer {
        private final Ensure m_ensure;

        public FilteredConsumer(Ensure e) {
            m_ensure = e;
        }
        
        public void add(Bundle b) {
            m_ensure.step(1);
        }
        
        public void remove(Bundle b) {
            m_ensure.step(3);
        }
    }
    
    static class FilteredConsumerRequired {
        private final Ensure m_ensure;

        public FilteredConsumerRequired(Ensure e) {
            m_ensure = e;
        }
        
        public void add(Bundle b) {
            System.out.println("Bundle is " + b);
//            Assert.assertNotNull(b);
            if (b.getSymbolicName().equals("org.apache.felix.dependencymanager")) {
                m_ensure.step(1);
            }
        }
        
        public void remove(Bundle b) {
            Assert.assertNotNull(b);
            if (b.getSymbolicName().equals("org.apache.felix.dependencymanager")) {
                m_ensure.step(2);
            }
        }
    }
}
