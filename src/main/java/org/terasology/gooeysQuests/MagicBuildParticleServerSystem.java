// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.gestalt.assets.management.AssetManager;
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
        Region3i region = event.getTransformation().transformRegion(component.region);
        worldProvider.getWorldEntity().send(new SpawnMagicBuildParticlesEvent(region));
    }

}
