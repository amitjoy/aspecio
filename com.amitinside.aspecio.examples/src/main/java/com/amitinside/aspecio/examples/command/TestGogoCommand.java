package com.amitinside.aspecio.examples.command;

import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.amitinside.aspecio.examples.aspect.counting.CountingAspect;
import com.amitinside.aspecio.examples.async.SuperSlowService;

@Component(service = TestGogoCommand.class)
@GogoCommand(scope = "test", function = { "showCounts", "callSlowService" })
public final class TestGogoCommand {

    @Reference
    private CountingAspect countingAspect;

    @Reference
    private SuperSlowService superSlowService;

    public void showCounts() {
        countingAspect.printCounts();
    }

    public void callSlowService() {
        superSlowService.compute();
    }

}
