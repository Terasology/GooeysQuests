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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gooeysQuests.api.SpawnStructureActionComponent;
import org.terasology.gooeysQuests.api.SpawnStructureEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.world.block.BlockComponent;

/**
 * Handles the activation of items with the {@link SpawnStructureActionComponent}
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnStructureActionServerSystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity,
                           SpawnStructureActionComponent structureTemplateEditorComponent) {
        EntityRef target = event.getTarget();
        BlockComponent blockComponent = target.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            entity.send(new SpawnStructureEvent(blockComponent.getPosition()));
        }
    }

}
