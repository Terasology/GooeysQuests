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

import org.joml.Vector3f;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gooeysQuests.api.SpawnMagicBuildParticlesComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.particles.components.generators.PositionRangeGeneratorComponent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockRegionc;

/**
 * Contains the client side logic for making the {@link SpawnMagicBuildParticlesComponent}
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MagicBuildParticleClientSystem extends BaseComponentSystem {

    private Prefab spawnDungeonParticlePrefab;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;


    @ReceiveEvent
    public void onShowMAgicBuildSpawnParticlesEvent(SpawnMagicBuildParticlesEvent event, EntityRef worldEntity) {
        spawnMagicalBuildParticles(event.getRegion());
    }

    @Override
    public void initialise() {
        spawnDungeonParticlePrefab = assetManager.getAsset("GooeysQuests:teleportParticleEffect", Prefab.class).get();

    }

    private void spawnMagicalBuildParticles(BlockRegionc region) {
        EntityBuilder entityBuilder = entityManager.newBuilder(spawnDungeonParticlePrefab);
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        locationComponent.setWorldPosition(region.center(new org.joml.Vector3f()));
        PositionRangeGeneratorComponent particleEffect = entityBuilder.getComponent(PositionRangeGeneratorComponent.class);

        Vector3f size = new Vector3f(region.getSizeX(), region.getSizeY(), region.getSizeZ()).mul(0.5f);
        particleEffect.minPosition.set(size).negate();
        particleEffect.maxPosition.set(size);
        entityBuilder.build();
    }
}
