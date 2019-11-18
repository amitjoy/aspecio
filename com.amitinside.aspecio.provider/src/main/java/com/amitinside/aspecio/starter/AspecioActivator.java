package com.amitinside.aspecio.starter;

import static com.amitinside.aspecio.api.AspecioConstants.ASPECIO_FILTER_SERVICES;
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
            context.registerService(new String[] { Aspecio.class.getName(), FindHook.class.getName(),
                    EventListenerHook.class.getName() }, aspecio, null);
        } else {
            context.registerService(Aspecio.class, aspecio, null);
        }
        final AspecioGogoCommand  gogoCommand = new AspecioGogoCommand(context, aspecio);
        final Map<String, Object> props       = new HashMap<>();

        props.put("osgi.command.scope", "aspecio");
        props.put("osgi.command.function", new String[] { "aspects", "woven" });

        context.registerService(Object.class, gogoCommand, new Hashtable<>(props));
    }

    @Override
    public void stop(final BundleContext context) {
        aspecio.deactivate();
    }

    private boolean shouldFilterServices(final BundleContext bundleContext) {
        final String filterProp = bundleContext.getProperty(ASPECIO_FILTER_SERVICES);
        return filterProp == null ? true : Boolean.valueOf(filterProp.toLowerCase());
    }

}
