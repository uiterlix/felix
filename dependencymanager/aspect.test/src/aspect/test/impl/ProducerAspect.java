package aspect.test.impl;

import aspect.test.Producer;

public class ProducerAspect implements Producer {

	private volatile Producer producer;
	
	@Override
	public String produce() {
		return "aspect on " + producer.produce();
	}

}
