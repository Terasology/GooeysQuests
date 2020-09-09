// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.particles.components.generators.PositionRangeGeneratorComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gooeysQuests.api.SpawnMagicBuildParticlesComponent;
import org.terasology.math.geom.Vector3f;

/**
 * Contains the client side logic for making the {@link SpawnMagicBuildParticlesComponent}
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MagicBuildParticleClientSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;
    private Prefab spawnDungeonParticlePrefab;

    @ReceiveEvent
    public void onShowMAgicBuildSpawnParticlesEvent(SpawnMagicBuildParticlesEvent event, EntityRef worldEntity) {
        spawnMagicalBuildParticles(event.getRegion());
    }

    @Override
    public void initialise() {
        spawnDungeonParticlePrefab = assetManager.getAsset("GooeysQuests:teleportParticleEffect", Prefab.class).get();

    }

    private void spawnMagicalBuildParticles(Region3i region) {

        EntityBuilder entityBuilder = entityManager.newBuilder(spawnDungeonParticlePrefab);
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        locationComponent.setWorldPosition(region.center());
        PositionRangeGeneratorComponent particleEffect =
                entityBuilder.getComponent(PositionRangeGeneratorComponent.class);
        Vector3f size = region.size().toVector3f();
        size.scale(0.5f);
        particleEffect.minPosition.set(size.x, size.y, size.z).negate();
        particleEffect.maxPosition.set(size.x, size.y, size.z);
        entityBuilder.build();
    }
}
