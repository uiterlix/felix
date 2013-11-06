package aspect.test.impl;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import aspect.test.Producer;

public class Activator extends DependencyActivatorBase {

	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		System.out.println("Adding producer");
		Component producerComponent = createComponent().setInterface(Producer.class.getName(), null).setImplementation(ProducerImpl.class);
		manager.add(producerComponent);
		System.out.println("Adding producer aspect");
		Component aspectComponent = createAspectService(Producer.class, null, 1000).setImplementation(ProducerAspect.class);
		manager.add(aspectComponent);
		System.out.println("Adding another producer aspect");
		Component anotherAspectComponent = createAspectService(Producer.class, null, 2000).setImplementation(AnotherProducerAspect.class);
		manager.add(anotherAspectComponent);		
		System.out.println("Adding consumer");
		ConsumerImpl consumer = new ConsumerImpl();
		manager.add(createComponent().setImplementation(consumer)
				.add(createServiceDependency().setService(Producer.class)
						.setCallbacks("addProducer", null, "removeProducer", "swapProducer").setRequired(true)));
		System.out.println("Removing aspect");
		manager.remove(aspectComponent);
		System.out.println("Removing producer");
		manager.remove(producerComponent);
		System.out.println("Add producer aspect");
		manager.add(aspectComponent);
		System.out.println("Add producer");
		manager.add(producerComponent);
		consumer.consume();
		System.out.println("Remove another producer aspect");
		manager.remove(anotherAspectComponent);
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
		
	}

}
