// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.api;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

/**
 * Sent to a quest at the server from the client when the player orders gooey to start the quest.
 */
@ServerEvent
public class QuestStartRequest implements Event {

}
