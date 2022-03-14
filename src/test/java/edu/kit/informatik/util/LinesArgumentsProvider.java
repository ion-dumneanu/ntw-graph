package edu.kit.informatik.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.List;
import java.util.stream.Stream;

public class LinesArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<LinesSource> {
    private List<String> resources;
    private boolean flatten;

    @Override
    public void accept(LinesSource linesSource) {
        resources = List.of(linesSource.value());
        flatten = linesSource.flatten();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        Stream<String> stream = resources.stream();
        return (flatten
            ? stream.flatMap(KoeriTestUtils::lines)
            : stream.map(KoeriTestUtils::lines))
            .map(Arguments::of);
    }
}
