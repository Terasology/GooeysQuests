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
import org.terasology.gooeysQuests.api.AbstractBlockRegionConditionComponent;
import org.terasology.gooeysQuests.api.AirLikeBlockRegionConditionComponent;
import org.terasology.gooeysQuests.api.BlockRegionChecker;
import org.terasology.gooeysQuests.api.CheckSpawnConditionEvent;
import org.terasology.gooeysQuests.api.SolidBlockRegionConditionComponent;
import org.terasology.math.Region3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.structureTemplates.components.container.Region;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.function.Predicate;

/**
 * Implementation of {@link BlockRegionChecker}. See interface for more information.
 *
 * Implements also the checking of some condition components.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(BlockRegionChecker.class)
public class QuestBlockAreaCheckerSystem extends BaseComponentSystem implements BlockRegionChecker {

    @In
    private WorldProvider worldProvider;

    @Override
    public boolean allBlocksMatch(Region3i region, Predicate<Block> condition) {
        return allBlocksInAABBMatch(region.minX(), region.maxX(), region.minY(), region.maxY(), region.minZ(),
                region.maxZ(), condition);
    }

    private boolean allBlocksInAABBMatch(int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                         Predicate<Block> condition) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = worldProvider.getBlock(x ,y, z);
                    if (!condition.test(block)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void checkSpawnFor(CheckSpawnConditionEvent event, EntityRef entity,
                              AbstractBlockRegionConditionComponent component, Predicate<Block> condition) {
        for (Region region: component.regions) {
            Region3i region3i = Region3i.createBounded(region.min, region.max);
            region3i = region3i.move(event.getSpawnPosition());
            boolean match = allBlocksMatch(region3i, condition);
            if (!match) {
                event.consume();
                return;
            }
        }
    }


    @ReceiveEvent
    public void onAirLikeBlockRegionCheck(CheckSpawnConditionEvent event, EntityRef entity,
                             AirLikeBlockRegionConditionComponent conditionComponent) {
        checkSpawnFor(event, entity, conditionComponent, BlockRegionChecker.BLOCK_IS_AIR_LIKE);
    }

    @ReceiveEvent
    public void onSolidBlockRegionCheck(CheckSpawnConditionEvent event, EntityRef entity,
                                      SolidBlockRegionConditionComponent conditionComponent) {
        checkSpawnFor(event, entity, conditionComponent, BlockRegionChecker.BLOCK_IS_GROUND_LIKE);
    }

}
