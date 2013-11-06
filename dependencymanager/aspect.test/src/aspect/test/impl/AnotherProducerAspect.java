package aspect.test.impl;

import org.apache.felix.dm.Component;

import aspect.test.Producer;

public class AnotherProducerAspect implements Producer {

	private volatile Producer producer;
	
	private volatile Component component;
	
	@Override
	public String produce() {
		return "another aspect on " + producer.produce();
	}

}
