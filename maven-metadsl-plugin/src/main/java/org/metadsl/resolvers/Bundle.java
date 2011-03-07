package org.metadsl.resolvers;

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
    public static Bundle create(String bundle) {
        final String[] parts = bundle.split(":");
        if (parts.length != 3) {
            throw new RuntimeException("bundle name must be as groupId:artifactId:version");
        }

        final Bundle b = new Bundle();
        b.setGroupId(parts[0]);
        b.setArtifactId(parts[1]);
        b.setVersion(parts[2]);
        return b;
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

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId.trim();
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId.trim();
    }

    public void setVersion(String version) {
        this.version = version.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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
    public boolean equals(Object obj) {
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
