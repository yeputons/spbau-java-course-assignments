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

        Constructor<?> constructor = Class.forName(name).getConstructors()[0];
        ArrayList<Object> arguments = new ArrayList<>();
        for (String depImpl : findDependencyImplementations(constructor)) {
            arguments.add(_resolve(depImpl));
        }
        result = constructor.newInstance(arguments.toArray());
        alreadyCreated.put(name, result);

        currentlyCreating.remove(name);
        return result;
    }

    private String[] findDependencyImplementations(Constructor<?> constructor) throws AmbiguousImplementationException, ImplementationNotFoundException, ClassNotFoundException {
        Class<?>[] parameters = constructor.getParameterTypes();
        String[] depImpl = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> parameter = parameters[i];
            String dependency = null;
            for (String otherClassName : existingClasses) {
                Class otherClass = Class.forName(otherClassName);
                if (parameter.isAssignableFrom(otherClass)) {
                    if (dependency != null) {
                        throw new AmbiguousImplementationException();
                    }
                    dependency = otherClassName;
                }
            }
            if (dependency == null) {
                throw new ImplementationNotFoundException();
            }
            depImpl[i] = dependency;
        }
        return depImpl;
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
