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
import org.terasology.gooeysQuests.api.CreateStructureSpawnItemRequest;
import org.terasology.gooeysQuests.quests.dungeon.CopyBlockRegionRequest;
import org.terasology.rendering.nui.BaseInteractionScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;

/**
 * Main structure template editor UI
 */
public class StructureTemplateEditorScreen extends BaseInteractionScreen {

    private UIText minXField;
    private UIText minYField;
    private UIText minZField;
    private UIText maxXField;
    private UIText maxYField;
    private UIText maxZField;
    private UIButton copyToClipboardButton;
    private UIButton createSpawnerButton;

    @Override
    protected void initializeWithInteractionTarget(EntityRef interactionTarget) {
        // nothing to do
    }

    @Override
    protected void initialise() {
        minXField = find("minXField", UIText.class);
        minXField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.min.x();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                editorComponent.editRegion.min.setX(value);
            }
        });

        minYField = find("minYField", UIText.class);
        minYField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.min.y();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                editorComponent.editRegion.min.setY(value);
            }
        });

        minZField = find("minZField", UIText.class);
        minZField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.min.z();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                editorComponent.editRegion.min.setZ(value);
            }
        });

        maxXField = find("maxXField", UIText.class);
        maxXField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.max.x();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                editorComponent.editRegion.max.setX(value);
            }
        });

        maxYField = find("maxYField", UIText.class);
        maxYField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.max.y();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                editorComponent.editRegion.max.setY(value);
            }
        });

        maxZField = find("maxZField", UIText.class);
        maxZField.bindText(new AbstractIntEditorPropertyBinding() {
            @Override
            public int getInt(StructureTemplateEditorComponent editorComponent) {
                return editorComponent.editRegion.max.z();
            }

            @Override
            public void setInt(StructureTemplateEditorComponent editorComponent, int value) {
                editorComponent.editRegion.max.setZ(value);
            }
        });

        copyToClipboardButton = find("copyToClipboardButton", UIButton.class);
        if (copyToClipboardButton != null) {
            copyToClipboardButton.subscribe(this::onCopyToClipboardClicked);
        }

        createSpawnerButton = find("createSpawnerButton", UIButton.class);
        if (createSpawnerButton != null) {
            createSpawnerButton.subscribe(this::onCreateSpawnerButton);
        }

    }

    private void onCreateSpawnerButton(UIWidget button) {
        getInteractionTarget().send(new CreateStructureSpawnItemRequest());
    }

    private void onCopyToClipboardClicked(UIWidget button) {
        getInteractionTarget().send(new CopyBlockRegionRequest());
    }

    private abstract class AbstractIntEditorPropertyBinding implements Binding<String> {
        public abstract int getInt(StructureTemplateEditorComponent editorComponent);
        public abstract void setInt(StructureTemplateEditorComponent editorComponent,
                                    int value);
        @Override
        public String get() {
            EntityRef entity = getInteractionTarget();
            StructureTemplateEditorComponent component = entity.getComponent(StructureTemplateEditorComponent.class);
            return Integer.toString(getInt(component));
        }

        @Override
        public void set(String value) {
            int intValue;
            try {
                intValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return; // do nothing (ignore invalid change request and keep old valid value)
            }
            EntityRef entity = getInteractionTarget();
            StructureTemplateEditorComponent component = entity.getComponent(StructureTemplateEditorComponent.class);
            setInt(component ,intValue);
            entity.saveComponent(component);
        }
    }
}
