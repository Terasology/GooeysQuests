// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;

/**
 * Used to find the character "Gooey".
 */
public class GooeyComponent implements Component {
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public EntityRef offeredQuest = EntityRef.NULL;
}
