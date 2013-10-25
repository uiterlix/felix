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
import org.apache.felix.dm.test.components.ExtraAdapterServiceProperties;
import org.apache.felix.dm.test.components.ExtraAspectServiceProperties;
import org.apache.felix.dm.test.components.ExtraFactoryServiceProperties;
import org.apache.felix.dm.test.components.ExtraServiceProperties;
import org.apache.felix.dm.test.integration.common.TestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.ServiceRegistration;

/**
 * Use case: Verify the a Service may provide its service properties dynamically from its start method.
 */
@RunWith(PaxExam.class)
public class ExtraServicePropertiesTest extends TestBase {
    public ExtraServicePropertiesTest() {
        super(true /* start test components bundle */);
    }

    /**
     * Tests if a Service can provide its service properties from its start method.
     */
    @Test
    public void testExtraServiceProperties() {
        Ensure e = new Ensure();
        ServiceRegistration sr = register(e, ExtraServiceProperties.ENSURE);
        e.waitForStep(2, 10000);
        sr.unregister();
    }

    /**
     * Tests if a Service instantiated by a Factory can provide its service properties from its start method.
     */
    @Test
    public void testExtraFactoryServiceProperties() {
        Ensure e = new Ensure();
        ServiceRegistration sr = register(e, ExtraFactoryServiceProperties.ENSURE);
        e.waitForStep(3, 10000);
        sr.unregister();
    }

    /**
     * Tests if an AdapterService can provide its service properties from its start method.
     */
    @Test
    public void testExtraAdapterServiceProperties() {
        Ensure e = new Ensure();
        ServiceRegistration sr = register(e, ExtraAdapterServiceProperties.ENSURE);
        e.waitForStep(3, 10000);
        sr.unregister();
    }

    /**
     * Tests if an AspectService can provide its service properties from its start method.
     */
    @Test
    public void testExtraAspectServiceProperties() {
        Ensure e = new Ensure();
        ServiceRegistration sr = register(e, ExtraAspectServiceProperties.ENSURE);
        e.waitForStep(3, 10000);
        sr.unregister();
    }
}
