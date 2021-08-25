// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.api;

import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.engine.math.Side;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes how the current structure entity can be connected to other structure entities.
 */
public class StructureConnectionPointsComponent implements Component<StructureConnectionPointsComponent> {
    public List<ConnectionPoint> points = new ArrayList<>();

    @Override
    public void copyFrom(StructureConnectionPointsComponent other) {
        this.points = Lists.newArrayList(other.points);
    }

    @MappedContainer
    public static class ConnectionPoint {
        public Vector3i position;
        /**
         * Type of the connection point. Two connection poitns can only be connected if they have the same type.
         *
         */
        public String type;
        /**
         * The direction the connection is facing. The point this structure can connect to must have the opposite
         * direction value. When a connection point is at the front of a structure then this value must be also set to
         * front.
         */
        public Side direction;
        /**
         * When this structure hasn't been spawwned yet, then this point will be used to attach it to an existing
         * structure.
         */
        public boolean incoming;
        /**
         * When owning structure has already been spawned, then this is a connection point that can be used to connect
         * it to other buildings.
         */
        public boolean outgoing;

    }
}

