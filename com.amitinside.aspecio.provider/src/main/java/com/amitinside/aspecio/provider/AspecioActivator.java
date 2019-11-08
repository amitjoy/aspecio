package com.amitinside.aspecio.provider;

import static org.osgi.framework.Constants.BUNDLE_ACTIVATOR;
import java.util.Hashtable;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;
import com.amitinside.aspecio.api.Aspecio;
import com.amitinside.aspecio.api.AspecioConstants;
import com.amitinside.aspecio.logging.provider.AspecioLogger;
import com.amitinside.aspecio.service.command.provider.AspecioGogoCommand;
import com.amitinside.aspecio.service.provider.AspecioProvider;

@Header(name = BUNDLE_ACTIVATOR, value = "${@class}")
public final class AspecioActivator implements BundleActivator {

  private AspecioProvider aspecio;

  @Override
  public void start(final BundleContext context) {
    AspecioLogger.init(context);
    aspecio = new AspecioProvider(context);
    aspecio.activate();

    final boolean filterServices = shouldFilterServices(context);

    if (filterServices) {
      context.registerService(new String[] {Aspecio.class.getName(), FindHook.class.getName(),
          EventListenerHook.class.getName()}, aspecio, null);
    } else {
      context.registerService(Aspecio.class, aspecio, null);
    }
    final Hashtable<String, Object> props = new Hashtable<>();
    props.put("osgi.command.scope", AspecioGogoCommand.ASPECIO_GOGO_COMMAND_SCOPE);
    props.put("osgi.command.function", AspecioGogoCommand.ASPECIO_GOGO_COMMANDS);

    final AspecioGogoCommand gogoCommand = new AspecioGogoCommand(context, aspecio);
    context.registerService(Object.class, gogoCommand, props);
  }

  @Override
  public void stop(final BundleContext context) {
    if (aspecio != null) {
      aspecio.deactivate();
    }
  }

  private boolean shouldFilterServices(final BundleContext bundleContext) {
    final String filterProp = bundleContext.getProperty(AspecioConstants.ASPECIO_FILTER_SERVICES);
    if (filterProp == null) {
      return true; // default to true
    } else {
      return Boolean.valueOf(filterProp.toLowerCase());
    }
  }

}
