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
import org.terasology.gooeysQuests.quests.dungeon.CopyBlockRegionResultEvent;
import org.terasology.logic.clipboard.ClipboardManager;
import org.terasology.registry.In;

/**
 * Handles the result of the activation of the copyBlockRegionTool item.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CopyBlockRegionClientSystem extends BaseComponentSystem {

    @In
    private ClipboardManager clipboardManager;

    @ReceiveEvent
    public void onCopyBlockRegionResultEvent(CopyBlockRegionResultEvent event, EntityRef entity) {
        clipboardManager.setClipboardContents(event.getJson());
    }
}
