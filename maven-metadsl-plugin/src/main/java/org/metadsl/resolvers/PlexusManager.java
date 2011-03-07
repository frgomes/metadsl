package org.metadsl.resolvers;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;

public class PlexusManager {

    private final PlexusContainer container;

    public static PlexusManager getInstance() {
        return PlexusManagerHolder.helper;
    }

    private PlexusManager() {
        final PlexusContainer c;
        try {
            c= new DefaultPlexusContainer();
            //XXX container.setLoggerManager(loggerManager);
            c.initialize();
            c.start();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        this.container = c;
    }

    public PlexusContainer getContainer() {
        return container;
    }

    public void shutdown() {
        container.dispose();
    }


    //
    // private inner classes
    //

    /**
     * Initialization on demand holder idiom
     *
     * @see http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom
     */
    private static class PlexusManagerHolder {
        public static PlexusManager helper = new PlexusManager();
    }

}
