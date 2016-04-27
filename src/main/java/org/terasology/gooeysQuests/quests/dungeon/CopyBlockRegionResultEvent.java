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

import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;

/**
 * Sent from the server to the client with copied data.
 */
@OwnerEvent
public class CopyBlockRegionResultEvent implements Event {
    private String json;

    protected CopyBlockRegionResultEvent() {
        // for serialization
    }

    public CopyBlockRegionResultEvent(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }
}
