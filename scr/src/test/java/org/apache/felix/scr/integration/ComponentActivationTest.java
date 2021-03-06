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
package org.apache.felix.scr.integration;


import junit.framework.TestCase;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.integration.components.ActivatorComponent;
import org.apache.felix.scr.integration.components.SimpleService;
import org.apache.felix.scr.integration.components.SimpleServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.ServiceReference;


@RunWith(JUnit4TestRunner.class)
public class ComponentActivationTest extends ComponentTestBase
{

    static
    {
        // use different components
        descriptorFile = "/integration_test_activation_components.xml";

        // uncomment to enable debugging of this test class
        // paxRunnerVmOption = DEBUG_VM_OPTION;
    }


    @Test
    public void test_activator_not_declared()
    {
        final String componentname = "ActivatorComponent.no.decl";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


//    @Test  I think this test is wrong.  Failure to activate does not mean that the state changes from Registered.
    public void test_activate_missing()
    {
        final String componentname = "ActivatorComponent.activate.missing";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        // activate must fail
        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


    @Test
    public void test_deactivate_missing()
    {
        final String componentname = "ActivatorComponent.deactivate.missing";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


    @Test
    public void test_activator_declared()
    {
        final String componentname = "ActivatorComponent.decl";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


//    @Test  Failure to activate does not mean the state should change to unsatisfied.
    public void test_activate_fail()
    {
        final String componentname = "ActivatorComponent.activate.fail";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        // activate has failed
        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


    @Test
    public void test_deactivate_fail()
    {
        final String componentname = "ActivatorComponent.deactivate.fail";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


    @Test
    public void test_activate_register_service()
    {
        final String componentname = "ActivatorComponent.activate.with.bind";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        ActivatorComponent ac = (ActivatorComponent) component.getComponentInstance().getInstance();
        TestCase.assertNotNull( ac.getSimpleService() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }


    @Test
    public void test_activate_register_service_delayed()
    {
        final String componentname = "ActivatorComponent.activate.delayed.with.bind";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_REGISTERED, component.getState() );

        ServiceReference<ActivatorComponent> ref = bundleContext.getServiceReference( ActivatorComponent.class );
        ActivatorComponent ac = bundleContext.getService( ref );
        TestCase.assertNotNull( ac.getSimpleService() );

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }
    
    @Test
    public void test_activate_service_factory_register_service()
    {
        final String componentname = "ActivatorComponent.activate.service.factory.with.bind";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_REGISTERED, component.getState() );

        ServiceReference<ActivatorComponent> ref = bundleContext.getServiceReference( ActivatorComponent.class );
        ActivatorComponent ac = bundleContext.getService( ref );
        TestCase.assertNotNull( ac.getSimpleService() );

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }
    
    @Test
    public void test_activate_register_service_single_static_dependency()
    {
        final String componentname = "ActivatorComponent.bind.single.static";

        testRequiredDependency( componentname );
    }

    @Test
    public void test_activate_register_service_multiple_static_reluctant_dependency()
    {
        final String componentname = "ActivatorComponent.bind.multiple.static.reluctant";

        testRequiredDependency( componentname );
    }

    @Test
    public void test_activate_register_service_multiple_static_greedy_dependency()
    {
        final String componentname = "ActivatorComponent.bind.multiple.static.greedy";

        testRequiredDependency( componentname );
    }

    @Test
    public void test_activate_register_service_single_dynamic_dependency()
    {
        final String componentname = "ActivatorComponent.bind.single.dynamic";

        testRequiredDependency( componentname );
    }

    @Test
    public void test_activate_register_service_multiple_dynamic_dependency()
    {
        final String componentname = "ActivatorComponent.bind.multiple.dynamic";

        testRequiredDependency( componentname );
    }


    private void testRequiredDependency(final String componentname)
    {
        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );
        
        SimpleServiceImpl ss = SimpleServiceImpl.create( bundleContext, "foo" );
        
        TestCase.assertEquals( Component.STATE_REGISTERED, component.getState() );

        ServiceReference<ActivatorComponent> ref = bundleContext.getServiceReference( ActivatorComponent.class );
        
        ss.drop();
        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );
        
        TestCase.assertNull(bundleContext.getServiceReference( ActivatorComponent.class ));
        ss = SimpleServiceImpl.create( bundleContext, "foo" );
        ref = bundleContext.getServiceReference( ActivatorComponent.class );
        ActivatorComponent ac = bundleContext.getService( ref );
        TestCase.assertNotNull( ac.getSimpleService() );

        TestCase.assertEquals( Component.STATE_ACTIVE, component.getState() );

        component.disable();

        delay();
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
    }

}
