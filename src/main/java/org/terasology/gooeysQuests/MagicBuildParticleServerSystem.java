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

import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.gooeysQuests.api.SpawnMagicBuildParticlesComponent;
import org.terasology.structureTemplates.events.StructureSpawnStartedEvent;

/**
 * Contains the server side logic for making the {@link SpawnMagicBuildParticlesComponent} work.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MagicBuildParticleServerSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onShowMagicBuildSpawnParticles(StructureSpawnStartedEvent event, EntityRef entity,
                                               SpawnMagicBuildParticlesComponent component) {
        BlockRegionc region = event.getTransformation().transformRegion(component.region);
        worldProvider.getWorldEntity().send(new SpawnMagicBuildParticlesEvent(region));
    }

}
