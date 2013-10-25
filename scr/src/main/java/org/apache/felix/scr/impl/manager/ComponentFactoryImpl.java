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
package org.apache.felix.scr.impl.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.impl.BundleComponentActivator;
import org.apache.felix.scr.impl.TargetedPID;
import org.apache.felix.scr.impl.config.ComponentHolder;
import org.apache.felix.scr.impl.helper.ComponentMethods;
import org.apache.felix.scr.impl.metadata.ComponentMetadata;
import org.apache.felix.scr.impl.metadata.ReferenceMetadata;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.log.LogService;

/**
 * The <code>ComponentFactoryImpl</code> extends the {@link AbstractComponentManager}
 * class to implement the component factory functionality. As such the
 * OSGi Declarative Services <code>ComponentFactory</code> interface is
 * implemented.
 * <p>
 * In addition the {@link ComponentHolder} interface is implemented to use this
 * class directly as the holder for component instances created by the
 * {@link #newInstance(Dictionary)} method.
 * <p>
 * Finally, if the <code>ds.factory.enabled</code> bundle context property is
 * set to <code>true</code>, component instances can be created by factory
 * configurations. This functionality is present for backwards compatibility
 * with earlier releases of the Apache Felix Declarative Services implementation.
 * But keep in mind, that this is non-standard behaviour.
 */
public class ComponentFactoryImpl<S> extends AbstractComponentManager<S> implements ComponentFactory, ComponentHolder
{

    /**
     * Contains the component instances created by calling the
     * {@link #newInstance(Dictionary)} method. These component instances are
     * provided with updated configuration (or deleted configuration) if
     * such modifications for the component factory takes place.
     * <p>
     * The map is keyed by the component manager instances. The value of each
     * entry is the same as the entry's key.
     * This is an IdentityHashMap for speed, thus not a Set.
     */
    private final Map<SingleComponentManager<S>, SingleComponentManager<S>> m_componentInstances;

    /**
     * The configuration for the component factory. This configuration is
     * supplied as the base configuration for each component instance created
     * by the {@link #newInstance(Dictionary)} method.
     */
    private volatile Dictionary<String, Object> m_configuration;
    
    /**
     * Flag telling if our component factory is currently configured from config admin.
     * We are configured when configuration policy is required and we have received the
     * config admin properties, or when configuration policy is optional or ignored.
     */
    private volatile boolean m_hasConfiguration;
    
    /**
     * Configuration change count (R5) or imitation (R4)
     */
    protected volatile long m_changeCount = -1;
    
    protected TargetedPID m_targetedPID;

    public ComponentFactoryImpl( BundleComponentActivator activator, ComponentMetadata metadata )
    {
        super( activator, metadata, new ComponentMethods() );
        m_componentInstances = new IdentityHashMap<SingleComponentManager<S>, SingleComponentManager<S>>();
        m_configuration = new Hashtable<String, Object>();
    }


    @Override
    public boolean isFactory()
    {
        return true;
    }

    /* (non-Javadoc)
    * @see org.osgi.service.component.ComponentFactory#newInstance(java.util.Dictionary)
    */
    public ComponentInstance newInstance( Dictionary<String, ?> dictionary )
    {
        final SingleComponentManager<S> cm = createComponentManager();
        log( LogService.LOG_DEBUG, "Creating new instance from component factory {0} with configuration {1}",
                new Object[] {getComponentMetadata().getName(), dictionary}, null );

        ComponentInstance instance;
        cm.setFactoryProperties( ( Dictionary<String, Object> ) dictionary );
        //configure the properties
        cm.reconfigure( m_configuration, m_changeCount, m_targetedPID );
        // enable
        cm.enableInternal();
        //activate immediately
        cm.activateInternal( getTrackingCount().get() );

        instance = cm.getComponentInstance();
        if ( instance == null || instance.getInstance() == null )
        {
            // activation failed, clean up component manager
            cm.dispose( ComponentConstants.DEACTIVATION_REASON_DISPOSED );
            throw new ComponentException( "Failed activating component" );
        }

        synchronized ( m_componentInstances )
        {
            m_componentInstances.put( cm, cm );
        }

        return instance;
    }

    /**
     * Compares this {@code ComponentFactoryImpl} object to another object.
     * 
     * <p>
     * A component factory impl is considered to be <b>equal to </b> another component
     * factory impl if the component names are equal(using {@code String.equals}).
     * 
     * @param object The {@code ComponentFactoryImpl} object to be compared.
     * @return {@code true} if {@code object} is a
     *         {@code ComponentFactoryImpl} and is equal to this object;
     *         {@code false} otherwise.
     */
   public boolean equals(Object object)
    {
        if (!(object instanceof ComponentFactoryImpl))
        {
            return false;
        }

        ComponentFactoryImpl other = (ComponentFactoryImpl) object;
        return getComponentMetadata().getName().equals(other.getComponentMetadata().getName());
    }
    
   /**
    * Returns a hash code value for the object.
    * 
    * @return An integer which is a hash code value for this object.
    */
   public int hashCode()
   {
       return getComponentMetadata().getName().hashCode();
   }

    /**
     * The component factory does not have a component to create.
     */
    protected boolean createComponent()
    {
        return true;
    }


    /**
     * The component factory does not have a component to delete.
     * <p>
     * But in the backwards compatible case any instances created for factory
     * configuration instances are to disabled as a consequence of deactivating
     * the component factory.
     */
    protected void deleteComponent( int reason )
    {
    }


    @Override
    protected String[] getProvidedServices()
    {
        return new String[] { ComponentFactory.class.getName() };
    }


    public boolean hasConfiguration()
    {
        return m_hasConfiguration;
    }


    public Dictionary<String, Object> getProperties()
    {
        Dictionary<String, Object> props = getServiceProperties();

        // add target properties of references
        List<ReferenceMetadata> depMetaData = getComponentMetadata().getDependencies();
        for ( ReferenceMetadata rm : depMetaData )
        {
            if ( rm.getTarget() != null )
            {
                props.put( rm.getTargetPropertyName(), rm.getTarget() );
            }
        }

        // add target properties from configuration (if we have one)        
        for ( String key : Collections.list( m_configuration.keys() ) )
        {
            if ( key.endsWith( ".target" ) )
            {
                props.put( key, m_configuration.get( key ) );
            }
        }

        return props;
    }
    
    public void setServiceProperties( Dictionary serviceProperties )
    {
        throw new IllegalStateException( "ComponentFactory service properties are immutable" );
    }


    public Dictionary<String, Object> getServiceProperties()
    {
        Dictionary<String, Object> props = new Hashtable<String, Object>();

        // 112.5.5 The Component Factory service must register with the following properties
        props.put( ComponentConstants.COMPONENT_NAME, getComponentMetadata().getName() );
        props.put( ComponentConstants.COMPONENT_FACTORY, getComponentMetadata().getFactoryIdentifier() );

        props.put( Constants.SERVICE_VENDOR, "The Apache Software Foundation" );

        return props;
    }

    boolean hasInstance()
    {
        return false;
    }

    protected boolean collectDependencies()
    {
        return true;
    }

    <T> void invokeUpdatedMethod( DependencyManager<S, T> dependencyManager, RefPair<T> ref, int trackingCount )
    {
    }

    <T> void invokeBindMethod( DependencyManager<S, T> dependencyManager, RefPair<T> reference, int trackingCount )
    {
    }

    <T> void invokeUnbindMethod( DependencyManager<S, T> dependencyManager, RefPair<T> oldRef, int trackingCount )
    {
    }

    //---------- Component interface


    public ComponentInstance getComponentInstance()
    {
        // a ComponentFactory is not a real component and as such does
        // not have a ComponentInstance
        return null;
    }


    //---------- ComponentHolder interface

    public void configurationDeleted( String pid )
    {
        m_targetedPID = null;
        if ( pid.equals( getComponentMetadata().getConfigurationPid() ) )
        {
            log( LogService.LOG_DEBUG, "Handling configuration removal", null );

            m_changeCount = -1;
            // nothing to do if there is no configuration currently known.
            if ( !m_hasConfiguration )
            {
                log( LogService.LOG_DEBUG, "ignoring configuration removal: not currently configured", null );
                return;
            }

            // So far, we were configured: clear the current configuration.
            m_hasConfiguration = false;
            m_configuration = new Hashtable();

            log( LogService.LOG_DEBUG, "Current component factory state={0}", new Object[] {getState()}, null );

            // And deactivate if we are not currently disposed and if configuration is required
            if ( ( getState() & STATE_DISPOSED ) == 0 && getComponentMetadata().isConfigurationRequired() )
            {
                log( LogService.LOG_DEBUG, "Deactivating component factory (required configuration has gone)", null );
                deactivateInternal( ComponentConstants.DEACTIVATION_REASON_CONFIGURATION_DELETED, true, false );
            }
        }
        else
        {
            // 112.7 Factory Configuration not allowed for factory component
            log( LogService.LOG_ERROR, "Component Factory cannot be configured by factory configuration", null );
        }
    }


    public boolean configurationUpdated( String pid, Dictionary<String, Object> configuration, long changeCount, TargetedPID targetedPid )
    {
        if ( m_targetedPID != null && !m_targetedPID.equals( targetedPid ))
        {
            log( LogService.LOG_ERROR, "ImmediateComponentHolder unexpected change in targetedPID from {0} to {1}",
                    new Object[] {m_targetedPID, targetedPid}, null);
            throw new IllegalStateException("Unexpected targetedPID change");
        }
        m_targetedPID = targetedPid;
        if ( configuration != null )
        {
            if ( changeCount <= m_changeCount )
            {
                log( LogService.LOG_DEBUG,
                        "ImmediateComponentHolder out of order configuration updated for pid {0} with existing count {1}, new count {2}",
                        new Object[] { getConfigurationPid(), m_changeCount, changeCount }, null );
                return false;
            }
            m_changeCount = changeCount;
        }
        else 
        {
            m_changeCount = -1;
        }
        if ( pid.equals( getComponentMetadata().getConfigurationPid() ) )
        {
            log( LogService.LOG_INFO, "Configuration PID updated for Component Factory", null );

            // Ignore the configuration if our policy is 'ignore'
            if ( getComponentMetadata().isConfigurationIgnored() )
            {
                return false;
            }

            // Store the config admin configuration
            m_configuration = configuration;

            // We are now configured from config admin.
            m_hasConfiguration = true;

            log( LogService.LOG_INFO, "Current ComponentFactory state={0}", new Object[]
                    {getState()}, null );

            // If we are active, but if some config target filters don't match anymore
            // any required references, then deactivate.
            if ( getState() == STATE_FACTORY )
            {
                log( LogService.LOG_INFO, "Verifying if Active Component Factory is still satisfied", null );

                // First update target filters.
                updateTargets( getProperties() );

                // Next, verify dependencies
                if ( !verifyDependencyManagers() )
                {
                    log( LogService.LOG_DEBUG,
                            "Component Factory target filters not satisfied anymore: deactivating", null );
                    deactivateInternal( ComponentConstants.DEACTIVATION_REASON_REFERENCE, false, false );
                    return false;
                }
            }

            // Unsatisfied component and required configuration may change targets
            // to satisfy references.
            if ( getState() == STATE_UNSATISFIED && getComponentMetadata().isConfigurationRequired() )
            {
                // try to activate our component factory, if all dependnecies are satisfied
                log( LogService.LOG_DEBUG, "Attempting to activate unsatisfied component", null );
                // First update target filters.
                updateTargets( getProperties() );
                activateInternal( getTrackingCount().get() );
            }
        }
        else
        {
            // 112.7 Factory Configuration not allowed for factory component
            log( LogService.LOG_ERROR, "Component Factory cannot be configured by factory configuration", null );
        }
        return false;
    }


    public synchronized long getChangeCount( String pid)
    {
        
        return m_changeCount;
    }

    public Component[] getComponents()
    {
        List<AbstractComponentManager<S>> cms = getComponentList();
        return cms.toArray( new Component[ cms.size() ] );
    }

    protected List<AbstractComponentManager<S>> getComponentList()
    {
        List<AbstractComponentManager<S>> cms = new ArrayList<AbstractComponentManager<S>>( );
        cms.add( this );
        getComponentManagers( m_componentInstances, cms );
        return cms;
    }


    /**
     * A component factory component holder enables the held components by
     * enabling itself.
     */
    public void enableComponents( boolean async )
    {
        enable( async );
    }


    /**
     * A component factory component holder disables the held components by
     * disabling itself.
     */
    public void disableComponents( boolean async )
    {
        disable( async );
    }


    /**
     * Disposes off all components ever created by this component holder. This
     * method is called if either the Declarative Services runtime is stopping
     * or if the owning bundle is stopped. In both cases all components created
     * by this holder must be disposed off.
     */
    public void disposeComponents( int reason )
    {
        List<AbstractComponentManager<S>> cms = new ArrayList<AbstractComponentManager<S>>( );
        getComponentManagers( m_componentInstances, cms );
        for ( AbstractComponentManager acm: cms )
        {
            acm.dispose( reason );
        }

        synchronized ( m_componentInstances )
        {
            m_componentInstances.clear();
        }

        // finally dispose the component factory itself
        dispose( reason );
    }


    public void disposed( SingleComponentManager component )
    {
        synchronized ( m_componentInstances )
        {
            m_componentInstances.remove( component );
        }
    }


    //---------- internal


    /**
     * Creates an {@link SingleComponentManager} instance with the
     * {@link BundleComponentActivator} and {@link ComponentMetadata} of this
     * instance. The component manager is kept in the internal set of created
     * components. The component is neither configured nor enabled.
     */
    private SingleComponentManager<S> createComponentManager()
    {
        return new ComponentFactoryNewInstance<S>( getActivator(), this, getComponentMetadata(), getComponentMethods() );
    }


    protected void getComponentManagers( Map<?, SingleComponentManager<S>> componentMap, List<AbstractComponentManager<S>> componentManagers )
    {
        if ( componentMap != null )
        {
            synchronized ( componentMap )
            {
                componentManagers.addAll( componentMap.values() );
            }
        }
    }

    static class ComponentFactoryNewInstance<S> extends SingleComponentManager<S> {

        public ComponentFactoryNewInstance( BundleComponentActivator activator, ComponentHolder componentHolder,
                ComponentMetadata metadata, ComponentMethods componentMethods )
        {
            super( activator, componentHolder, metadata, componentMethods, true );
        }

    }

    public TargetedPID getConfigurationTargetedPID(TargetedPID pid)
    {
        return m_targetedPID;
    }


}
