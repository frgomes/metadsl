/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.metadsl.resolvers;

/**
 *
 * @author rgomes
 */
public class Artifact extends Bundle {
    private String packaging;
    private String classifier;

    public Artifact() {
        // nothing
    }

    /**
     * groupId:artifactId:packaging:classifier:version
     * 
     * @param artifact
     * @return
     */
    public static Artifact create(String artifact) {
        final String[] parts = artifact.split(":");
        if (parts.length != 5) {
            throw new RuntimeException("artifact name must be as groupId:artifactId:packaging:classifier:version");
        }

        final Artifact a = new Artifact();
        a.setGroupId(parts[0]);
        a.setArtifactId(parts[1]);
        a.setVersion(parts[5]);
        a.setPackaging(parts[3]);
        a.setClassifier(parts[4]);
        return a;
    }


    public String getPackaging() {
        return packaging;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging.trim();
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier.trim();
    }

}
