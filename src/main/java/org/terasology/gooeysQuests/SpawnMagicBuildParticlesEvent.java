/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gooeysQuests;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.Region3i;
import org.terasology.network.BroadcastEvent;

/**
 * Sent from server to client to spawn particles in a region. The eventgets sent to the world entity since
 * it is available on both client and server while the template might not.
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
