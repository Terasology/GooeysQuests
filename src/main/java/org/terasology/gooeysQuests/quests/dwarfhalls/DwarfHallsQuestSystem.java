// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gooeysQuests.quests.dwarfhalls;

import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gooeysQuests.api.CreateStartQuestsEvent;
import org.terasology.gooeysQuests.api.PersonalQuestsComponent;
import org.terasology.gooeysQuests.api.PrepareQuestEvent;
import org.terasology.gooeysQuests.api.QuestReadyEvent;
import org.terasology.gooeysQuests.api.QuestStartRequest;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.math.geom.Vector3i;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.interfaces.BlockPredicateProvider;
import org.terasology.structureTemplates.interfaces.StructureTemplateProvider;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Makes gooey offer a dungeon quest
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DwarfHallsQuestSystem extends BaseComponentSystem {

    private static final int MAX_HORIZONTAL_DISTANCE = 20;
    private static final int VERTICAL_SCAN_DISTANCE = 5;
    private final Map<EntityRef, FoundSpawnPossiblity> questToFoundSpawnPossibilityMap = new HashMap<>();
    private final Random random = new Random();
    @In
    private AssetManager assetManager;
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private BlockPredicateProvider blockPredicateProvider;
    @In
    private StructureTemplateProvider structureTemplateProvider;
    private Predicate<Block> isAirCondition;
    private Predicate<Block> isGroundCondition;


    @Override
    public void postBegin() {
        isAirCondition = blockPredicateProvider.getBlockPredicate("StructureTemplates:IsAirLike");
        isGroundCondition = blockPredicateProvider.getBlockPredicate("StructureTemplates:IsGroundLike");
    }

    @ReceiveEvent
    public void onCreateStartQuestsEvent(CreateStartQuestsEvent event, EntityRef character,
                                         PersonalQuestsComponent questsComponent) {
        Prefab questPrefab = assetManager.getAsset("GooeysQuests:DwarfHallsQuest", Prefab.class).get();
        EntityBuilder questEntityBuilder = entityManager.newBuilder(questPrefab);
        questEntityBuilder.setOwner(character);
        EntityRef entity = questEntityBuilder.build();
        questsComponent.questsInPreperation.add(entity);
        character.saveComponent(questsComponent);
    }


    @ReceiveEvent(components = DwarfHallsQuestComponent.class)
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

        EntityRef entranceSpawner = structureTemplateProvider.getRandomTemplateOfType("GooeysQuests" +
                ":dwarfHallsEntrance");

        BlockRegionTransform foundSpawnTransformation = findGoodSpawnTransformation(surfaceGroundBlockPosition,
                entranceSpawner);
        if (foundSpawnTransformation == null) {
            return;
        }


        questToFoundSpawnPossibilityMap.put(quest, new FoundSpawnPossiblity(entranceSpawner, foundSpawnTransformation));
        quest.send(new QuestReadyEvent());
    }

    private BlockRegionTransform findGoodSpawnTransformation(Vector3i spawnPosition, EntityRef entranceSpawner) {
        for (Side side : Side.horizontalSides()) {
            BlockRegionTransform transformList = createTransformation(spawnPosition, side);

            CheckSpawnConditionEvent checkConditionEvent = new CheckSpawnConditionEvent(transformList);
            entranceSpawner.send(checkConditionEvent);
            if (!checkConditionEvent.isPreventSpawn()) {
                return transformList;
            }
        }

        return null;
    }

    private BlockRegionTransform createTransformation(Vector3i spawnPosition, Side side) {
        return BlockRegionTransform.createRotationThenMovement(Side.FRONT, side, spawnPosition);
    }

    @ReceiveEvent(components = DwarfHallsQuestComponent.class)
    public void onQuestStart(QuestStartRequest event, EntityRef quest) {
        FoundSpawnPossiblity spawnPossiblity = questToFoundSpawnPossibilityMap.get(quest);
        BlockRegionTransform spawnTransformation = spawnPossiblity.getTransformation();
        if (spawnTransformation == null) {
            return; // TODO report failure to client and gooey system
        }

        EntityRef entranceSpawner = spawnPossiblity.getEntranceSpawner();
        entranceSpawner.send(new SpawnStructureEvent(spawnTransformation));
    }

    /**
     * Free the memory once the quest is no longer loaded
     */
    @ReceiveEvent
    public void onDeactivateQuestEntity(BeforeDeactivateComponent event, EntityRef questEntity,
                                        PersonalQuestsComponent questsComponent) {
        questToFoundSpawnPossibilityMap.remove(questEntity);
    }

    private Vector3i findSurfaceGroundBlockPosition(Vector3i position) {
        int yScanStop = position.getY() - VERTICAL_SCAN_DISTANCE;
        int yScanStart = position.getY() + VERTICAL_SCAN_DISTANCE;
        // TODO simplify algorithm
        boolean airFound = false;
        for (int y = yScanStart; y > yScanStop; y--) {
            int x = position.getX();
            int z = position.getZ();
            Block block = worldProvider.getBlock(x, y, z);
            if (isAirCondition.test(block)) {
                airFound = true;
            } else if (isGroundCondition.test(block)) {
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
        return randomSign() * (random.nextInt(MAX_HORIZONTAL_DISTANCE));
    }

    private int randomSign() {
        if (random.nextBoolean()) {
            return 1;
        } else {
            return -1;
        }
    }

    private static class FoundSpawnPossiblity {
        private final EntityRef entranceSpawner;
        private final BlockRegionTransform transformation;

        public FoundSpawnPossiblity(EntityRef entranceSpawner, BlockRegionTransform transformation) {
            this.entranceSpawner = entranceSpawner;
            this.transformation = transformation;
        }

        public EntityRef getEntranceSpawner() {
            return entranceSpawner;
        }

        public BlockRegionTransform getTransformation() {
            return transformation;
        }
    }
}
