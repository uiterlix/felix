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
package org.apache.felix.dm.test.integration.annotations;

import org.apache.felix.dm.test.components.Ensure;
import org.apache.felix.dm.test.components.AspectLifecycleAnnotation.ServiceProvider;
import org.apache.felix.dm.test.components.AspectLifecycleAnnotation.ServiceProviderAspect;
import org.apache.felix.dm.test.integration.common.TestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.ServiceRegistration;

/**
 * Use case: Tests an aspect service, and ensure that its lifecycle methods are properly invoked 
 * (init/start/stop/destroy methods).
 */
@RunWith(PaxExam.class)
public class AspectLifecycleAnnotationTest extends TestBase {
    public AspectLifecycleAnnotationTest() {
        super(true /* start test components bundle */);
    }

    @Test
    public void testAnnotatedAspect() {
        Ensure e = new Ensure();
        // Provide the Sequencer server to the ServiceProvider service
        ServiceRegistration sr1 = register(e, ServiceProvider.ENSURE);
        // Check if the ServiceProvider has been injected in the AspectTest service.
        e.waitForStep(1, 10000);
        // Provide the Sequencer server to the ServiceProviderAspect service
        ServiceRegistration sr2 = register(e, ServiceProviderAspect.ENSURE);
        // Check if the AspectTest has been injected with the aspect
        e.waitForStep(3, 10000);
        // Stop the ServiceProviderAspect service.
        sr2.unregister();
        // And check if the aspect has been called in its stop/destroy methods.
        e.waitForStep(7, 10000);
        sr1.unregister();
    }
}
