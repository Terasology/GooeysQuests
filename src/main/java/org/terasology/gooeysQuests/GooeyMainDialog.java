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

import org.terasology.module.behaviors.components.FollowComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.BaseInteractionScreen;
import org.terasology.gooeysQuests.api.GooeysQuestComponent;
import org.terasology.gooeysQuests.api.QuestStartRequest;
import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIText;

/**
 * Dialog that gets shown when you interact (per default hotkey E) with gooey.
 */
public class GooeyMainDialog extends BaseInteractionScreen {
    private static final String GOOEYS_TEXT_PREFIX = "Hi, I am Gooey\n\n";

    @In
    private LocalPlayer localPlayer;

    private EntityRef gooey;
    private UIButton startQuestButton;
    private UIText gooeysText;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        this.gooey = interactionTarget;

        GooeyComponent gooeyComponent = gooey.getComponent(GooeyComponent.class);
        EntityRef offeredQuest = gooeyComponent.offeredQuest;
        GooeysQuestComponent gooeysQuestComponent = offeredQuest.getComponent(GooeysQuestComponent.class);
        if (gooeysQuestComponent == null) {
            gooeysQuestComponent = new GooeysQuestComponent();
        }

        if (startQuestButton != null) {
            startQuestButton.setText(gooeysQuestComponent.startButtonText);
        }

        if (gooeysText != null) {
            gooeysText.setText(GOOEYS_TEXT_PREFIX + gooeysQuestComponent.description);
        }
    }

    @Override
    public void initialise() {
        gooeysText = find("gooeysText", UIText.class);

        WidgetUtil.trySubscribe(this, "closeButton", button -> getManager().popScreen());
        WidgetUtil.trySubscribe(this, "followButton", this::onFollowClicked);
        WidgetUtil.trySubscribe(this, "stayButton", this::onStayClicked);

        startQuestButton = find("startQuestButton", UIButton.class);
        if (startQuestButton != null) {
            startQuestButton.subscribe(this::onStartQuestClicked);
        }
    }



    private void onFollowClicked(UIWidget clickedButton) {
        setEntityToFollow(localPlayer.getCharacterEntity());
        localPlayer.getClientEntity().send(new ChatMessageEvent("Lead the way, I will follow you", gooey));
        getManager().popScreen();
    }

    private void onStayClicked(UIWidget clickedButton) {
        setEntityToFollow(EntityRef.NULL);
        localPlayer.getClientEntity().send(new ChatMessageEvent("Ok, I will stay here", gooey));
        getManager().popScreen();
    }

    private void setEntityToFollow(EntityRef entityToFollow) {
        FollowComponent followWish = gooey.getComponent(FollowComponent.class);
        if (followWish == null) {
            followWish = new FollowComponent();
        }
        followWish.entityToFollow = entityToFollow;
        gooey.addOrSaveComponent(followWish);
    }


    private void onStartQuestClicked(UIWidget clickedButton) {
        GooeyComponent gooeyComponent = gooey.getComponent(GooeyComponent.class);
        EntityRef offeredQuest = gooeyComponent.offeredQuest;
        if (!offeredQuest.isActive()) {
            localPlayer.getClientEntity().send(new ChatMessageEvent("Oops, that quest is no longer doable", gooey));
            return;
        }
        // TODO should it be a request instead so that it can be checked if the quest is still doable?
        offeredQuest.send(new QuestStartRequest());
        localPlayer.getClientEntity().send(new ChatMessageEvent("Ha ha, that will be fun!", gooey));
        getManager().popScreen();
    }
}
