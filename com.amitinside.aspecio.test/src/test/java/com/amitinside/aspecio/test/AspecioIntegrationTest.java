package com.amitinside.aspecio.test;

import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT;
import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WEAVE;
import static com.amitinside.aspecio.api.AspecioConstants.SERVICE_ASPECT_WOVEN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.osgi.framework.Constants.OBJECTCLASS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.promise.Promise;
import org.osgi.util.tracker.ServiceTracker;

import com.amitinside.aspecio.api.Aspecio;
import com.amitinside.aspecio.api.AspectDTO;
import com.amitinside.aspecio.examples.DemoConsumer;
import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.amitinside.aspecio.examples.aspect.metric.MetricAspect;
import com.amitinside.aspecio.examples.async.SuperSlowService;
import com.amitinside.aspecio.examples.greetings.Goodbye;
import com.amitinside.aspecio.examples.greetings.Hello;
import com.amitinside.aspecio.test.api.Randomizer;
import com.amitinside.aspecio.test.aspect.NoopAspect;
import com.amitinside.aspecio.test.component.RandomizerImpl;

import aQute.launchpad.Launchpad;
import aQute.launchpad.LaunchpadBuilder;
import aQute.launchpad.Service;
import aQute.launchpad.junit.LaunchpadRunner;

@RunWith(LaunchpadRunner.class)
public final class AspecioIntegrationTest {

    @Service
    private Aspecio aspecio;

    @Service
    private Launchpad launchpad;

    @Service
    private DemoConsumer demoConsumer;

    @Service
    private CountingAspect countingAspect;

    @Service
    private ServiceReference<DemoConsumer> demoConsumerRef;

    @Service(target = "(" + SERVICE_ASPECT_WOVEN + "=*)")
    private Hello hello;

    @Service(target = "(" + SERVICE_ASPECT_WOVEN + "=*)")
    private ServiceReference<Hello> helloRef;

    @Service(target = "(" + SERVICE_ASPECT_WOVEN + "=*)")
    private Goodbye goodbye;

    @Service(target = "(" + SERVICE_ASPECT_WOVEN + "=*)")
    private ServiceReference<Goodbye> goodbyeRef;

    @Service(target = "(" + SERVICE_ASPECT_WOVEN + "=*)")
    private SuperSlowService superSlowService;

    static LaunchpadBuilder builder = new LaunchpadBuilder().bndrun("test.bndrun").debug();

    @Test
    public void testExampleApplication() throws Exception {
        launchpad.reportServices();
        checkExampleApplicationIsProperlyWoven();
    }

    private void checkExampleApplicationIsProperlyWoven() throws Exception {
        // Check if all aspects are accounted for
        final Set<String> aspects = aspecio.getRegisteredAspects();
        assertThat(aspects).contains(MetricAspect.All.class.getName(), MetricAspect.AnnotatedOnly.class.getName(),
                CountingAspect.class.getName());

        final Optional<AspectDTO> aspectDTO = aspecio.getAspectDescription(MetricAspect.All.class.getName());
        assertThat(aspectDTO).isPresent();
        assertThat(aspectDTO.get().aspectName).isEqualTo(MetricAspect.All.class.getName());

        // In our system, we have exactly one service, that is woven by Aspecio,
        // that provides both Hello and Goodbye.
        assertThat(hello).isNotNull();
        assertThat(goodbye).isNotNull();

        // The following service references should be the same
        assertThat(helloRef).isEqualTo(goodbyeRef);

        // Hidden property added to woven services
        final Object wovenProperty = helloRef.getProperty(SERVICE_ASPECT_WOVEN);
        assertThat(wovenProperty).isNotNull().isInstanceOf(String[].class);
        assertThat((String[]) wovenProperty).containsExactly(CountingAspect.class.getName(),
                MetricAspect.All.class.getName());

        assertThat(hello).isSameAs(goodbye);
        assertThat(hello.getClass().getName())
                .isEqualTo("com.amitinside.aspecio.examples.greetings.internal.HelloGoodbyeImpl$Proxy$");

        hello.hello();

        // Check that there is one shared classloader for woven aspects of objects of a same given bundle revision
        assertThat(superSlowService.getClass().getClassLoader()).isSameAs(hello.getClass().getClassLoader());

        final Promise<Long> longResult = demoConsumer.getLongResult();

        assertThat(extractFromPrintStream(demoConsumer::consumeTo)).isEqualTo("hello goodbye\n");

        countingAspect.printCounts();

        assertThat(longResult.getValue()).isEqualTo(42L);
    }

    @Test
    public void testAspectDynamics() throws InvalidSyntaxException {

        final String ldapFilter = "(&(" + OBJECTCLASS + "=" + Randomizer.class.getName() + ")(" + SERVICE_ASPECT_WOVEN
                + "=*))";
        final Filter filter     = launchpad.getBundleContext().createFilter(ldapFilter);

        final ServiceTracker<Randomizer, Randomizer> randomizerTracker = new ServiceTracker<>(
                launchpad.getBundleContext(), filter, null);
        randomizerTracker.open();

        final String                          fakeAspect     = "tested.aspect";
        final Randomizer                      randomizerImpl = new RandomizerImpl();
        final ServiceRegistration<Randomizer> serviceReg     = launchpad.register(Randomizer.class, randomizerImpl,
                SERVICE_ASPECT_WEAVE, fakeAspect);

        // Check that the service is not available, because our fakeAspect is not provided.
        assertThat(randomizerTracker.size()).isEqualTo(0);

        final NoopAspect                  noopAspect = new NoopAspect();
        final ServiceRegistration<Object> aspectReg  = launchpad.register(Object.class, noopAspect, SERVICE_ASPECT,
                fakeAspect);

        // Check that the service is available, because our fakeAspect is provided.
        assertThat(randomizerTracker.size()).isEqualTo(1);
        assertThat((String[]) randomizerTracker.getServiceReference().getProperty(SERVICE_ASPECT_WOVEN))
                .containsExactly(fakeAspect);

        aspectReg.unregister();
        // Check that the service is available, because our fakeAspect is gone.
        assertThat(randomizerTracker.size()).isEqualTo(0);

        // Register the aspect again
        launchpad.register(Object.class, noopAspect, SERVICE_ASPECT, fakeAspect);
        assertThat(randomizerTracker.size()).isEqualTo(1);

        // Let the service go
        serviceReg.unregister();
        assertThat(randomizerTracker.size()).isEqualTo(0);

        // Register the service again, it should be immediately available
        launchpad.register(Randomizer.class, randomizerImpl, SERVICE_ASPECT_WEAVE, fakeAspect);
        assertThat(randomizerTracker.size()).isEqualTo(1);

        randomizerTracker.close();
    }

    private String extractFromPrintStream(final Consumer<PrintStream> psConsumer) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
            psConsumer.accept(ps);
            return baos.toString(UTF_8.name());
        }
    }

}
