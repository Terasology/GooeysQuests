// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.network.BroadcastEvent;

/**
 * Sent from server to client to spawn particles in a region. The eventgets sent to the world entity since it is
 * available on both client and server while the template might not.
 */
@BroadcastEvent
public class SpawnMagicBuildParticlesEvent implements Event {
    private Region3i region;

    public SpawnMagicBuildParticlesEvent(Region3i region) {
        this.region = region;
    }

    public SpawnMagicBuildParticlesEvent() {
        // for serialization
    }

    public Region3i getRegion() {
        return region;
    }
}
