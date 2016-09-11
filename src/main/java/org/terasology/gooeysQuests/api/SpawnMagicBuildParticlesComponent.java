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
package org.terasology.gooeysQuests.api;

import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;

/**
 * If a entity with this component gets a {@link SpawnStructureEvent} (that triggers a
 * {@link StructureBlocksSpawnedEvent}) then particles will be spawned in the given region.
 */
public class SpawnMagicBuildParticlesComponent implements Component {
    public Region3i region;
}
