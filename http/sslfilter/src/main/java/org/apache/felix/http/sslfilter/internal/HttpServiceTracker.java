/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.http.sslfilter.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class HttpServiceTracker extends ServiceTracker
{
    private final Map<ServiceReference, SslFilter> filters;

    public HttpServiceTracker(BundleContext context)
    {
        super(context, ExtHttpService.class.getName(), null);

        this.filters = new HashMap<ServiceReference, SslFilter>();
    }

    public Object addingService(ServiceReference reference)
    {
        ExtHttpService service = (ExtHttpService) super.addingService(reference);
        if (service != null)
        {
            SslFilter filter = new SslFilter();
            try
            {
                service.registerFilter(filter, ".*", new Hashtable(), 0, null);

                this.filters.put(reference, filter);

                SystemLogger.log(LogService.LOG_DEBUG, "SSL filter registered...");
            }
            catch (ServletException e)
            {
                SystemLogger.log(LogService.LOG_WARNING, "Failed to register SSL filter!", e);
            }
        }

        return service;
    }

    public void removedService(ServiceReference reference, Object service)
    {
        SslFilter filter = (SslFilter) this.filters.remove(reference);
        if (filter != null)
        {
            ((ExtHttpService) service).unregisterFilter(filter);

            SystemLogger.log(LogService.LOG_DEBUG, "SSL filter unregistered...");
        }

        super.removedService(reference, service);
    }
}
