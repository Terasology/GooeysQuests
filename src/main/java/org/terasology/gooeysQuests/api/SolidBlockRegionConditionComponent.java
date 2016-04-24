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

/**
 * When a CheckSpawnConditionEvent gets sent to an entity with this component, than the event will be consumed,
 * if some specified regions (relative to spawn position sent in event) are not filled with not penetratable blocks.
 */
public class SolidBlockRegionConditionComponent extends AbstractBlockRegionConditionComponent {

}
