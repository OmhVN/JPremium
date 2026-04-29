package com.community.jpremium.common.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import sun.misc.Unsafe;

public final class UrlClassLoaderClasspathInjector implements ClasspathInjector {
    private final URLClassLoader classLoader;
    private final UrlClassLoaderAccess access;

    public UrlClassLoaderClasspathInjector(URLClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.access = UrlClassLoaderAccess.create(classLoader);
    }

    @Override
    public void addToClasspath(Path jarPath) {
        try {
            this.access.addUrl(jarPath.toUri().toURL());
        } catch (Exception e) {
            throw new IllegalStateException("Could not add jar to classpath: " + jarPath, e);
        }
    }

    private interface UrlClassLoaderAccess {
        void addUrl(URL url);

        static UrlClassLoaderAccess create(URLClassLoader classLoader) {
            if (ReflectiveAddUrlAccess.isSupported()) {
                return new ReflectiveAddUrlAccess(classLoader);
            }
            if (UnsafeUcpAccess.isSupported()) {
                return new UnsafeUcpAccess(classLoader);
            }
            throw new UnsupportedOperationException(
                    "JPremium is unable to inject into the plugin URLClassLoader.\n"
                            + "You may be able to fix this problem by adding the following command-line argument directly after the 'java' command in your start script:\n"
                            + "'--add-opens java.base/java.lang=ALL-UNNAMED'"
            );
        }
    }

    private static final class ReflectiveAddUrlAccess implements UrlClassLoaderAccess {
        private static final Method ADD_URL_METHOD = findAddUrlMethod();
        private final URLClassLoader classLoader;

        private ReflectiveAddUrlAccess(URLClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        static boolean isSupported() {
            return ADD_URL_METHOD != null;
        }

        @Override
        public void addUrl(URL url) {
            try {
                ADD_URL_METHOD.invoke(this.classLoader, url);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not inject into URLClassLoader via addURL(URL)", e);
            }
        }

        private static Method findAddUrlMethod() {
            try {
                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                return method;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static final class UnsafeUcpAccess implements UrlClassLoaderAccess {
        private static final Unsafe UNSAFE = findUnsafe();
        private final Collection<URL> unopenedUrls;
        private final Collection<URL> path;

        private UnsafeUcpAccess(URLClassLoader classLoader) {
            try {
                Object urlClassPath = getFieldValue(URLClassLoader.class, classLoader, "ucp");
                this.unopenedUrls = (Collection<URL>) getFieldValue(urlClassPath.getClass(), urlClassPath, "unopenedUrls");
                this.path = (Collection<URL>) getFieldValue(urlClassPath.getClass(), urlClassPath, "path");
            } catch (Throwable t) {
                throw new IllegalStateException("Could not access URLClassLoader internals for classpath injection", t);
            }
        }

        static boolean isSupported() {
            return UNSAFE != null;
        }

        @Override
        public void addUrl(URL url) {
            if (this.unopenedUrls == null || this.path == null) {
                throw new IllegalStateException("unopenedUrls or path collection is null");
            }
            synchronized (this.unopenedUrls) {
                this.unopenedUrls.add(url);
                this.path.add(url);
            }
        }

        private static Unsafe findUnsafe() {
            try {
                Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                return (Unsafe) field.get(null);
            } catch (Throwable t) {
                return null;
            }
        }

        private static Object getFieldValue(Class<?> ownerClass, Object instance, String fieldName) throws NoSuchFieldException {
            Field field = ownerClass.getDeclaredField(fieldName);
            long offset = UNSAFE.objectFieldOffset(field);
            return UNSAFE.getObject(instance, offset);
        }
    }
}

