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

import org.junit.Test;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.util.transform.BlockRegionTransformationList;

import static org.junit.Assert.assertEquals;
import static org.terasology.gooeysQuests.ConnectedStructureSpawnSystem.createTransformForIncomingConnectionPoint;

/**
 *
 */
public class ConnectedStructureSpawnSystemTest {

    @Test
    public void testCreateTransformForIncomingConnectionPoint() {
        /*-
         * |----------|
         * |          |
         * |i         |
         * |          |
         * |   o      |
         * |----------|
         * i = incoming connection point at (0,0,4) with direction left
         * o = outgoing connection point at (3,0,0) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with front at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         *
         * |----------|
         * |          |
         * |          |
         * |         o|
         * |    i     |
         * |----------|
         *
         */


        Side direction = Side.FRONT;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(0, 0, 4);
        Side incomingConnectionPointDirection = Side.LEFT;

        BlockRegionTransformationList result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.FRONT, result.transformSide(Side.LEFT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        Vector3i outoingConnectionPointPosition = new Vector3i(3, 0, 0);

        // output point transformed:
        assertEquals(Side.RIGHT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(104, 200, 303),  result.transformVector3i(new Vector3i(3, 0, 0)));

    }



    @Test
    public void testCreateTransformForIncomingConnectionPoint2() {
        /*-
         * |----------|
         * |          |
         * |i         |
         * |          |
         * |   o      |
         * |----------|
         * i = incoming connection point at (0,0,4) with direction left
         * o = outgoing connection point at (3,0,0) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with back at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         *
         * |----------|
         * |     i    |
         * |o         |
         * |          |
         * |          |
         * |----------|
         *
         */


        Side direction = Side.BACK;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(0, 0, 4);
        Side incomingConnectionPointDirection = Side.LEFT;

        BlockRegionTransformationList result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.BACK, result.transformSide(Side.LEFT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        Vector3i outoingConnectionPointPosition = new Vector3i(3, 0, 0);

        // output point transformed:
        assertEquals(Side.LEFT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(100 - 4, 200, 300 - 3),  result.transformVector3i(new Vector3i(3, 0, 0)));

    }


    @Test
    public void testCreateTransformForIncomingConnectionPoint3() {
        /*-
         * |---------|
         * |    o    |
         * |         |
         * |         |
         * |    i    |
         * |---------|
         * i = incoming connection point at (2,0,0) with direction left
         * o = outgoing connection point at (2,0,5) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with right at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         *
         * |------------|
         * |            |
         * |o          i|
         * |            |
         * |------------|
         *
         */


        Side direction = Side.RIGHT;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(2, 0, 0);
        Side incomingConnectionPointDirection = Side.FRONT;

        BlockRegionTransformationList result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.RIGHT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        // output point transformed:
        assertEquals(Side.LEFT, result.transformSide(Side.BACK));
        assertEquals(new Vector3i(100 - 5, 200, 300),  result.transformVector3i(new Vector3i(2, 0, 5)));

    }


    @Test
    public void testCreateTransformForIncomingConnectionPoint4CorrectcoordinateSystem() {
        /*-
         * |------------------------> x-Axis
         * |           Front
         * |
         * |        |---------|
         * |        |    i    |
         * |  Left  |         |   Right
         * |        |         |
         * |        |    o    |
         * |        |---------|
         * |
         * |            Back
         * |
         * v z-Axis,
         *
         * i = incoming connection point at (2,0,0) with direction left
         * o = outgoing connection point at (2,0,5) with direciton front
         *
         * ||||||||||||transform||||||||||||
         * place with right at 100, 200, 300
         * vvvvvvvvvvvvvVVvvvvvvvvvvvvvVVvvv
         * |------------------------> x-Axis
         * |           Front
         * |
         * |        |------------|
         * |        |            |
         * |  Left  |o          i|  Right
         * |        |            |
         * |        |------------|
         * |
         * |            Back
         * |
         * v z-Axis,
         *
         *
         */



        Side direction = Side.RIGHT;
        Vector3i spawnPosition = new Vector3i(100, 200, 300);
        Vector3i incomingConnectionPointPosition = new Vector3i(2, 0, 0);
        Side incomingConnectionPointDirection = Side.FRONT;

        BlockRegionTransformationList result = createTransformForIncomingConnectionPoint(
                direction, spawnPosition, incomingConnectionPointPosition, incomingConnectionPointDirection);

        // input point transformed:
        assertEquals(Side.RIGHT, result.transformSide(Side.FRONT));
        assertEquals(new Vector3i(100, 200, 300), result.transformVector3i(incomingConnectionPointPosition));

        // output point transformed:
        assertEquals(Side.LEFT, result.transformSide(Side.BACK));
        assertEquals(new Vector3i(100 - 5, 200, 300),  result.transformVector3i(new Vector3i(2, 0, 5)));

    }
}
