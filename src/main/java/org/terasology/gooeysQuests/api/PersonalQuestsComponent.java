// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.api;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * Player characters start without this component. When a joined player character does not have this event yet, the
 * component will be added and a {@link CreateStartQuestsEvent}  event will be sent to the character.
 * <p>
 * Systems can react to the {@link CreateStartQuestsEvent} sent to entties with this compoent by adding their own quest
 * to the component. The {@link CreateStartQuestsEvent} event can be consumed to prevent the creation of the default
 * quests.
 */
public class PersonalQuestsComponent implements Component {

    /**
     * Quests that are currently being prepared. Once the preperation is done, gooey will offer the quest to the
     * player.
     */
    public List<EntityRef> questsInPreperation = Lists.newArrayList();

    // public EntityRef activeQuest = EntityRef.NULL;

    // TODO allow the player to reject certain categories of quests
    // public Set<String> rejectedQuestCategories = Sets.newHashSet();
}
