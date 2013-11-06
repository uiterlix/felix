package index.test;

import java.util.Properties;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		
		Properties serviceProperties = new Properties();
		serviceProperties.setProperty("name", "service implementation");
		manager.add(createComponent().setInterface(ServiceInterface.class.getName(), serviceProperties)
				.setImplementation(ServiceImpl.class));
		
		manager.add(createComponent().setImplementation(this)
				.add(createServiceDependency().setService(ServiceInterface.class, "(name=service implementation)").setRequired(true)));
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		
	}
	
	void start() {
		System.out.println("Start...");
	}

}
