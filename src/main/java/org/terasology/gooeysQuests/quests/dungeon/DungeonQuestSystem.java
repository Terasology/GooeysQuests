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
import org.terasology.gooeysQuests.api.CreateStartQuestsEvent;
import org.terasology.gooeysQuests.api.PersonalQuestsComponent;
import org.terasology.gooeysQuests.api.PrepareQuestEvent;
import org.terasology.gooeysQuests.api.QuestReadyEvent;
import org.terasology.gooeysQuests.api.QuestStartRequest;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.events.CheckSpawnConditionEvent;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.interfaces.BlockPredicateProvider;
import org.terasology.structureTemplates.util.transform.BlockRegionMovement;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.structureTemplates.util.transform.BlockRegionTransformationList;
import org.terasology.structureTemplates.util.transform.HorizontalBlockRegionRotation;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Makes gooey offer a dungeon quest
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DungeonQuestSystem extends BaseComponentSystem {

    private static final int MAX_HORIZONTAL_DISTANCE = 20;
    private static final int VERTICAL_SCAN_DISTANCE = 5;
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

    private Map<EntityRef, BlockRegionTransform> questToFoundSpawnTransformationMap = new HashMap<>();

    private Random random = new Random();

    private Prefab spawnDungeonParticlePrefab;
    private EntityRef entranceSpawner;
    private EntityRef CorridorSpawner;
    private Predicate<Block> isAirCondition;
    private Predicate<Block> isGroundCondition;



    @Override
    public void initialise() {
        spawnDungeonParticlePrefab = assetManager.getAsset("GooeysQuests:teleportParticleEffect", Prefab.class).get();

    }


    @Override
    public void postBegin() {
        isAirCondition = blockPredicateProvider.getBlockPredicate("StructureTemplates:IsAirLike");
        isGroundCondition = blockPredicateProvider.getBlockPredicate("StructureTemplates:IsGroundLike");
        entranceSpawner = createNonPersistentEntityFromPrefab("GooeysQuests:dungeonEntranceSpawner");
    }

    private EntityRef createNonPersistentEntityFromPrefab(String prefabUrn) {
        Prefab prefab = assetManager.getAsset(prefabUrn, Prefab.class)
                .get();
        EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
        entityBuilder.setPersistent(false);
        return entityBuilder.build();

    }

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

        BlockRegionTransform foundSpawnTransformation =  findGoodSpawnTransformation(surfaceGroundBlockPosition);
        if (foundSpawnTransformation == null) {
            return;
        }


        questToFoundSpawnTransformationMap.put(quest, foundSpawnTransformation);
        quest.send(new QuestReadyEvent());
    }


    private BlockRegionTransform findGoodSpawnTransformation(Vector3i spawnPosition) {
        for (Side side: Side.horizontalSides()) {
            BlockRegionTransformationList transformList = createTransformation(spawnPosition, side);

            CheckSpawnConditionEvent checkConditionEvent = new CheckSpawnConditionEvent(transformList);
            entranceSpawner.send(checkConditionEvent);
            if (!checkConditionEvent.isPreventSpawn()) {
                return transformList;
            }
        }

        return null;
    }

    private BlockRegionTransformationList createTransformation(Vector3i spawnPosition, Side side) {
        BlockRegionTransformationList transformList = new BlockRegionTransformationList();
        transformList.addTransformation(
                HorizontalBlockRegionRotation.createRotationFromSideToSide(Side.FRONT, side));
        transformList.addTransformation(new BlockRegionMovement(spawnPosition));
        return transformList;
    }

    @ReceiveEvent(components = DungeonQuestComponent.class)
    public void onQuestStart(QuestStartRequest event, EntityRef quest) {
        BlockRegionTransform spawnTransformation = questToFoundSpawnTransformationMap.get(quest);
        if (spawnTransformation == null) {
            return; // TODO report failure to client and gooey system
        }

        Region3i entranceDoorRegion = getEntranceDoorRegion(spawnTransformation);
        entranceSpawner.send(new SpawnStructureEvent(spawnTransformation));
        spawnMagicalBuildParticles(entranceDoorRegion);


        if (false) {
            Vector3i CorridorSpawnPosition = new Vector3i(0, 0, 3);
            CorridorSpawnPosition = spawnTransformation.transformVector3i(CorridorSpawnPosition);

            Side CorridorRotation = spawnTransformation.transformSide(Side.FRONT);
            BlockRegionTransform CorridorSpawnTransformation = createTransformation(CorridorSpawnPosition, CorridorRotation);
            CorridorSpawner.send(new SpawnStructureEvent(CorridorSpawnTransformation));
        }
    }

    Region3i wallRegionBelow(Region3i region) {
        Vector3i min= new Vector3i(region.minX(), region.minY() - 1, region.minZ());
        Vector3i max= new Vector3i(region.maxX(), region.minY() - 1, region.maxZ());
        return Region3i.createFromMinMax(min,max);
    }

    Region3i wallRegionAbove(Region3i region) {
        Vector3i min= new Vector3i(region.minX(), region.maxY() + 1, region.minZ());
        Vector3i max= new Vector3i(region.maxX(), region.maxY() + 1, region.maxZ());
        return Region3i.createFromMinMax(min,max);
    }

    Region3i wallRegionLeft(Region3i region) {
        Vector3i min= new Vector3i(region.minX() - 1 , region.minY(), region.minZ());
        Vector3i max= new Vector3i(region.minX() - 1, region.maxY(), region.maxZ());
        return Region3i.createFromMinMax(min,max);
    }

    Region3i wallRegionRight(Region3i region) {
        Vector3i min= new Vector3i(region.maxX() + 1, region.minY(), region.minZ());
        Vector3i max= new Vector3i(region.maxX() + 1, region.maxY(), region.maxZ());
        return Region3i.createFromMinMax(min,max);
    }

    Region3i wallRegionFront(Region3i region) {
        Vector3i min= new Vector3i(region.minX(), region.minY(), region.minZ() - 1);
        Vector3i max= new Vector3i(region.maxX(), region.maxY(), region.minZ() - 1);
        return Region3i.createFromMinMax(min,max);
    }

    Region3i wallRegionBack(Region3i region) {
        Vector3i min= new Vector3i(region.minX(), region.minY(), region.maxZ() + 1);
        Vector3i max= new Vector3i(region.maxX(), region.maxY(), region.maxZ() + 1);
        return Region3i.createFromMinMax(min,max);
    }

    List<Region3i> outerWallRegionsOf(Region3i region) {
        List<Region3i> regions = new ArrayList<>();
        regions.add(wallRegionBelow(region));
        regions.add(wallRegionAbove(region));
        regions.add(wallRegionLeft(region));
        regions.add(wallRegionRight(region));
        regions.add(wallRegionFront(region));
        regions.add(wallRegionBack(region));
        return regions;
    }

    private void spawnRegion(Region3i region, List<String> blockURIList) {
        int index = 0;
        Block dirtBlock = blockManager.getBlock("engine:Dirt");
        Block grassBlock = blockManager.getBlock("engine:Grass");
        for (Vector3i pos : region) {
            String blockUri = blockURIList.get(index);
            if (blockUri != null) {
                Block block = blockManager.getBlock(blockUri);
                worldProvider.setBlock(pos, block);
            } else {
                Block existingBlock = worldProvider.getBlock(pos);
                if (existingBlock == dirtBlock) {
                    worldProvider.setBlock(pos, grassBlock);
                }
            }
            index++;
        }
    }


    /**
     * Free the memory once the quest is no longer loaded
     */
    @ReceiveEvent
    public void onDeactivateQuestEntity(BeforeDeactivateComponent event, EntityRef questEntity,
                                        PersonalQuestsComponent questsComponent) {
        questToFoundSpawnTransformationMap.remove(questEntity);
    }


    private Region3i getEntranceDoorRegion(BlockRegionTransform transformation) {
        Vector3i min = new Vector3i(-1, 0, 0);
        Vector3i max = new Vector3i(1, 3, 2);
        Region3i region = Region3i.createFromMinMax(min, max);
        return transformation.transformRegion(region);
    }

    private Region3i getCorridorInnerRegion(Vector3i origin) {
        int minX = origin.getX() - 1;
        int maxX = origin.getX() + 1;
        int minY = origin.getY() - 3;
        int maxY = origin.getY() + 3;
        int minZ = origin.getZ() + 3;
        int maxZ = origin.getZ() + 18;

        Vector3i min = new Vector3i(minX, minY, minZ);
        Vector3i max = new Vector3i(maxX, maxY, maxZ);
        return Region3i.createFromMinMax(min, max);
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


    private void spawnMagicalBuildParticles(Region3i region) {

        EntityBuilder entityBuilder = entityManager.newBuilder(spawnDungeonParticlePrefab);
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        locationComponent.setWorldPosition(region.center());
        BlockParticleEffectComponent particleEffect = entityBuilder.getComponent(BlockParticleEffectComponent.class);
        Vector3f size = new Vector3f(region.size().toVector3f());
        size.scale(0.5f);
        particleEffect.spawnRange = size;
        entityBuilder.build();
    }
}