package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;
    private final ProfilingState profilingState;
    private final Object obj;
    
    ProfilingMethodInterceptor(Object obj, Clock clock, ProfilingState profilingState) {
        this.clock = Objects.requireNonNull(clock);
        this.profilingState = Objects.requireNonNull(profilingState);
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {

        if (method.isAnnotationPresent(Profiled.class)) {
            Instant start = clock.instant();
            try {
                return method.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException()
                                            .getMessage());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                profilingState.record(obj.getClass(), method, Duration.between(start, clock.instant()));
            }
        }


        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException()
                                        .getMessage());
        }

    }
}
