package properties.test.impl;

import org.osgi.framework.ServiceReference;

import properties.test.Producer;

public class AdapterImpl {

	private volatile Producer producer;
	
	void addProducer(ServiceReference ref, Producer producer) {
		System.out.println("[AdapterImpl callback] add producer: " + producer.produce());
		this.producer = producer;
	}
	
	void swapProducer(ServiceReference oldRef, Producer oldProducer, ServiceReference ref, Producer producer) {
		System.out.println("[AdapterImpl callback] swap producer out " + oldProducer.produce());
		System.out.println("[AdapterImpl callback] swap producer in " + producer.produce());
		this.producer = producer;
	}
	
	void removeProducer(ServiceReference ref, Producer producer) {
		System.out.println("[AdapterImpl callback] remove producer " + producer.produce());
		this.producer = null;
	}
	
	void changeProducer() {
		System.out.println("[AdapterImpl callback] Producer service properties change");
	}

	public void consume() {
		System.out.println("[AdapterImpl callback] Consume: " + producer.produce());
		
	}
}
