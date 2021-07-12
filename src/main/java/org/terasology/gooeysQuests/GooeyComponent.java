// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Used to find the character "Gooey".
 */
public class GooeyComponent implements Component<GooeyComponent> {
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public EntityRef offeredQuest = EntityRef.NULL;

    @Override
    public void copy(GooeyComponent other) {
        this.offeredQuest = other.offeredQuest;
    }
}
