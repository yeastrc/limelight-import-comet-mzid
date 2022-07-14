package org.yeastrc.limelight.xml.comet_mzid.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class MzidProtein {
    private String sequence;
    private Collection<Annotation> annotations;
    private int uniqueId;

    public MzidProtein(String sequence, int uniqueId) {
        this.sequence = sequence;
        this.annotations = new HashSet<>();
        this.uniqueId = uniqueId;
    }

    public String getSequence() {
        return sequence;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MzidProtein that = (MzidProtein) o;
        return sequence.equals(that.sequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequence);
    }

    public static class Annotation {
        private String name;
        private String description;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Annotation that = (Annotation) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }


    }

}
