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


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.integration.components.SimpleComponent;
import org.apache.felix.scr.integration.components.SimpleServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.log.LogService;


@RunWith(JUnit4TestRunner.class)
public class ComponentFactoryTest extends ComponentTestBase
{

    private static final String PROP_NAME_FACTORY = ComponentTestBase.PROP_NAME + ".factory";

    static
    {
        descriptorFile = "/integration_test_simple_factory_components.xml";
        // uncomment to enable debugging of this test class
//        paxRunnerVmOption = DEBUG_VM_OPTION;
    }


    @Test
    public void test_component_factory() throws InvalidSyntaxException
    {
        final String componentname = "factory.component";
        final String componentfactory = "factory.component.factory";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        final ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        final ComponentInstance instance = factory.newInstance( props );
        TestCase.assertNotNull( instance );

        TestCase.assertNotNull( instance.getInstance() );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instance.getInstance() );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );

        final Map<?, ?> instanceMap = ( Map<?, ?> ) getFieldValue( component, "m_componentInstances" );
        TestCase.assertNotNull( instanceMap );
        TestCase.assertEquals( 1, instanceMap.size() );

        final Object instanceManager = getComponentManagerFromComponentInstance( instance );
        TestCase.assertTrue( instanceMap.containsValue( instanceManager ) );

        // check registered components
        final Component[] allFactoryComponents = findComponentsByName( componentname );
        TestCase.assertNotNull( allFactoryComponents );
        TestCase.assertEquals( 2, allFactoryComponents.length );
        for ( int i = 0; i < allFactoryComponents.length; i++ )
        {
            final Component c = allFactoryComponents[i];
            if ( c.getId() == component.getId() )
            {
                TestCase.assertEquals( Component.STATE_FACTORY, c.getState() );
            }
            else if ( c.getId() == SimpleComponent.INSTANCE.m_id )
            {
                TestCase.assertEquals( Component.STATE_ACTIVE, c.getState() );
            }
            else
            {
                TestCase.fail( "Unexpected Component " + c );
            }
        }

        instance.dispose();
        TestCase.assertNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( instance.getInstance() ); // SCR 112.12.6.2

        TestCase.assertEquals( 0, instanceMap.size() );
        TestCase.assertFalse( instanceMap.containsValue( instanceManager ) );
    }


    @Test
    public void test_component_factory_disable_factory() throws InvalidSyntaxException
    {
        // tests components remain alive after factory has been disabled

        final String componentname = "factory.component";
        final String componentfactory = "factory.component.factory";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        final ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        final ComponentInstance instance = factory.newInstance( props );
        TestCase.assertNotNull( instance );

        TestCase.assertNotNull( instance.getInstance() );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instance.getInstance() );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );

        final Map<?, ?> instanceMap = ( Map<?, ?> ) getFieldValue( component, "m_componentInstances" );
        TestCase.assertNotNull( instanceMap );
        TestCase.assertEquals( 1, instanceMap.size() );

        final Object instanceManager = getComponentManagerFromComponentInstance( instance );
        TestCase.assertTrue( instanceMap.containsValue( instanceManager ) );

        // disable the factory
        component.disable();
        delay();

        // factory is disabled but the instance is still alive
        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNotNull( SimpleComponent.INSTANCE );
        TestCase.assertEquals( 1, instanceMap.size() );
        TestCase.assertTrue( instanceMap.containsValue( instanceManager ) );

        instance.dispose();
        TestCase.assertNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( instance.getInstance() ); // SCR 112.12.6.2

        TestCase.assertEquals( 0, instanceMap.size() );
        TestCase.assertFalse( instanceMap.containsValue( instanceManager ) );
    }


    @Test
    public void test_component_factory_newInstance_failure() throws InvalidSyntaxException
    {
        final String componentname = "factory.component";
        final String componentfactory = "factory.component.factory";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        final ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        props.put( SimpleComponent.PROP_ACTIVATE_FAILURE, "Requested Failure" );
        try
        {
            final ComponentInstance instance = factory.newInstance( props );
            TestCase.assertNotNull( instance );
            TestCase.fail( "Expected newInstance method to fail with ComponentException" );
        }
        catch ( ComponentException ce )
        {
            // this is expected !
        }

        final Map<?, ?> instanceMap = ( Map<?, ?> ) getFieldValue( component, "m_componentInstances" );
        TestCase.assertNotNull( instanceMap );
        TestCase.assertTrue( instanceMap.isEmpty() );
        TestCase.assertNull( SimpleComponent.INSTANCE );
    }


    @Test
    public void test_component_factory_require_configuration() throws InvalidSyntaxException
    {
        final String componentname = "factory.component.configuration";
        final String componentfactory = "factory.component.factory.configuration";

        // ensure there is no configuration for the component
        deleteConfig( componentname );
        delay();

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        // At this point, since we don't have created the configuration, then the ComponentFactory
        // should not be available.
        
        ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNull( refs );
        
        // supply configuration now and ensure active
        configure( componentname );
        delay();        

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        // get the component factory service
        refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        // create an instance
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        final ComponentInstance instance = factory.newInstance( props );
        TestCase.assertNotNull( instance );

        final Object instanceObject = instance.getInstance();
        TestCase.assertNotNull( instanceObject );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instanceObject );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );
        TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );                        

        final Map<?, ?> instanceMap = ( Map<?, ?> ) getFieldValue( component, "m_componentInstances" );
        TestCase.assertNotNull( instanceMap );
        TestCase.assertEquals( 1, instanceMap.size() );

        final Object instanceManager = getComponentManagerFromComponentInstance( instance );
        TestCase.assertTrue( instanceMap.containsValue( instanceManager ) );

        // delete config, ensure factory is not active anymore and component instance not changed
        deleteConfig( componentname );
        delay();
        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );

        TestCase.assertNotNull( instance.getInstance() );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instance.getInstance() );
        TestCase.assertEquals( instanceObject, instance.getInstance() );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );
        TestCase.assertEquals( PROP_NAME, SimpleComponent.INSTANCE.getProperty( PROP_NAME ) );

        instance.dispose();
        TestCase.assertNull( SimpleComponent.INSTANCE ); // component is deactivated
        TestCase.assertNull( instance.getInstance() ); // SCR 112.12.6.2

        // with removal of the factory, the created instance should also be removed
        TestCase.assertEquals( 0, instanceMap.size() );
        TestCase.assertFalse( instanceMap.containsValue( instanceManager ) );
    }

    @Test
    public void test_component_factory_reference() throws InvalidSyntaxException
    {
        final String componentname = "factory.component.reference";
        final String componentfactory = "factory.component.factory.reference";

        SimpleServiceImpl.create( bundleContext, "ignored" ).setFilterProperty( "ignored" );

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        // missing reference -> unsatisfied
        TestCase.assertEquals( Component.STATE_UNSATISFIED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        // register a service : filterprop=match
        SimpleServiceImpl match = SimpleServiceImpl.create( bundleContext, "required" ).setFilterProperty( "required" );
        delay();

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        final ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        // non-overwrite filterprop
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        final ComponentInstance instance = factory.newInstance( props );
        TestCase.assertNotNull( instance );

        TestCase.assertNotNull( instance.getInstance() );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instance.getInstance() );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );
        TestCase.assertEquals( 1, SimpleComponent.INSTANCE.m_multiRef.size() );
        TestCase.assertTrue( SimpleComponent.INSTANCE.m_multiRef.contains( match ) );

        final Map<?, ?> instanceMap = ( Map<?, ?> ) getFieldValue( component, "m_componentInstances" );
        TestCase.assertNotNull( instanceMap );
        TestCase.assertEquals( 1, instanceMap.size() );

        final Object instanceManager = getComponentManagerFromComponentInstance( instance );
        TestCase.assertTrue( instanceMap.containsValue( instanceManager ) );

        // check registered components
        final Component[] allFactoryComponents = findComponentsByName( componentname );
        TestCase.assertNotNull( allFactoryComponents );
        TestCase.assertEquals( 2, allFactoryComponents.length );
        for ( int i = 0; i < allFactoryComponents.length; i++ )
        {
            final Component c = allFactoryComponents[i];
            if ( c.getId() == component.getId() )
            {
                TestCase.assertEquals( Component.STATE_FACTORY, c.getState() );
            }
            else if ( c.getId() == SimpleComponent.INSTANCE.m_id )
            {
                TestCase.assertEquals( Component.STATE_ACTIVE, c.getState() );
            }
            else
            {
                TestCase.fail( "Unexpected Component " + c );
            }
        }

        instance.dispose();
        TestCase.assertNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( instance.getInstance() ); // SCR 112.12.6.2

        TestCase.assertEquals( 0, instanceMap.size() );
        TestCase.assertFalse( instanceMap.containsValue( instanceManager ) );

        // overwritten filterprop
        Hashtable<String, String> propsNonMatch = new Hashtable<String, String>();
        propsNonMatch.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        propsNonMatch.put( "ref.target", "(filterprop=nomatch)" );
        try
        {
            factory.newInstance( propsNonMatch );
            TestCase.fail( "Missing reference must fail instance creation" );
        }
        catch ( ComponentException ce )
        {
            // expected
        }

        final SimpleServiceImpl noMatch = SimpleServiceImpl.create( bundleContext, "nomatch" ).setFilterProperty(
            "nomatch" );
        delay();

        final ComponentInstance instanceNonMatch = factory.newInstance( propsNonMatch );

        TestCase.assertNotNull( instanceNonMatch );

        TestCase.assertNotNull( instanceNonMatch.getInstance() );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instanceNonMatch.getInstance() );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );

        TestCase.assertEquals( 1, SimpleComponent.INSTANCE.m_multiRef.size() );
        TestCase.assertTrue( SimpleComponent.INSTANCE.m_multiRef.contains( noMatch ) );

        // check registered components
        final Component[] allFactoryComponents2 = findComponentsByName( componentname );
        TestCase.assertNotNull( allFactoryComponents2 );
        TestCase.assertEquals( 2, allFactoryComponents2.length );
        for ( int i = 0; i < allFactoryComponents2.length; i++ )
        {
            final Component c = allFactoryComponents2[i];
            if ( c.getId() == component.getId() )
            {
                TestCase.assertEquals( Component.STATE_FACTORY, c.getState() );
            }
            else if ( c.getId() == SimpleComponent.INSTANCE.m_id )
            {
                TestCase.assertEquals( Component.STATE_ACTIVE, c.getState() );
            }
            else
            {
                TestCase.fail( "Unexpected Component " + c );
            }
        }

        match.getRegistration().unregister();
        delay();

        // check registered components (ComponentFactory aint no longer)
        final Component[] allFactoryComponents3 = findComponentsByName( componentname );
        TestCase.assertNotNull( allFactoryComponents3 );
        TestCase.assertEquals( 2, allFactoryComponents3.length );
        for ( int i = 0; i < allFactoryComponents3.length; i++ )
        {
            final Component c = allFactoryComponents3[i];
            if ( c.getId() == component.getId() )
            {
                TestCase.assertEquals( Component.STATE_UNSATISFIED, c.getState() );
            }
            else if ( c.getId() == SimpleComponent.INSTANCE.m_id )
            {
                TestCase.assertEquals( Component.STATE_ACTIVE, c.getState() );
            }
            else
            {
                TestCase.fail( "Unexpected Component " + c );
            }
        }

        //it has already been deactivated.... this should cause an exception?
        noMatch.getRegistration().unregister();
        delay();

        // check registered components (ComponentFactory aint no longer)
        final Component[] allFactoryComponents4 = findComponentsByName( componentname );
        TestCase.assertNotNull( allFactoryComponents4 );
        TestCase.assertEquals( 1, allFactoryComponents4.length );
        for ( int i = 0; i < allFactoryComponents4.length; i++ )
        {
            final Component c = allFactoryComponents4[i];
            if ( c.getId() == component.getId() )
            {
                TestCase.assertEquals( Component.STATE_UNSATISFIED, c.getState() );
            }
            else
            {
                TestCase.fail( "Unexpected Component " + c );
            }
        }

        // deactivated due to unsatisfied reference
        TestCase.assertNull( instanceNonMatch.getInstance() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        //Check that calling dispose on a deactivated instance has no effect
        instanceNonMatch.dispose();
        TestCase.assertNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( instanceNonMatch.getInstance() ); // SCR 112.12.6.2
    }

    @Test
    public void test_component_factory_referredTo() throws InvalidSyntaxException
    {
        //set up the component that refers to the service the factory will create.
        final String referringComponentName = "ComponentReferringToFactoryObject";
        final Component referringComponent = findComponentByName( referringComponentName );
        TestCase.assertNotNull( referringComponent );
        referringComponent.enable();
        delay();

        //make sure it's unsatisfied (service is not yet available
        TestCase.assertEquals( Component.STATE_UNSATISFIED, referringComponent.getState() );


        final String componentname = "factory.component.referred";
        final String componentfactory = "factory.component.factory.referred";

        final Component component = findComponentByName( componentname );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        final ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        // create the factory instance
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( "service.pid", "myFactoryInstance" );
        final ComponentInstance instance = factory.newInstance( props );
        TestCase.assertNotNull( instance );

        TestCase.assertNotNull( instance.getInstance() );

        //The referring service should now be active
        TestCase.assertEquals( Component.STATE_ACTIVE, referringComponent.getState() );

        instance.dispose();
        TestCase.assertNull( instance.getInstance() ); // SCR 112.12.6.2

        //make sure it's unsatisfied (service is no longer available)
        TestCase.assertEquals( Component.STATE_UNSATISFIED, referringComponent.getState() );
    }

    @Test
    public void test_component_factory_with_target_filters() throws InvalidSyntaxException
    {
        final String componentfactory = "factory.component.reference.targetfilter";
        final Component component = findComponentByName( componentfactory );

        TestCase.assertNotNull( component );
        TestCase.assertFalse( component.isDefaultEnabled() );

        TestCase.assertEquals( Component.STATE_DISABLED, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );

        component.enable();
        delay();

        SimpleServiceImpl s1 = SimpleServiceImpl.create(bundleContext, "service1");
        SimpleServiceImpl s2 = SimpleServiceImpl.create(bundleContext, "service2");

        // supply configuration now and ensure active
        configure( componentfactory );
        delay();        

        TestCase.assertEquals( Component.STATE_FACTORY, component.getState() );
        TestCase.assertNull( SimpleComponent.INSTANCE );
        
        final ServiceReference[] refs = bundleContext.getServiceReferences( ComponentFactory.class.getName(), "("
            + ComponentConstants.COMPONENT_FACTORY + "=" + componentfactory + ")" );
        TestCase.assertNotNull( refs );
        TestCase.assertEquals( 1, refs.length );
        final ComponentFactory factory = ( ComponentFactory ) bundleContext.getService( refs[0] );
        TestCase.assertNotNull( factory );

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put( PROP_NAME_FACTORY, PROP_NAME_FACTORY );
        props.put("ref.target", "(value=service2)");
        final ComponentInstance instance = factory.newInstance( props );
        TestCase.assertNotNull( instance );

        TestCase.assertNotNull( instance.getInstance() );
        TestCase.assertEquals( SimpleComponent.INSTANCE, instance.getInstance() );
        TestCase.assertEquals( PROP_NAME_FACTORY, SimpleComponent.INSTANCE.getProperty( PROP_NAME_FACTORY ) );

        log.log(LogService.LOG_WARNING, "Bound Services: " +  SimpleComponent.INSTANCE.m_multiRef);
        TestCase.assertFalse( SimpleComponent.INSTANCE.m_multiRef.contains( s1 ) );
        TestCase.assertTrue( SimpleComponent.INSTANCE.m_multiRef.contains( s2 ) );

        instance.dispose();
        TestCase.assertNull( SimpleComponent.INSTANCE );
        TestCase.assertNull( instance.getInstance() ); // SCR 112.12.6.2
        
        s2.drop();
        s1.drop();
    }

}
