// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.events.CollideEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gooeysQuests.api.SpawnMagicBuildParticlesComponent;

/**
 * Contains the server side logic for making the {@link SpawnMagicBuildParticlesComponent} work.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnPrefabOnCollisionServerSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onCollision(CollideEvent event, EntityRef entity,
                            SpawnPrefabOnPlayerCollisionComponent spawnPrefabComponent,
                            LocationComponent placeholderLocationComponent) {

        EntityBuilder entityBuilder = entityManager.newBuilder(spawnPrefabComponent.prefab);
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        locationComponent.setWorldPosition(placeholderLocationComponent.getWorldPosition());
        entityBuilder.build();
        entity.destroy();
    }


}
