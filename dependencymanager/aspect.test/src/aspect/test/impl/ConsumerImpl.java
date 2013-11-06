package aspect.test.impl;

import org.osgi.framework.ServiceReference;

import aspect.test.Producer;

public class ConsumerImpl {

	private volatile Producer producer;
	
	void addProducer(ServiceReference ref, Producer producer) {
		System.out.println("add producer: " + producer.produce());
		this.producer = producer;
	}
	
	void swapProducer(ServiceReference oldRef, Producer oldProducer, ServiceReference ref, Producer producer) {
		System.out.println("swap producer out " + oldProducer.produce());
		System.out.println("swap producer in " + producer.produce());
		this.producer = producer;
	}
	
	void removeProducer(ServiceReference ref, Producer producer) {
		System.out.println("remove producer " + producer.produce());
		this.producer = null;
	}

	public void consume() {
		System.out.println("Consume: " + producer.produce());
		
	}
}
