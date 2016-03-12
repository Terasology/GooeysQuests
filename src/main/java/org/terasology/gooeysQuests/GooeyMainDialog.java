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
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UIText;

/**
 * Dialog that gets shown when you interact (per default hotkey E) with gooey.
 */
public class GooeyMainDialog extends BaseInteractionScreen {
    private static final String GOOEYS_TEXT =                    "Hi, I am Gooey\n\n"
            +                    "I can use my magic to make the world around us a bit more interesting.\n\n"
            +                    "Just tell me when we are in an area where you want to start a quest and I will see if I can come up with something.\n\n"
            +                    "Be however warned: My magic may tear our surroundings appart or add new stuff to it.\n\n"
            +                    "So you may want to lead me to another place first before we start a quest.";

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {

    }

    @Override
    protected void initialise() {
        UIText gooeysText = find("gooeysText", UIText.class);
        if (gooeysText != null) {
            gooeysText.setText(GOOEYS_TEXT);
        }
        WidgetUtil.trySubscribe(this, "closeButton", button -> getManager().popScreen());
    }

}
