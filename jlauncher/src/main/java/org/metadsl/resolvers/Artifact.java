/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.metadsl.resolvers;

import java.util.ArrayList;
import java.util.List;

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
    public static Artifact[] create(final String artifacts) {
    	final List<Artifact> result = new ArrayList<Artifact>();
    	final String[] elements = artifacts.split("[,;]");
    	for (final String element : elements) {
            final String[] parts = element.split(":");
            if (parts.length != 5) {
                throw new RuntimeException(String.format("invalid artifact name %s", element));
            }

            final Artifact a = new Artifact();
            a.setGroupId(parts[0]);
            a.setArtifactId(parts[1]);
            a.setVersion(parts[5]);
            a.setPackaging(parts[3]);
            a.setClassifier(parts[4]);

            result.add(a);
    	}
        return result.toArray(new Artifact[result.size()]);
    }


    public String getPackaging() {
        return packaging;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setPackaging(final String packaging) {
        this.packaging = packaging.trim();
    }

    public void setClassifier(final String classifier) {
        this.classifier = classifier.trim();
    }

}
