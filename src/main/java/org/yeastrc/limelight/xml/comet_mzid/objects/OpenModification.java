package org.yeastrc.limelight.xml.comet_mzid.objects;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public class OpenModification {

    private BigDecimal mass;
    private Collection<Integer> positions;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenModification that = (OpenModification) o;
        return mass.equals(that.mass) &&
                Objects.equals(positions, that.positions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mass, positions);
    }

    @Override
    public String toString() {
        return "OpenModification{" +
                "mass=" + mass +
                ", positions=" + positions +
                '}';
    }

    public OpenModification(BigDecimal mass, Collection<Integer> positions) {
        this.mass = mass;
        this.positions = positions;
    }

    public BigDecimal getMass() {
        return mass;
    }

    public Collection<Integer> getPositions() {
        return positions;
    }
}
