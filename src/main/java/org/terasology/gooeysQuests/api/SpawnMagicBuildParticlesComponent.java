// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.api;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;

/**
 * If a entity with this component gets a {@link SpawnStructureEvent} (that triggers a {@link
 * StructureBlocksSpawnedEvent}) then particles will be spawned in the given region.
 */
public class SpawnMagicBuildParticlesComponent implements Component {
    public Region3i region;
}
