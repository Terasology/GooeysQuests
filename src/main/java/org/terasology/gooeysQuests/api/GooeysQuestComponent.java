// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.api;

import org.terasology.gestalt.entitysystem.component.Component;

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
public class GooeysQuestComponent implements Component<GooeysQuestComponent> {
    public String greetingText = "Hi";
    public String startButtonText = "Start Quest";
    public String description="Up for a quest?\n\n"
            + "Sadly I have no further description for you.\n\n"
            + "However be warned: Starting a quest can result in possibly unwanted world modifications!";
}
