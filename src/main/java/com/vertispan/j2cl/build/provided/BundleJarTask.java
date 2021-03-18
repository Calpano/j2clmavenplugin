package com.vertispan.j2cl.build.provided;

import com.vertispan.j2cl.build.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BundleJarTask extends TaskFactory {
    @Override
    public String getOutputType() {
        return OutputTypes.BUNDLED_JS_APP;
    }

    @Override
    public String getTaskName() {
        return "default";
    }

    @Override
    public Task resolve(Project project, PropertyTrackingConfig config) {
        List<Input> jsSources = Stream.concat(
                Stream.of(input(project, OutputTypes.BUNDLED_JS)),
                scope(project.getDependencies(), Dependency.Scope.RUNTIME)
                        .stream()
                        .map(inputs(OutputTypes.BUNDLED_JS))
        ).collect(Collectors.toList());

        return outputPath -> {

        };
    }
}