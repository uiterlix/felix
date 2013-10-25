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
import org.apache.felix.dm.ComponentStateListener;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.dm.test.components.Ensure;
import org.apache.felix.dm.test.integration.common.TestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

@RunWith(PaxExam.class)
public class ComponentLifeCycleTest extends TestBase {
    @Test
    public void testComponentLifeCycleCallbacks() {
        DependencyManager m = new DependencyManager(context);
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        // create a simple service component
        Component s = m.createComponent()
            .setImplementation(new ComponentInstance(e));
        // add it, and since it has no dependencies, it should be activated immediately
        m.add(s);
        // remove it so it gets destroyed
        m.remove(s);
        // ensure we executed all steps inside the component instance
        e.step(6);
    }
    
    static class ComponentInstance {
        private final Ensure m_ensure;
        public ComponentInstance(Ensure e) {
            m_ensure = e;
            m_ensure.step(1);
        }
        public void init() {
            m_ensure.step(2);
        }
        public void start() {
            m_ensure.step(3);
        }
        public void stop() {
            m_ensure.step(4);
        }
        public void destroy() {
            m_ensure.step(5);
        }
    }

    @Test
    public void testCustomComponentLifeCycleCallbacks() {
        DependencyManager m = new DependencyManager(context);
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        // create a simple service component
        Component s = m.createComponent()
            .setImplementation(new CustomComponentInstance(e))
            .setCallbacks("a", "b", "c", "d");
        // add it, and since it has no dependencies, it should be activated immediately
        m.add(s);
        // remove it so it gets destroyed
        m.remove(s);
        // ensure we executed all steps inside the component instance
        e.step(6);
    }
    
    static class CustomComponentInstance {
        private final Ensure m_ensure;
        public CustomComponentInstance(Ensure e) {
            m_ensure = e;
            m_ensure.step(1);
        }
        public void a() {
            m_ensure.step(2);
        }
        public void b() {
            m_ensure.step(3);
        }
        public void c() {
            m_ensure.step(4);
        }
        public void d() {
            m_ensure.step(5);
        }
    }
    
    
    
    @Test
    public void testComponentStateListingLifeCycle() {
        DependencyManager m = new DependencyManager(context);
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        // create a simple service component
        ComponentStateListeningInstance implementation = new ComponentStateListeningInstance(e);
        Component s = m.createComponent()
            .setInterface(MyInterface.class.getName(), null)
            .setImplementation(implementation);
        // add the state listener
        s.addStateListener(implementation);
        // add it, and since it has no dependencies, it should be activated immediately
        m.add(s);
        // remove it so it gets destroyed
        m.remove(s);
        // remove the state listener
        s.removeStateListener(implementation);
        // ensure we executed all steps inside the component instance
        e.step(10);
    }
    
    public static interface MyInterface {}

    static class ComponentStateListeningInstance implements MyInterface, ComponentStateListener {
        volatile ServiceRegistration m_registration;
        private final Ensure m_ensure;
        
        public ComponentStateListeningInstance(Ensure e) {
            m_ensure = e;
            m_ensure.step(1);
        }
        
        private void debug() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            System.out.println("AT: " + stackTrace[2].getClassName() + "." + stackTrace[2].getMethodName() + "():" + stackTrace[2].getLineNumber());
        }
        
        public void init(Component c) {
            debug();
            m_ensure.step(2);
        }
        
        public void start(Component c) {
            debug();
            m_ensure.step(4);
        }
        public void stop(Component c) {
            debug();
            m_ensure.step(7);
        }
        
        public void destroy(Component c) {
            debug();
            m_ensure.step(9);
        }
        
        public void starting(Component component) {
            debug();
            m_ensure.step(3);
        }

        public void started(Component component) {
            debug();
            m_ensure.step(5);
            ServiceReference reference = m_registration.getReference();
            Assert.assertNotNull("Service not yet registered.", reference);
        }

        public void stopping(Component component) {
            debug();
            m_ensure.step(6);
        }

        public void stopped(Component component) {
            debug();
            m_ensure.step(8);
        }
    }

    

    @Test
    public void testDynamicComponentStateListingLifeCycle() {
        DependencyManager m = new DependencyManager(context);
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        // create a simple service component
        Component s = m.createComponent()
            .setInterface(MyInterface.class.getName(), null)
            .setImplementation(new DynamicComponentStateListeningInstance(e));
        // add it, and since it has no dependencies, it should be activated immediately
        m.add(s);
        // remove it so it gets destroyed
        m.remove(s);
        // ensure we executed all steps inside the component instance
        e.step(10);
    }

    static class DynamicComponentStateListeningInstance implements MyInterface, ComponentStateListener {
        volatile ServiceRegistration m_registration;
        private final Ensure m_ensure;
        
        public DynamicComponentStateListeningInstance(Ensure e) {
            m_ensure = e;
            m_ensure.step(1);
        }
        
        private void debug() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            System.out.println("AT: " + stackTrace[2].getClassName() + "." + stackTrace[2].getMethodName() + "():" + stackTrace[2].getLineNumber());
        }
        
        public void init(Component c) {
            debug();
            m_ensure.step(2);
            c.addStateListener(this);
        }
        
        public void start(Component c) {
            debug();
            m_ensure.step(4);
        }
        public void stop(Component c) {
            debug();
            m_ensure.step(7);
        }
        
        public void destroy(Component c) {
            debug();
            m_ensure.step(9);
            c.removeStateListener(this);
        }
        
        public void starting(Component component) {
            debug();
            m_ensure.step(3);
        }

        public void started(Component component) {
            debug();
            m_ensure.step(5);
            ServiceReference reference = m_registration.getReference();
            Assert.assertNotNull("Service not yet registered.", reference);
        }

        public void stopping(Component component) {
            debug();
            m_ensure.step(6);
        }

        public void stopped(Component component) {
            debug();
            m_ensure.step(8);
        }
    }
    
    
    @Test
    public void testDynamicComponentStateListingLifeCycle2() {
        
        // TODO this test still fails (starting is not invoked...)
        
        DependencyManager m = new DependencyManager(context);
        // helper class that ensures certain steps get executed in sequence
        Ensure e = new Ensure();
        // create a simple service component
        Component s = m.createComponent()
            .setInterface(MyInterface.class.getName(), null)
            .setImplementation(new DynamicComponentStateListeningInstance2(e));
        // add it, and since it has no dependencies, it should be activated immediately
        m.add(s);
        // remove it so it gets destroyed
        m.remove(s);
        // ensure we executed all steps inside the component instance
        e.step(10);
    }

    static class DynamicComponentStateListeningInstance2 implements MyInterface, ComponentStateListener {
        volatile ServiceRegistration m_registration;
        private final Ensure m_ensure;
        
        public DynamicComponentStateListeningInstance2(Ensure e) {
            m_ensure = e;
            m_ensure.step(1);
        }
        
        private void debug() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            System.out.println("AT: " + stackTrace[2].getClassName() + "." + stackTrace[2].getMethodName() + "():" + stackTrace[2].getLineNumber());
        }
        
        public void init(Component c) {
            debug();
            m_ensure.step(2);
        }
        
        public void start(Component c) {
            debug();
            m_ensure.step(3);
            c.addStateListener(this);
        }
        public void stop(Component c) {
            debug();
            m_ensure.step(7);
            c.removeStateListener(this);
        }
        
        public void destroy(Component c) {
            debug();
            m_ensure.step(9);
        }
        
        public void starting(Component component) {
            debug();
            m_ensure.step(4);
        }

        public void started(Component component) {
            debug();
            m_ensure.step(5);
            ServiceReference reference = m_registration.getReference();
            Assert.assertNotNull("Service not yet registered.", reference);
        }

        public void stopping(Component component) {
            debug();
            m_ensure.step(6);
        }

        public void stopped(Component component) {
            debug();
            m_ensure.step(8);
        }
    }
}
