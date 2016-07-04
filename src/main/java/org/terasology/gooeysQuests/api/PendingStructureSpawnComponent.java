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

import org.terasology.entitySystem.Component;
import org.terasology.math.Side;

/**
 * The entity is a placeholder for a structure. The structure will be chosen randomly from all entities that have a
 * matching incoming connection point specified with {@link StructureConnectionPointsComponent}.
 */
public class PendingStructureSpawnComponent implements Component {
    /**
     * Type of the connection point. Two connection poitns can only be connected if they have the same type.
     *
     */
    public String type;
    /**
     * The direction the connection is facing. The point this structure can connect to must have the opposite
     * direction value. When a connection point is at the front of a structure then this value must be also set to
     * front.
     */
    public Side direction;
}
