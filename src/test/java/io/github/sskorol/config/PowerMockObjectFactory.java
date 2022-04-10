package io.github.sskorol.config;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.testng.internal.PowerMockClassloaderObjectFactory;
import org.testng.ITestObjectFactory;
import org.testng.internal.objects.ObjectFactoryImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PowerMockObjectFactory implements ITestObjectFactory {

    private final PowerMockClassloaderObjectFactory powerMockObjectFactory = new PowerMockClassloaderObjectFactory();
    private final ObjectFactoryImpl defaultObjectFactory = new ObjectFactoryImpl();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Constructor<T> constructor, Object... params) {
        final T testInstance;
        Class<?> testClass = constructor.getDeclaringClass();
        if (hasPowerMockAnnotation(testClass)) {
            testInstance = (T) powerMockObjectFactory.newInstance(constructor, params);
        } else {
            testInstance = defaultObjectFactory.newInstance(constructor, params);
        }

        return testInstance;
    }

    private boolean hasPowerMockAnnotation(Class<?> testClass) {
        return isClassAnnotatedWithPowerMockAnnotation(testClass) || anyMethodInClassHasPowerMockAnnotation(testClass);
    }

    private boolean anyMethodInClassHasPowerMockAnnotation(Class<?> testClass) {
        final Method[] methods = testClass.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(PrepareForTest.class) || method.isAnnotationPresent(SuppressStaticInitializationFor.class)) {
                return true;
            }
        }
        return false;
    }

    private boolean isClassAnnotatedWithPowerMockAnnotation(Class<?> testClass) {
        return testClass.isAnnotationPresent(PrepareForTest.class) || testClass.isAnnotationPresent(SuppressStaticInitializationFor.class);
    }
}
