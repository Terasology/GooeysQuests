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
package org.terasology.gooeysQuests.quests.dungeon;

import org.terasology.gooeysQuests.api.BlockRegionChecker;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

import java.util.function.Predicate;

/**
 * Contains the different checks that need to be performed to find a good spawn area for a dungeon entrance
 * that is at the east side of a mountain.
 */
public enum EntranceRegionWithCondition implements RegionWithCondition {

    FIRST_ROW_GROUND(-1, 1, 0, 0, 0, 0, BlockRegionChecker.BLOCK_IS_GROUND_LIKE),

    FIRST_ROW_AIR_ABOVE(-1, 1, 1, 4, 0, 0, BlockRegionChecker.BLOCK_IS_AIR_LIKE),

    SECOND_ROW_GROUND(-2, 2, 0, 0, 1, 1, BlockRegionChecker.BLOCK_IS_GROUND_LIKE),

    SECOND_ROW_AIR_ABOVE(-1, 1, 2, 3, 1, 1, BlockRegionChecker.BLOCK_IS_AIR_LIKE),

    THIRD_ROW_GROUND(-2, 2, -1, 2, 2, 2, BlockRegionChecker.BLOCK_IS_GROUND_LIKE),

    FOURTH_ROW_GROUND(-2, 2, -1, 3, 3, 3, BlockRegionChecker.BLOCK_IS_GROUND_LIKE),

    REMAINING_ROWS_GROUND(-2, 2, -1, 3, 4, 11, BlockRegionChecker.BLOCK_IS_GROUND_LIKE),


    ;

    private Predicate<Block> condition;
    private Region3i region;

    EntranceRegionWithCondition(int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Predicate<Block> condition) {
        Vector3i min = new Vector3i(minX, minY, minZ);
        Vector3i max = new Vector3i(maxX, maxY, maxZ);
        this.region = Region3i.createFromMinMax(min, max);
        this.condition = condition;
    }

    @Override
    public Region3i getRegion(Vector3i origin) {
        return region.move(origin);
    }

    @Override
    public Predicate<Block> getCondition() {
        return condition;
    }
}
