package aspect.test.impl;

import aspect.test.Producer;

public class ProducerImpl implements Producer {

	@Override
	public String produce() {
		return "producer";
	}

}
