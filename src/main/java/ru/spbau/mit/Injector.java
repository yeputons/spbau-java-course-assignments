package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.util.*;

public class Injector {
    private final Set<String> existingClasses;

    public Injector(Set<String> existingClasses) {
        this.existingClasses = existingClasses;
    }

    private Map<String, Object> alreadyCreated;
    private Set<String> currentlyCreating;

    public Object resolve(String rootClassName) throws Exception {
        alreadyCreated = new HashMap<>();
        currentlyCreating = new HashSet<>();
        return _resolve(rootClassName);
    }

    private Object _resolve(String name) throws Exception {
        if (currentlyCreating.contains(name)) {
            throw new InjectionCycleException();
        }
        Object result = alreadyCreated.get(name);
        if (result != null) {
            return result;
        }
        currentlyCreating.add(name);
        ClassDescription klass = new ClassDescription(Class.forName(name));
        Object[] arguments = new Object[klass.dependencies.length];
        for (int i = 0; i < klass.dependencies.length; i++) {
            arguments[i] = _resolve(klass.dependencies[i]);
        }
        result = klass.constructor.newInstance(arguments);
        alreadyCreated.put(name, result);
        currentlyCreating.remove(name);
        return result;
    }

    private class ClassDescription {
        public final Constructor<?> constructor;
        public final String[] dependencies;

        private ClassDescription(Class<?> klass) throws AmbiguousImplementationException, ImplementationNotFoundException, ClassNotFoundException {
            Constructor<?>[] ctors = klass.getConstructors();
            if (ctors.length != 1) {
                throw new IllegalArgumentException("Exactly one public constructor should be available");
            }
            constructor = ctors[0];

            Class<?>[] parameters = constructor.getParameterTypes();
            dependencies = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class<?> parameter = parameters[i];
                Class<?> dependency = null;
                for (String otherClassName : existingClasses) {
                    Class otherClass = Class.forName(otherClassName);
                    if (parameter.isAssignableFrom(otherClass)) {
                        if (dependency != null) {
                            throw new AmbiguousImplementationException();
                        }
                        dependency = otherClass;
                    }
                }
                if (dependency == null) {
                    throw new ImplementationNotFoundException();
                }
                dependencies[i] = dependency.getCanonicalName();
            }
        }
    }

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        Set<String> existingClasses = new HashSet<>(implementationClassNames);
        existingClasses.add(rootClassName);
        return new Injector(existingClasses).resolve(rootClassName);
    }
}
