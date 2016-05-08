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
package org.terasology.gooeysQuests.api;

import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

/**
 * A {@link BlockRegionTransform} that adds a offset. Typically used to move the spawn location of a structure from
 * around (0,0,0) to a chosen location.
 */
public class BlockRegionMovement implements BlockRegionTransform {
    private Vector3i offset;

    public BlockRegionMovement(Vector3i offset) {
        this.offset = offset;
    }

    @Override
    public Block transformBlock(Block block) {
        return block;
    }

    @Override
    public Side transformSide(Side side) {
        return side;
    }

    @Override
    public Vector3i transformVector3i(Vector3i position) {
        Vector3i result = new Vector3i(position);
        result.add(offset);
        return result;
    }
}
