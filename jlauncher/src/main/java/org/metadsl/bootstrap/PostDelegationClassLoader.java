package org.metadsl.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class PostDelegationClassLoader extends URLClassLoader {

    private boolean debug = false;

    public PostDelegationClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public PostDelegationClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public PostDelegationClassLoader(URL[] urls) {
        super(urls);
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // First check whether it's already been loaded
        Class<?> loadedClass = findLoadedClass(name);

        // Nope, try to load it
        if (loadedClass == null) {
            try {
                // Ignore parent delegation and just try to load locally
                loadedClass = findClass(name);
                if (debug) System.out.println(String.format("%s: loaded %s", PostDelegationClassLoader.class.getSimpleName(), name));
            } catch (ClassNotFoundException e) {
                // Swallow exception : resource does not exist locally
            }

            // If not found, just use the standard URLClassLoader (which follows normal parent delegation)
            if (loadedClass == null) {
                // throws ClassNotFoundException if not found in delegation hierarchy at all
                loadedClass = super.loadClass(name);
                if (debug) System.out.println(String.format("%s: loaded %s", this.getParent().getClass().getSimpleName(), name));
            }
        }
        return loadedClass;
    }
}
