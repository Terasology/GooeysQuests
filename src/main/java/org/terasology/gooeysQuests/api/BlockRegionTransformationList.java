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

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a list of transformations.
 */
public class BlockRegionTransformationList implements BlockRegionTransform {
    private List<BlockRegionTransform> transformations = new ArrayList<>();

    /**
     * The added transformation will be applied after the previous ones.
     * @param transformation
     */
    public void addTransformation(BlockRegionTransform transformation) {
        transformations.add(transformation);
    }

    @Override
    public Block transformBlock(Block block) {
        Block result = block;
        for (BlockRegionTransform transform: transformations) {
            result = transform.transformBlock(result);
        }
        return result;
    }

    @Override
    public Side transformSide(Side side) {
        Side result = side;
        for (BlockRegionTransform transform: transformations) {
            result = transform.transformSide(result);
        }
        return result;
    }

    @Override
    public Vector3i transformVector3i(Vector3i position) {
        Vector3i result = position;
        for (BlockRegionTransform transform: transformations) {
            result = transform.transformVector3i(result);
        }
        return result;
    }
}
