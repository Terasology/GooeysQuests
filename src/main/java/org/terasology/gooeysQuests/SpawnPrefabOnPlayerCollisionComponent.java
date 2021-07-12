// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Destroys this entity when a player collides with the current entity and spawns the specified prefab.
 */
public class SpawnPrefabOnPlayerCollisionComponent implements Component<SpawnPrefabOnPlayerCollisionComponent> {

    public Prefab prefab;

    @Override
    public void copy(SpawnPrefabOnPlayerCollisionComponent other) {
        this.prefab = other.prefab;
    }
}
