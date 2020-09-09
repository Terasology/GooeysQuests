// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;

/**
 * Destroys this entity when a player collides with the current entity and spawns the specified prefab.
 */
public class SpawnPrefabOnPlayerCollisionComponent implements Component {

    public Prefab prefab;

}
