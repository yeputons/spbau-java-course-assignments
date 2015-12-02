package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.util.*;

public class Injector {
    private final Map<String, Class> classes;
    private final Map<String, ClassDescription> descriptions;
    private final Map<String, Exception> descriptionErrors;

    public Injector(Map<String, Class> classes) {
        this.classes = classes;
        descriptions = new HashMap<>();
        descriptionErrors = new HashMap<>();
        for (Class klass : classes.values()) {
            String name = klass.getCanonicalName();
            try {
                descriptions.put(name, new ClassDescription(klass));
            } catch (AmbiguousImplementationException|ImplementationNotFoundException e) {
                descriptionErrors.put(name, e);
            }
        }
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
        ClassDescription klass = descriptions.get(name);
        if (klass == null) {
            Exception err = descriptionErrors.get(name);
            if (err == null) {
                throw new RuntimeException("Unable to find class description or description of its dependency problems");
            }
            throw err;
        }
        Object[] arguments = new Object[klass.dependencies.length];
        for (int i = 0; i < klass.dependencies.length; i++) {
            arguments[i] = _resolve(klass.dependencies[i]);
        }
        result = klass.constructor.newInstance(arguments);
        currentlyCreating.remove(name);
        return result;
    }

    private class ClassDescription {
        public final Class klass;
        public final Constructor constructor;
        public final String[] dependencies;

        private ClassDescription(Class klass) throws AmbiguousImplementationException, ImplementationNotFoundException {
            this.klass = klass;
            Constructor[] ctors = klass.getConstructors();
            if (ctors.length != 1) {
                throw new IllegalArgumentException("Exactly one public constructor should be available");
            }
            constructor = ctors[0];

            Class[] parameters = constructor.getParameterTypes();
            dependencies = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class parameter = parameters[i];
                Class dependency = null;
                for (Class otherClass : classes.values()) {
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
        Map<String, Class> classes = new HashMap<>();
        Class rootClass = Class.forName(rootClassName);
        rootClassName = rootClass.getCanonicalName();
        classes.put(rootClassName, rootClass);

        for (String name : implementationClassNames) {
            Class klass = Class.forName(name);
            if (classes.containsKey(klass.getCanonicalName())) {
                throw new IllegalArgumentException("Class " + klass.getCanonicalName() + " occurs twice as an argument");
            }
            classes.put(klass.getCanonicalName(), klass);
        }
        Injector i = new Injector(classes);
        return i.resolve(rootClassName);
    }
}
