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
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gooeysQuests.api.BlockRegionTransform;
import org.terasology.gooeysQuests.api.FrontDirectionComponent;
import org.terasology.gooeysQuests.api.SpawnBlockRegionsComponent;
import org.terasology.gooeysQuests.api.SpawnStructureActionComponent;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerTargetChangedEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.logic.RegionOutlineComponent;
import org.terasology.world.block.BlockComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shows a preview/outline of the structure to spawn when the player hold a item with
 * {@link SpawnStructureActionComponent} and {@link SpawnBlockRegionsComponent}.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class StructureSpawnClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem{

    @In
    private ClipboardManager clipboardManager;

    @In
    private LocalPlayer locatPlayer;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    private List<EntityRef> regionOutlineEntities = new ArrayList<>();


    private Vector3i spawnPosition;

    private Side directionPlayerLooksAt;


    @Override
    public void update(float delta) {
        LocationComponent locationComponent = locatPlayer.getCharacterEntity().getComponent(LocationComponent.class);
        if (locationComponent == null) {
            directionPlayerLooksAt = null;
            return;
        }

        Vector3f directionVector = locationComponent.getWorldDirection();
        Side newDirection = Side.inHorizontalDirection(directionVector.getX(), directionVector.getZ());
        if (directionPlayerLooksAt != newDirection) {
            directionPlayerLooksAt = newDirection;
            updateOutlineEntity();
        }

    }

    @ReceiveEvent
    public void onActivatedSpawnStructureActionComponent(OnActivatedComponent event, EntityRef entity,
                                                SpawnStructureActionComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onChangedSpawnStructureActionOrBlockRegions(OnChangedComponent event, EntityRef entity,
                                             SpawnBlockRegionsComponent blockRegionComponent,
                                             SpawnStructureActionComponent spawnStructureComponent) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onBeforeDeactivateSpawnStructureActionComponent(BeforeDeactivateComponent event, EntityRef entity,
                                                                SpawnStructureActionComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onInventorySlotChanged(InventorySlotChangedEvent event, EntityRef entity) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onChangedSelectedInventorySlotComponent(OnChangedComponent event, EntityRef entity,
                                                SelectedInventorySlotComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onPlayerTargetChangedEvent(PlayerTargetChangedEvent event, EntityRef entity) {
        EntityRef newTarget = event.getNewTarget();
        BlockComponent blockComponent = newTarget.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            spawnPosition = blockComponent.getPosition();
        } else {
            spawnPosition = null;
        }
        updateOutlineEntity();
    }

    public void updateOutlineEntity() {
        List<Region3i> regionsToDraw = getRegionsToDraw();
        for (EntityRef entity: regionOutlineEntities) {
            if (entity.exists()) {
                entity.destroy();
            }
        }
        if (regionsToDraw.isEmpty()) {
            return;
        }

        for (Region3i region: regionsToDraw) {
            EntityBuilder entityBuilder = entityManager.newBuilder();
            entityBuilder.setPersistent(false);
            RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
            regionOutlineComponent.corner1 = new Vector3i(region.min());
            regionOutlineComponent.corner2 = new Vector3i(region.max());
            entityBuilder.addComponent(regionOutlineComponent);
            EntityRef entity = entityBuilder.build();
            regionOutlineEntities.add(entity);
        }
    }



    private List<Region3i> getRegionsToDraw() {
        EntityRef characterEntity = locatPlayer.getCharacterEntity();
        SelectedInventorySlotComponent selectedSlotComponent = characterEntity.
                getComponent(SelectedInventorySlotComponent.class);
        if (selectedSlotComponent == null) {
            return Collections.emptyList();
        }
        InventoryComponent inventoryComponent =  characterEntity.getComponent(InventoryComponent.class);
        if (inventoryComponent == null) {
            return Collections.emptyList();
        }
        inventoryComponent.itemSlots.get(selectedSlotComponent.slot);

        EntityRef item = inventoryManager.getItemInSlot(characterEntity, selectedSlotComponent.slot);
        SpawnBlockRegionsComponent spawnBlockRegionsComponent = item.getComponent(SpawnBlockRegionsComponent.class);
        if (spawnBlockRegionsComponent == null) {
            return Collections.emptyList();
        }

        if (spawnPosition == null) {
            return Collections.emptyList();
        }

        if (directionPlayerLooksAt == null) {
            return Collections.emptyList();
        }

        Side wantedFrontOfStructure = directionPlayerLooksAt.reverse();

        FrontDirectionComponent templateFrontDirComp = item.getComponent(FrontDirectionComponent.class);
        Side frontOfStructure = (templateFrontDirComp != null) ? templateFrontDirComp.direction : Side.FRONT;

        BlockRegionTransform regionTransform = SpawnStructureActionServerSystem.createBlockRegionTransformForCharacterTargeting(
                frontOfStructure, wantedFrontOfStructure, spawnPosition);

        List<Region3i> regionsToDraw = new ArrayList<>();
        for (SpawnBlockRegionsComponent.RegionToFill regionToFill: spawnBlockRegionsComponent.regionsToFill) {
            Region3i region = Region3i.createBounded(regionToFill.region.min,
                    regionToFill.region.max);
            region = regionTransform.transformRegion(region);
            regionsToDraw.add(region);
        }

        return regionsToDraw;
    }
}
