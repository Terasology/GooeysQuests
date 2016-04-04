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
package org.terasology.gooeysQuests.quests.dungeon;

import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gooeysQuests.api.BlockRegionChecker;
import org.terasology.gooeysQuests.api.CreateStartQuestsEvent;
import org.terasology.gooeysQuests.api.PersonalQuestsComponent;
import org.terasology.gooeysQuests.api.PrepareQuestEvent;
import org.terasology.gooeysQuests.api.QuestReadyEvent;
import org.terasology.gooeysQuests.api.QuestStartRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Makes gooey offer a dungeon quest
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DungeonQuestSystem extends BaseComponentSystem {

    private static final int MAX_HORIZONTAL_DISTANCE = 20;
    private static final int MIN_HORIZONTAL_DISTANCE = 5;
    private static final int VERTICAL_SCAN_DISTANCE = 5;
    private static final int DUNGEON_HEIGHT = 5;
    private static final int DUNGEON_LENGTH = 10;
    private static final int DUNGEON_WIDTH = 4;

    @In
    private AssetManager assetManager;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private BlockRegionChecker blockRegionChecker;

    private Map<EntityRef, Vector3i> questToFoundSpawnPositionMap = new HashMap<>();

    private Random random = new Random();

    @ReceiveEvent
    public void onCreateStartQuestsEvent(CreateStartQuestsEvent event, EntityRef character,
                                         PersonalQuestsComponent questsComponent) {
        Prefab questPrefab = assetManager.getAsset("GooeysQuests:DungeonQuest", Prefab.class).get();
        EntityBuilder questEntityBuilder = entityManager.newBuilder(questPrefab);
        questEntityBuilder.setOwner(character);
        EntityRef entity = questEntityBuilder.build();
        questsComponent.questsInPreperation.add(entity);
        character.saveComponent(questsComponent);
    }


    @ReceiveEvent(components = DungeonQuestComponent.class)
    public void onPrepareQuest(PrepareQuestEvent event, EntityRef quest) {

        EntityRef owner = quest.getOwner();
        LocationComponent questOwnerLocation = owner.getComponent(LocationComponent.class);
        Vector3i questOwnerBlockPos = new Vector3i(questOwnerLocation.getWorldPosition());
        Vector3i randomPosition = new Vector3i(questOwnerBlockPos);
        randomPosition.addX(randomHorizontalOffset());
        randomPosition.addZ(randomHorizontalOffset());

        Vector3i surfaceGroundBlockPosition = findSurfaceGroundBlockPosition(randomPosition);
        if (surfaceGroundBlockPosition == null) {
            return;
        }

        Region3i dungeonArea = getDungeonRegionFromSurfaceBlockPosition(surfaceGroundBlockPosition);

        if (!blockRegionChecker.allBlocksMatch(dungeonArea, BlockRegionChecker.BLOCK_IS_GROUND_LIKE)) {
            return;
        }
        questToFoundSpawnPositionMap.put(quest, surfaceGroundBlockPosition);
        quest.send(new QuestReadyEvent());
    }

    @ReceiveEvent(components = DungeonQuestComponent.class)
    public void onQuestStart(QuestStartRequest event, EntityRef quest) {
        Vector3i spawnPos = questToFoundSpawnPositionMap.get(quest);
        if (spawnPos == null) {
            return; // TODO report failure to client and gooey system
        }

        Region3i dungeonRegion = getDungeonRegionFromSurfaceBlockPosition(spawnPos);

        Block airBlock = blockManager.getBlock(BlockManager.AIR_ID);


        for (Vector3i pos : dungeonRegion) {
            worldProvider.setBlock(pos, airBlock);
        }

        // TODO example code for future use:
        // Block stairBlock = blockManager.getBlock("core:stone:engine:Stair.LEFT");
        // worldProvider.setBlock(spawnPos, stairBlock);
    }



    /**
     * Free the memory once the quest is no longer loaded
     */
    @ReceiveEvent
    public void onDeactivateQuestEntity(BeforeDeactivateComponent event, EntityRef questEntity,
                                        PersonalQuestsComponent questsComponent) {
        questToFoundSpawnPositionMap.remove(questEntity);
    }


    private Region3i getDungeonRegionFromSurfaceBlockPosition(Vector3i surfaceGroundBlockPosition) {
        int minX = surfaceGroundBlockPosition.getX() - ((DUNGEON_WIDTH / 2) - 1);
        int minY = surfaceGroundBlockPosition.getY() - (DUNGEON_HEIGHT - 1) ;
        int minZ = surfaceGroundBlockPosition.getZ() - ((DUNGEON_LENGTH / 2) - 1);
        int maxX = minX + (DUNGEON_WIDTH -1);
        int maxY = surfaceGroundBlockPosition.getY();
        int maxZ = minZ + (DUNGEON_LENGTH -1);

        Vector3i min = new Vector3i(minX, minY, minZ);
        Vector3i max = new Vector3i(maxX, maxY, maxZ);
        return Region3i.createFromMinMax(min, max);
    }


    private Vector3i findSurfaceGroundBlockPosition(Vector3i position) {
        int yScanStop = position.getY() - VERTICAL_SCAN_DISTANCE;
        int yScanStart = position.getY() + VERTICAL_SCAN_DISTANCE;
        // TODO simplify algorithm
        boolean airFound = false;
        for (int y = yScanStart; y> yScanStop; y--) {
            int x = position.getX();
            int z = position.getZ();
            Block block = worldProvider.getBlock(x, y, z);
            if (BlockRegionChecker.BLOCK_IS_AIR_LIKE.test(block)) {
                airFound = true;
            } else if (BlockRegionChecker.BLOCK_IS_GROUND_LIKE.test(block)) {
                if (!airFound) {
                    return null; // found ground first -> not surface
                }
                return new Vector3i(x, y, z);
            } else {
                return null; //neither ground nor air (e.g. water)
            }
        }
        return null; // no ground found
    }

    private int randomHorizontalOffset() {
        return randomSign() * (MIN_HORIZONTAL_DISTANCE + random.nextInt(MAX_HORIZONTAL_DISTANCE));
    }

    private int randomSign() {
        if (random.nextBoolean()) {
            return 1;
        } else {
            return -1;
        }
    }
}
