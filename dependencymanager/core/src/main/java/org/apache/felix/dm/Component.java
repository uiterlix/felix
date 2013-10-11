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

import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.ServiceRegistration;

/**
 * Component interface. Components are the main building blocks for OSGi applications.
 * They can publish themselves as a service, and they can have dependencies. These
 * dependencies will influence their life cycle as component will only be activated
 * when all required dependencies are available.
 * 
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public interface Component {
    /**
     * Adds a new dependency to this component.
     * 
     * @param dependency the dependency to add
     * @return this component
     */
    public Component add(Dependency dependency);

    /**
     * Adds a list of new dependencies to this component.
     * 
     * @param dependencies the dependencies to add
     * @return this component
     */
    public Component add(List dependencies);
    
    /**
     * Removes a dependency from this component.
     * 
     * @param dependency the dependency to remove
     * @return this component
     */
    public Component remove(Dependency dependency);

    /**
     * Sets the public interface under which this component should be registered
     * in the OSGi service registry.
     *  
     * @param serviceName the name of the service interface
     * @param properties the properties for this service
     * @return this component
     */
    public Component setInterface(String serviceName, Dictionary properties);
    
    /**
     * Sets the public interfaces under which this component should be registered
     * in the OSGi service registry.
     *  
     * @param serviceNames the names of the service interface
     * @param properties the properties for these services
     * @return this component
     */
    public Component setInterface(String[] serviceNames, Dictionary properties);
    
    /**
     * Sets the implementation for this component. You can actually specify
     * an instance you have instantiated manually, or a <code>Class</code>
     * that will be instantiated using its default constructor when the
     * required dependencies are resolved, effectively giving you a lazy
     * instantiation mechanism.
     * 
     * There are four special methods that are called when found through
     * reflection to give you life cycle management options:
     * <ol>
     * <li><code>init()</code> is invoked after the instance has been
     * created and dependencies have been resolved, and can be used to
     * initialize the internal state of the instance or even to add more
     * dependencies based on runtime state</li>
     * <li><code>start()</code> is invoked right before the service is 
     * registered</li>
     * <li><code>stop()</code> is invoked right after the service is
     * unregistered</li>
     * <li><code>destroy()</code> is invoked after all dependencies are
     * removed</li>
     * </ol>
     * In short, this allows you to initialize your instance before it is
     * registered, perform some post-initialization and pre-destruction code
     * as well as final cleanup. If a method is not defined, it simply is not
     * called, so you can decide which one(s) you need. If you need even more
     * fine-grained control, you can register as a service state listener too.
     * 
     * @param implementation the implementation
     * @return this component
     * @see ComponentStateListener
     */
    public Component setImplementation(Object implementation);
    
    /**
     * Returns a list of dependencies.
     * 
     * @return a list of dependencies
     */
    public List getDependencies();
    
    /**
     * Returns the service registration for this component. The method
     * will return <code>null</code> if no service registration is
     * available, for example if this component is not registered as a
     * service at all.
     * 
     * @return the service registration
     */
    public ServiceRegistration getServiceRegistration();
    
    /**
     * Returns the component instance for this component. The method will
     * return <code>null</code> if no component instance is available.
     * 
     * @return the component instance
     */
    public Object getService();

    /**
     * Returns the service properties associated with the component.
     * 
     * @return the properties or <code>null</code> if there are none
     */
    public Dictionary getServiceProperties();
    
    /**
     * Sets the service properties associated with the component. If the service
     * was already registered, it will be updated.
     * 
     * @param serviceProperties the properties
     */
    public Component setServiceProperties(Dictionary serviceProperties);
    
    /**
     * Sets the names of the methods used as callbacks. These methods, when found, are
     * invoked as part of the life cycle management of the component implementation. The
     * dependency manager will look for a method of this name with the following signatures,
     * in this order:
     * <ol>
     * <li>method(Component component)</li>
     * <li>method()</li>
     * </ol>
     * 
     * @param init the name of the init method
     * @param start the name of the start method
     * @param stop the name of the stop method
     * @param destroy the name of the destroy method
     * @return the component
     */
    public Component setCallbacks(String init, String start, String stop, String destroy);
    /**
     * Sets the names of the methods used as callbacks. These methods, when found, are
     * invoked on the specified instance as part of the life cycle management of the component
     * implementation.
     * <p>
     * See setCallbacks(String init, String start, String stop, String destroy) for more
     * information on the signatures. Specifying an instance means you can create a manager
     * that will be invoked whenever the life cycle of a component changes and this manager
     * can then decide how to expose this life cycle to the actual component, offering an
     * important indirection when developing your own component models.
     */
    public Component setCallbacks(Object instance, String init, String start, String stop, String destroy);

    // listener
    /**
     * Adds a component state listener to this component.
     * 
     * @param listener the state listener
     */
    public void addStateListener(ComponentStateListener listener);

    /**
     * Removes a component state listener from this component.
     * 
     * @param listener the state listener
     */
    public void removeStateListener(ComponentStateListener listener);
    
    /**
     * Starts the component. This activates the dependency tracking mechanism
     * for this component.
     */
    public void start();
    
    /**
     * Stops the component. This deactivates the dependency tracking mechanism
     * for this component.
     */
    public void stop();
    
    /**
     * Sets the factory to use to create the implementation. You can specify
     * both the factory class and method to invoke. The method should return
     * the implementation, and can use any method to create it. Actually, this
     * can be used together with <code>setComposition</code> to create a
     * composition of instances that work together to implement a component. The
     * factory itself can also be instantiated lazily by not specifying an
     * instance, but a <code>Class</code>.
     * 
     * @param factory the factory instance or class
     * @param createMethod the name of the create method
     */
    public Component setFactory(Object factory, String createMethod);
	
	/**
	 * Sets the factory to use to create the implementation. You specify the
	 * method to invoke. The method should return the implementation, and can
	 * use any method to create it. Actually, this can be used together with
	 * <code>setComposition</code> to create a composition of instances that
	 * work together to implement a component.
	 * <p>
	 * Note that currently, there is no default for the factory, so please use
	 * <code>setFactory(factory, createMethod)</code> instead.
	 * 
	 * @param createMethod the name of the create method
	 */
	public Component setFactory(String createMethod);
	
	/**
	 * Sets the instance and method to invoke to get back all instances that
	 * are part of a composition and need dependencies injected. All of them
	 * will be searched for any of the dependencies. The method that is
	 * invoked must return an <code>Object[]</code>.
	 * 
	 * @param instance the instance that has the method
	 * @param getMethod the method to invoke
	 */
	public Component setComposition(Object instance, String getMethod);
	
	/**
	 * Sets the method to invoke on the service implementation to get back all
	 * instances that are part of a composition and need dependencies injected.
	 * All of them will be searched for any of the dependencies. The method that
	 * is invoked must return an <code>Object[]</code>.
	 * 
	 * @param getMethod the method to invoke
	 */
	public Component setComposition(String getMethod);
	
	/**
	 * Returns the composition instances that make up this component, or just the
	 * component instance if it does not have a composition, or an empty array if
	 * the component has not even been instantiated.
	 */
	public Object[] getCompositionInstances();
	
	/**
	 * Invokes a callback method on an array of instances. The method, whose name
	 * and signatures you provide, along with the values for the parameters, is
	 * invoked on all the instances where it exists.
	 * 
	 * @see InvocationUtil#invokeCallbackMethod(Object, String, Class[][], Object[][])
	 * 
	 * @param instances an array of instances to invoke the method on
	 * @param methodName the name of the method
	 * @param signatures the signatures of the method
	 * @param parameters the parameter values
	 */
    public boolean invokeCallbackMethod(Object[] instances, String methodName, Class[][] signatures, Object[][] parameters);
	
	/**
	 * Returns the dependency manager associated with this component.
	 */
	public DependencyManager getDependencyManager();

	/**
	 * Configures auto configuration of injected classes in the component instance.
	 * The following injections are currently performed, unless you explicitly
	 * turn them off:
	 * <dl>
	 * <dt>BundleContext</dt><dd>the bundle context of the bundle</dd>
     * <dt>ServiceRegistration</dt><dd>the service registration used to register your service</dd>
     * <dt>DependencyManager</dt><dd>the dependency manager instance</dd>
     * <dt>Component</dt><dd>the component instance of the dependency manager</dd>
	 * </dl>
	 * 
	 * @param clazz the class (from the list above)
	 * @param autoConfig <code>false</code> to turn off auto configuration
	 */
    public Component setAutoConfig(Class clazz, boolean autoConfig);
    
    /**
     * Configures auto configuration of injected classes in the component instance.
     * 
     * @param clazz the class (from the list above)
     * @param instanceName the name of the instance to inject the class into
     * @see setAutoConfig(Class, boolean)
     */
    public Component setAutoConfig(Class clazz, String instanceName);

    /**
     * Returns the status of auto configuration of the specified class.
     */
    public boolean getAutoConfig(Class clazz);
    
    /**
     * Returns the instance variable name of auto configuration of the specified class.
     */
    public String getAutoConfigInstance(Class clazz);
}
