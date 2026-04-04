package com.nodecraft.nodesystem.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Composite geometry that groups multiple geometry objects into one value.
 */
public class CompositeGeometryData implements GeometryData {

    private final List<GeometryData> geometries;

    public CompositeGeometryData(List<GeometryData> geometries) {
        List<GeometryData> flattened = new ArrayList<>();
        if (geometries != null) {
            for (GeometryData geometry : geometries) {
                appendGeometry(flattened, geometry);
            }
        }
        this.geometries = Collections.unmodifiableList(flattened);
    }

    private static void appendGeometry(List<GeometryData> target, GeometryData geometry) {
        if (geometry == null) {
            return;
        }
        if (geometry instanceof CompositeGeometryData composite) {
            for (GeometryData child : composite.getGeometries()) {
                appendGeometry(target, child);
            }
            return;
        }
        target.add(geometry);
    }

    public List<GeometryData> getGeometries() {
        return geometries;
    }

    public int size() {
        return geometries.size();
    }

    public boolean isEmpty() {
        return geometries.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositeGeometryData that)) return false;
        return Objects.equals(geometries, that.geometries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geometries);
    }

    @Override
    public String toString() {
        return "CompositeGeometryData{size=" + geometries.size() + "}";
    }
}
