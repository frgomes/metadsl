package org.metadsl.resolvers;

import java.util.ArrayList;
import java.util.List;

public class Bundle {

    private String groupId;
    private String artifactId;
    private String version;

    public Bundle() {
        // nothing
    }

    /**
     * groupId:artifactId:version.
     *
     * @param bundle
     * @return
     */
    public static Bundle[] create(final String bundles) {
    	final List<Bundle> result = new ArrayList<Bundle>();
    	final String[] elements = bundles.split("[,;]");
    	for (final String element : elements) {
            final String[] parts = element.split(":");
            if (parts.length != 3) {
                throw new RuntimeException(String.format("invalid bundle name %s", element));
            }

            final Bundle b = new Bundle();
            b.setGroupId(parts[0]);
            b.setArtifactId(parts[1]);
            b.setVersion(parts[2]);

            result.add(b);
    	}
        return result.toArray(new Bundle[result.size()]);
    }


    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId.trim();
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId.trim();
    }

    public void setVersion(final String version) {
        this.version = version.trim();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(groupId == null ? "" : groupId).append(':');
        sb.append(artifactId == null ? "" : artifactId).append(':');
        sb.append(version == null ? "" : version);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return toString().equals(obj.toString());
    }
}
