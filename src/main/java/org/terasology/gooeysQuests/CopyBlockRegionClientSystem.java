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
import org.terasology.gooeysQuests.quests.dungeon.CopyBlockRegionResultEvent;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.logic.RegionOutlineComponent;

/**
 * Handles the result of the activation of the copyBlockRegionTool item.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CopyBlockRegionClientSystem extends BaseComponentSystem {

    @In
    private ClipboardManager clipboardManager;

    @In
    private LocalPlayer locatPlayer;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    private EntityRef regionOutlineEntity = EntityRef.NULL;
    private Region3i regionWithOutline;


    @ReceiveEvent
    public void onCopyBlockRegionResultEvent(CopyBlockRegionResultEvent event, EntityRef entity) {
        clipboardManager.setClipboardContents(event.getJson());
    }

    @ReceiveEvent
    public void onAddedCopyBlockRegionComponent(OnAddedComponent event, EntityRef entity,
                                                CopyBlockRegionComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onChangedCopyBlockRegionComponent(OnChangedComponent event, EntityRef entity,
                                             CopyBlockRegionComponent component) {
        updateOutlineEntity();
    }

    @ReceiveEvent
    public void onBeforeRemoveCopyBlockRegionComponent(BeforeRemoveComponent event, EntityRef entity,
                                                  CopyBlockRegionComponent component) {
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


    public void updateOutlineEntity() {
        Region3i region3i = getRegionToDraw();
        if (region3i == null) {
            if (regionOutlineEntity.exists()) {
                regionOutlineEntity.destroy();
            }
        } else {
            if (regionOutlineEntity.exists()) {
                RegionOutlineComponent oldComponent = regionOutlineEntity.getComponent(RegionOutlineComponent.class);
                if (oldComponent != null && oldComponent.corner1 != null && oldComponent.corner2 != null) {
                    Region3i oldRegion = Region3i.createBounded(oldComponent.corner1, oldComponent.corner2);
                    if (oldRegion.equals(region3i)) {
                        return;
                    }
                }
            }
            if (regionOutlineEntity.exists()) {
                RegionOutlineComponent regionOutlineComponent = regionOutlineEntity.getComponent(RegionOutlineComponent.class);
                regionOutlineComponent.corner1 = new Vector3i(region3i.min());
                regionOutlineComponent.corner2 = new Vector3i(region3i.max());
                regionOutlineEntity.saveComponent(regionOutlineComponent);
            } else {
                EntityBuilder entityBuilder = entityManager.newBuilder();
                entityBuilder.setPersistent(false);
                RegionOutlineComponent regionOutlineComponent = new RegionOutlineComponent();
                regionOutlineComponent.corner1 = new Vector3i(region3i.min());
                regionOutlineComponent.corner2 = new Vector3i(region3i.max());
                entityBuilder.addComponent(regionOutlineComponent);
                regionOutlineEntity = entityBuilder.build();
            }
        }
    }



    private Region3i getRegionToDraw() {
        EntityRef characterEntity = locatPlayer.getCharacterEntity();
        SelectedInventorySlotComponent selectedSlotComponent = characterEntity.
                getComponent(SelectedInventorySlotComponent.class);
        if (selectedSlotComponent == null) {
            return null;
        }
        InventoryComponent inventoryComponent =  characterEntity.getComponent(InventoryComponent.class);
        if (inventoryComponent == null) {
            return null;
        }
        inventoryComponent.itemSlots.get(selectedSlotComponent.slot);

        EntityRef item = inventoryManager.getItemInSlot(characterEntity, selectedSlotComponent.slot);
        CopyBlockRegionComponent copyBlockRegionComponent = item.getComponent(CopyBlockRegionComponent.class);
        if (copyBlockRegionComponent == null) {
            return null;
        }

        if (copyBlockRegionComponent.origin == null) {
            return null;
        }

        Region3i region = Region3i.createBounded(copyBlockRegionComponent.corner1, copyBlockRegionComponent.corner2);
        region.move(copyBlockRegionComponent.origin);

        Vector3i min = new Vector3i(copyBlockRegionComponent.corner1);
        min.min(copyBlockRegionComponent.corner2);
        Vector3i max = new Vector3i(copyBlockRegionComponent.corner1);
        max.max(copyBlockRegionComponent.corner2);
        return region;
    }
}
