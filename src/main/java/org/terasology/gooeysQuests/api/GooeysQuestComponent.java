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

/**
 * Marks an entity to be a quest offered by gooey.
 *
 * Gooey's quest system sends the {@link PrepareQuestEvent} event very frequently to quests that the player wants to do.
 *
 * For each of gooey's quests there is a system. The system identifies events for its quest via a unique component
 * that is also at the quest's entity. Whenever the entity receives the {@link PrepareQuestEvent} it does a little
 * bit of quest preparation like searching a good spawn position for a building. The quest should not do all the
 * preperation work at once, but split it up so that there is no performance impact. When a quest is done with it's
 * preperation, it sends a {@link QuestReadyEvent} to the quest entitiy.
 *
 */
public class GooeysQuestComponent implements Component {
    public String greetingText = "Hi";
    public String startButtonText = "Start Quest";
    public String description="Up for a quest?\n\n"
            + "Sadly I have no further description for you.\n\n"
            + "However be warned: Starting a quest can result in possibly unwanted world modificaitons!";
}
