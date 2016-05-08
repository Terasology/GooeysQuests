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
import org.terasology.gooeysQuests.api.BlockRegionMovement;
import org.terasology.gooeysQuests.api.BlockRegionTransform;
import org.terasology.gooeysQuests.api.BlockRegionTransformationList;
import org.terasology.gooeysQuests.api.HorizontalBlockRegionRotation;
import org.terasology.gooeysQuests.api.SpawnStructureActionComponent;
import org.terasology.gooeysQuests.api.SpawnStructureEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
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
        if (blockComponent == null) {
            return;
        }

        LocationComponent characterLocation = event.getInstigator().getComponent(LocationComponent.class);
        Vector3f directionVector =  characterLocation.getWorldDirection();

        Side facedDirection = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());



        BlockRegionTransform blockRegionTransform = createBlockRegionTransformForCharacterTargeting(facedDirection,
                blockComponent.getPosition());

        entity.send(new SpawnStructureEvent(blockRegionTransform));

    }

    public static BlockRegionTransform createBlockRegionTransformForCharacterTargeting(
            Side facedDirection, Vector3i target) {
        Side sideOfStructure = Side.FRONT;
        BlockRegionTransformationList transformList = new BlockRegionTransformationList();
        transformList.addTransformation(
                HorizontalBlockRegionRotation.createRotationFromSideToSide(sideOfStructure, facedDirection));
        transformList.addTransformation(new BlockRegionMovement(target));
        return transformList;
    }


}
