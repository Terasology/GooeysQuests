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

import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.AttachedToSurfaceFamily;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.SideDefinedBlockFamily;

/**
 * Allows you to roate block regions by 90 degree.
 */
public class HorizontalBlockRegionRotation implements BlockRegionTransform {
    /**
     * How often it will be rotated around the y axis by 90 degree. Must be either 0, 1, 2 or 3.
     */
    private int counterClockWiseHorizontal90DegreeRotations = 0;


    public static HorizontalBlockRegionRotation createRotationFromSideToSide(Side startSide, Side targetSide) {
        return new HorizontalBlockRegionRotation(counterClockWiseTurnsFromSideToSide(startSide, targetSide));
    }

    public HorizontalBlockRegionRotation(int counterClockWiseHorizontal90DegreeRotations) {
        this.counterClockWiseHorizontal90DegreeRotations = counterClockWiseHorizontal90DegreeRotations;
    }

    @Override
    public Region3i transformRegion(Region3i region) {
        return Region3i.createBounded(transformVector3i(region.min()), transformVector3i(region.max()));
    }

    @Override
    public Block transformBlock(Block block) {
        BlockFamily blockFamily = block.getBlockFamily();
        if (blockFamily instanceof SideDefinedBlockFamily) {
            SideDefinedBlockFamily sideDefinedBlockFamily = (SideDefinedBlockFamily) blockFamily;
            return sideDefinedBlockFamily.getBlockForSide(transformSide(block.getDirection()));
        } else if (blockFamily instanceof AttachedToSurfaceFamily) {
            // TODO add some proper method to block famility to not have to do this hack
            return blockFamily.getBlockForPlacement(null, null,null, transformSide(block.getDirection()), null);
        }
        return block;
    }

    @Override
    public Side transformSide(Side side) {
        return side.yawClockwise(4 - counterClockWiseHorizontal90DegreeRotations);
    }

    @Override
    public Vector3i transformVector3i(Vector3i position) {
        Vector3i result = new Vector3i(position);
        for (int i = 0; i < counterClockWiseHorizontal90DegreeRotations; i++) {
            int xBackup = result.x();
            int zBackup = result.z();
            result.setX(-zBackup);
            result.setZ(xBackup);
        }
        return result;
    }

    private static int sideToCounterClockwiseTurnsFromRight(Side side) {
        switch (side) {
            case RIGHT:
                return 0;
            case BACK:
                return 1;
            case LEFT:
                return 2;
            case FRONT:
                return 3;
            default:
                throw new IllegalArgumentException("not a horizontal side " + side);
        }
    }

    private static int counterClockWiseTurnsFromSideToSide(Side startSide, Side endSide) {
        int turnsFromRightStart = sideToCounterClockwiseTurnsFromRight(startSide);
        int turnsFromRightEnd = sideToCounterClockwiseTurnsFromRight(endSide);
        int turns = turnsFromRightEnd - turnsFromRightStart;
        if (turns < 0) {
            turns += 4;
        }
        return turns;
    }
}
