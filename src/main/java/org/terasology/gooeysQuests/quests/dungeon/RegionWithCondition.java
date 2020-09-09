// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.quests.dungeon;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

import java.util.function.Predicate;

/**
 * A block region with a condition that must be true for all blocks.
 */
public interface RegionWithCondition {
    Predicate<Block> getCondition();

    Region3i getRegion(Vector3i origin);
}
