// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.api;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.entitySystem.event.ConsumableEvent;

/**
 * Sent to an entity with {@link PersonalQuestsComponent} when that component should be filled with the initial quests.
 */
public class CreateStartQuestsEvent extends AbstractConsumableEvent implements ConsumableEvent {

}
