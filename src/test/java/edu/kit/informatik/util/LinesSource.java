package edu.kit.informatik.util;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(LinesArgumentsProvider.class)
public @interface LinesSource {
    String[] value();

    boolean flatten() default true;
}
