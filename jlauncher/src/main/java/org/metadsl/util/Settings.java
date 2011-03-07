package org.metadsl.util;

import java.util.Map;
import java.util.TreeMap;
import org.codehaus.plexus.PlexusContainer;


public class Settings {

    private static final String PLEXUS_CONTAINER = "PLEXUS_CONTAINER";


    public PlexusContainer getPlexusContainer() {
        return (PlexusContainer) attrs.get().get(PLEXUS_CONTAINER);
    }

    public void setPlexusContainer(final PlexusContainer container) {
        attrs.get().put(PLEXUS_CONTAINER, container);
    }




    //
    // private inner classes
    //

    private static final ThreadAttributes attrs = new ThreadAttributes();

    //
    // Settings employs a ThreadLocal object in order to keep thread dependent data and
    // avoinding passing around stuff that many classes need.
    //
    private static class ThreadAttributes extends ThreadLocal<Map<String,Object>> {
        @Override
        public Map<String,Object> initialValue() {
            return new TreeMap<String, Object>();
        }
    }

}
