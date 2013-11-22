package properties.test.impl;

import java.util.Properties;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import properties.test.Producer;

public class Activator extends DependencyActivatorBase {

	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {

		System.out.println("Index configuration: " + System.getProperty("org.apache.felix.dependencymanager.filterindex"));
		
		System.out.println("Adding adapter");
		Component adapterComponent = createAdapterService(Producer.class, "(name=one)", "addProducer", "changeProducer", "removeProducer", "swapProducer")
				.setImplementation(AdapterImpl.class);
		
		manager.add(adapterComponent);
		System.out.println("Adding producer");
		Properties serviceProperties = new Properties();
		serviceProperties.setProperty("name", "one");
		Component producerComponent = createComponent().setInterface(Producer.class.getName(), serviceProperties).setImplementation(ProducerImpl.class);
		manager.add(producerComponent);

		System.out.println("Modifying service properties");
		Properties props = new Properties();
		producerComponent.setServiceProperties(props);
		
		System.out.println("Removing producer");
		manager.remove(producerComponent);

		System.out.println("Removing consumer");
		manager.remove(adapterComponent);
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		
	}

}
