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

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.gooeysQuests.api.PendingStructureSpawnComponent;
import org.terasology.gooeysQuests.api.StructureConnectionPointsComponent;
import org.terasology.gooeysQuests.api.StructureConnectionPointsComponent.ConnectionPoint;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.util.transform.BlockRegionMovement;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.structureTemplates.util.transform.BlockRegionTransformationList;
import org.terasology.structureTemplates.util.transform.HorizontalBlockRegionRotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ConnectedStructureSpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    private List<EntityRef> pendingSpawnEntities = new ArrayList<>();

    private Random random = new Random();

    @ReceiveEvent
    public void onSpawnBlockRegions(SpawnStructureEvent event, EntityRef entity,
                                    StructureConnectionPointsComponent connectionPointsComponent) {

        BlockRegionTransform transformation = event.getTransformation();
        for (ConnectionPoint connectionPoint: connectionPointsComponent.points) {
            if (connectionPoint.outgoing) {
                Side direction =  transformation.transformSide(connectionPoint.direction).reverse();
                Vector3i position = transformation.transformVector3i(connectionPoint.position);
                EntityBuilder entityBuilder = entityManager.newBuilder();
                LocationComponent locationComponent = new LocationComponent();
                locationComponent.setWorldPosition(position.toVector3f());
                entityBuilder.addComponent(locationComponent);

                PendingStructureSpawnComponent pendingStructureSpawnComponent = new PendingStructureSpawnComponent();
                pendingStructureSpawnComponent.direction = direction;
                pendingStructureSpawnComponent.type = connectionPoint.type;
                entityBuilder.addComponent(pendingStructureSpawnComponent);
                entityBuilder.build();
            }
        }
    }

    @ReceiveEvent
    public void onAddedStructureConnectionPointsComponent(OnAddedComponent event, EntityRef entity,
                                                StructureConnectionPointsComponent component) {
        // TODO cache
    }

    @ReceiveEvent
    public void onChangedStructureConnectionPointsComponent(OnChangedComponent event, EntityRef entity,
                                                  StructureConnectionPointsComponent component) {
        // TODO cache
    }

    @ReceiveEvent
    public void onBeforeRemoveStructureConnectionPointsComponent(BeforeRemoveComponent event, EntityRef entity,
                                                                 PendingStructureSpawnComponent component) {
        // TODO cache
    }

    @ReceiveEvent
    public void onAddedPendingStructureSpawnComponent(OnAddedComponent event, EntityRef entity,
                                                      PendingStructureSpawnComponent component,
                                                      LocationComponent locationComponent) {
        pendingSpawnEntities.add(entity);
    }

    @ReceiveEvent
    public void onBeforeRemovePendingStructureSpawnComponent(BeforeRemoveComponent event, EntityRef entity,
                                                             PendingStructureSpawnComponent component,
                                                             LocationComponent locationComponent) {
        pendingSpawnEntities.remove(entity);
    }

    private class SpawnPossiblity {
        private EntityRef structureToSpawn;
        private Side requiredDirection;
        private Vector3i relativeConnectionPointPostion;

        public SpawnPossiblity(EntityRef structureToSpawn, Side requiredDirection, Vector3i relativeConnectionPointPostion) {
            this.structureToSpawn = structureToSpawn;
            this.requiredDirection = requiredDirection;
            this.relativeConnectionPointPostion = relativeConnectionPointPostion;
        }

        public EntityRef getStructureToSpawn() {
            return structureToSpawn;
        }

        public Side getRequiredDirection() {
            return requiredDirection;
        }

        public Vector3i getRelativeConnectionPointPostion() {
            return relativeConnectionPointPostion;
        }
    }


    @Override
    public void update(float delta) {
        if (pendingSpawnEntities.size() == 0) {
            return;
        }

        EntityRef randomEntity = pendingSpawnEntities.get(pendingSpawnEntities.size()-1);
        PendingStructureSpawnComponent pendingStructureSpawnComponent = randomEntity.getComponent(
                PendingStructureSpawnComponent.class);
        LocationComponent locationComponent = randomEntity.getComponent(LocationComponent.class);
        if (pendingStructureSpawnComponent == null || locationComponent == null) {
            return; // should not happen though how map gets filled, but just to be sure
        }
        String type = pendingStructureSpawnComponent.type;
        Side direction = pendingStructureSpawnComponent.direction;

        List<SpawnPossiblity> spawnPossiblities = new ArrayList<>();
        for (EntityRef entity: entityManager.getEntitiesWith(StructureConnectionPointsComponent.class)) {
            StructureConnectionPointsComponent structureConnectionPointsComponent = entity.getComponent(
                    StructureConnectionPointsComponent.class);
            for (ConnectionPoint connectionPoint: structureConnectionPointsComponent.points) {
                if (connectionPoint.incoming && connectionPoint.type.equals(type)) {
                    spawnPossiblities.add(new SpawnPossiblity(entity, connectionPoint.direction,
                            connectionPoint.position));
                }
            }
        }

        if (spawnPossiblities.size() == 0) {
            return;
        }
        SpawnPossiblity spawnPossiblity = spawnPossiblities.get(random.nextInt(spawnPossiblities.size()));
        Vector3i spawnPosition = new Vector3i(locationComponent.getWorldPosition());
        Vector3i incomingConnectionPointPosition = new Vector3i(spawnPossiblity.getRelativeConnectionPointPostion());
        Side incomingConnectionPointDirection = spawnPossiblity.getRequiredDirection();

        BlockRegionTransformationList transformList = createTransformForIncomingConnectionPoint(direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        CheckSpawnConditionEvent checkSpawnConditionEvent = new CheckSpawnConditionEvent(transformList);
        EntityRef structureToSpawn = spawnPossiblity.getStructureToSpawn();
        structureToSpawn.send(checkSpawnConditionEvent);
        if (checkSpawnConditionEvent.isPreventSpawn()) {
            return;
        }
        structureToSpawn.send(new SpawnStructureEvent(transformList));

        randomEntity.destroy();
    }

    static BlockRegionTransformationList createTransformForIncomingConnectionPoint(Side direction, Vector3i spawnPosition, Vector3i incomingConnectionPointPosition, Side incomingConnectionPointDirection) {
        HorizontalBlockRegionRotation rot = HorizontalBlockRegionRotation.createRotationFromSideToSide(
                incomingConnectionPointDirection, direction);
        Vector3i tranformedOffset = rot.transformVector3i(incomingConnectionPointPosition);
        Vector3i actualSpawnPosition = new Vector3i(spawnPosition);
        actualSpawnPosition.sub(tranformedOffset);

        BlockRegionTransformationList transformList = new BlockRegionTransformationList();
        transformList.addTransformation(
                HorizontalBlockRegionRotation.createRotationFromSideToSide(incomingConnectionPointDirection, direction));
        transformList.addTransformation(new BlockRegionMovement(actualSpawnPosition));
        return transformList;
    }
}
