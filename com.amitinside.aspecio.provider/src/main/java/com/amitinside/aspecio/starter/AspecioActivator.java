/*******************************************************************************
 * Copyright 2021 Amit Kumar Mondal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.amitinside.aspecio.starter;

import static com.amitinside.aspecio.api.AspecioConstants.ASPECIO_FILTER_SERVICES;
import static java.util.Optional.ofNullable;
import static org.apache.felix.service.command.CommandProcessor.COMMAND_FUNCTION;
import static org.apache.felix.service.command.CommandProcessor.COMMAND_SCOPE;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATOR;
import static org.osgi.framework.Constants.BUNDLE_DESCRIPTION;
import static org.osgi.framework.Constants.BUNDLE_NAME;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

import com.amitinside.aspecio.api.Aspecio;
import com.amitinside.aspecio.command.AspecioGogoCommand;
import com.amitinside.aspecio.service.AspecioProvider;

@Header(name = BUNDLE_NAME, value = "Aspecio")
@Header(name = BUNDLE_ACTIVATOR, value = "${@class}")
@Header(name = BUNDLE_DESCRIPTION, value = "AOP Proxies for OSGi Services")
public final class AspecioActivator implements BundleActivator {

    private AspecioProvider aspecio;

    @Override
    public void start(final BundleContext context) {
        aspecio = new AspecioProvider(context);
        aspecio.activate();

        final boolean filterServices = shouldFilterServices(context);
        if (filterServices) {
            // @formatter:off
            context.registerService(
                    new String[] {
                            Aspecio.class.getName(),
                            FindHook.class.getName(),
                            EventListenerHook.class.getName()
                    },
                    aspecio, null);
            // @formatter:on
        } else {
            context.registerService(Aspecio.class, aspecio, null);
        }
        final AspecioGogoCommand gogoCommand = new AspecioGogoCommand(context, aspecio);
        final Map<String, Object> props = new HashMap<>();

        props.put(COMMAND_SCOPE, "aspecio");
        props.put(COMMAND_FUNCTION, new String[] { "aspects", "woven" });

        context.registerService(Object.class, gogoCommand, new Hashtable<>(props));
    }

    @Override
    public void stop(final BundleContext context) {
        aspecio.deactivate();
    }

    private boolean shouldFilterServices(final BundleContext bundleContext) {
        return ofNullable(bundleContext.getProperty(ASPECIO_FILTER_SERVICES)).map(Boolean::valueOf).orElse(true);
    }

}
