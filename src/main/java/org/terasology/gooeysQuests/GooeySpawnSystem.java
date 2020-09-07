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

import org.terasology.behaviors.components.NPCMovementComponent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gooeysQuests.api.CreateStartQuestsEvent;
import org.terasology.gooeysQuests.api.GooeysQuestComponent;
import org.terasology.gooeysQuests.api.PersonalQuestsComponent;
import org.terasology.gooeysQuests.api.PrepareQuestEvent;
import org.terasology.gooeysQuests.api.QuestReadyEvent;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.Direction;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.interfaces.BlockPredicateProvider;
import org.terasology.structureTemplates.interfaces.BlockRegionChecker;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Responsible for spawning gooey.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class GooeySpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    /**
     * Spawn location of gooey is infront of the player. This angle specifies the maximum angle from the players
     * current view angle gooey may spawn. A value of PI would mean that the gooey could spawn behind the player.
     * However if gooey spawns behind the player the player may overlook him. The angle should however also be not
     * to small to make it appear more random and not scripted.
     */
    private static final float MAX_GOOEY_SPAWN_OFFSET_ANGLE = (float) (Math.PI / 8.0f);
    private static final float MIN_GOOEY_SPAWN_DISTANCE = 3;
    private static final float MAX_GOOEY_SPAWN_DISTANCE = 5;
    private static final float SECONDS_BETWEEN_QUESTS = 60;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockRegionChecker blockRegionChecker;

    @In
    private BlockPredicateProvider blockPredicateProvider;

    private Random random = new Random();

    private EntityRef questToSpawnGooeyFor = EntityRef.NULL;

    private float nextQuestCooldown;
    private Predicate<Block> airLikeCondition;
    private Predicate<Block> groundLikeCondition;

    @Override
    public void initialise() {
        nextQuestCooldown = 3;
    }

    @Override
    public void postBegin() {
        this.airLikeCondition = blockPredicateProvider.getBlockPredicate("StructureTemplates:IsAirLike");
        this.groundLikeCondition = blockPredicateProvider.getBlockPredicate("StructureTemplates:IsGroundLike");
    }

    @Override
    public void update(float delta) {
        nextQuestCooldown -= delta;
        if (nextQuestCooldown > 0) {
            if (questToSpawnGooeyFor.isActive()) {
                tryToSpawnGooey();
            }
            return;
        }

        nextQuestCooldown = 0;
        questToSpawnGooeyFor = EntityRef.NULL;

        Iterable<EntityRef> personalQuestOwners = entityManager.getEntitiesWith(PersonalQuestsComponent.class);
        for (EntityRef questOwner : personalQuestOwners) {
            PersonalQuestsComponent questsComponent = questOwner.getComponent(PersonalQuestsComponent.class);
            List<EntityRef> questsInPreperation = questsComponent.questsInPreperation;
            if (questsInPreperation.size() > 0) {
                int randomIndex = random.nextInt(questsInPreperation.size());
                EntityRef questToPrepare = questsInPreperation.get(randomIndex);
                questToPrepare.send(new PrepareQuestEvent());
            }
        }
    }

    @ReceiveEvent
    public void onQuestReady(QuestReadyEvent event, EntityRef quest) {
        this.questToSpawnGooeyFor = quest;
        nextQuestCooldown = SECONDS_BETWEEN_QUESTS;
        tryToSpawnGooey();
    }

    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef character) {
        PersonalQuestsComponent questsComponent = character.getComponent(PersonalQuestsComponent.class);
        if (questsComponent == null) {
            questsComponent = new PersonalQuestsComponent();
            character.addOrSaveComponent(questsComponent);
            character.send(new CreateStartQuestsEvent());
        }
    }

    private void tryToSpawnGooey() {
        int gooeyCount = entityManager.getCountOfEntitiesWith(GooeyComponent.class);
        if (gooeyCount > 0) {
            // TODO teleport gooey instead
            for (EntityRef existinGooey : entityManager.getEntitiesWith(GooeyComponent.class)) {
                existinGooey.destroy();
            }
        }
        EntityRef character = questToSpawnGooeyFor.getOwner();

        if (character == null || !character.isActive()) {
            return;
        }
        LocationComponent characterLocation = character.getComponent(LocationComponent.class);
        if (characterLocation == null) {
            return;
        }
        Vector3f spawnPos = tryFindingGooeySpawnLocationInfrontOfCharacter(character);
        if (spawnPos == null) {
            return;
        }

        Vector3f spawnPosToCharacter = characterLocation.getWorldPosition().sub(spawnPos);
        Quat4f rotation = distanceDeltaToYAxisRotation(spawnPosToCharacter);
        Prefab gooeyPrefab = assetManager.getAsset("GooeysQuests:gooey", Prefab.class).get();

        EntityBuilder entityBuilder = entityManager.newBuilder(gooeyPrefab);
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        locationComponent.setWorldPosition(spawnPos);
        locationComponent.setWorldRotation(rotation);

        NPCMovementComponent movementComponent = entityBuilder.getComponent(NPCMovementComponent.class);

        float yaw = (float) Math.atan2(spawnPosToCharacter.x, spawnPosToCharacter.z);
        movementComponent.yaw = 180f + yaw * TeraMath.RAD_TO_DEG;
        entityBuilder.addOrSaveComponent(movementComponent);

        GooeyComponent gooeyComponent = entityBuilder.getComponent(GooeyComponent.class);
        gooeyComponent.offeredQuest = questToSpawnGooeyFor;
        entityBuilder.addOrSaveComponent(gooeyComponent);

        EntityRef gooey = entityBuilder.build();
        GooeysQuestComponent gooeysQuestComponent = questToSpawnGooeyFor.getComponent(GooeysQuestComponent.class);
        if (gooeysQuestComponent == null) {
            gooeysQuestComponent = new GooeysQuestComponent();
        }
        character.getOwner().send(new ChatMessageEvent(gooeysQuestComponent.greetingText, gooey));

        spawnTeleportParticles(spawnPos);
        questToSpawnGooeyFor = EntityRef.NULL;

    }

    private void spawnTeleportParticles(Vector3f spawnPos) {
        Prefab prefab = assetManager.getAsset("GooeysQuests:teleportParticleEffect", Prefab.class).get();
        EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        locationComponent.setWorldPosition(spawnPos);
        entityBuilder.build();
    }

    private Quat4f distanceDeltaToYAxisRotation(Vector3f direction) {
        direction.y = 0;
        if (direction.lengthSquared() > 0.001f) {
            direction.normalize();
        } else {
            direction.set(Direction.FORWARD.getVector3f());
        }
        return Quat4f.shortestArcQuat(Direction.FORWARD.getVector3f(), direction);
    }

    private Vector3f tryFindingGooeySpawnLocationInfrontOfCharacter(EntityRef character) {
        LocationComponent characterLocation = character.getComponent(LocationComponent.class);
        Vector3f spawnPos = locationInfrontOf(characterLocation, MIN_GOOEY_SPAWN_DISTANCE, MAX_GOOEY_SPAWN_DISTANCE,
                MAX_GOOEY_SPAWN_OFFSET_ANGLE);

        Vector3i spawnBlockPos = new Vector3i(spawnPos);

        if (!isValidGooeySpawnPosition(spawnBlockPos)) {
            return null;
        }

        Vector3i characterPos = new Vector3i(characterLocation.getWorldPosition());

        boolean lineOfSight = hasLineOfSight(spawnBlockPos, characterPos);
        if (!lineOfSight) {
            return null;
        }

        return spawnPos;
    }

    /**
     * Check if spawn location can be seen by the character.Abuses other method for a (too pessimistic) line of sight
     * check. (Feel free to implmeent a proper line of sight check)
     */
    private boolean hasLineOfSight(Vector3i spawnBlockPos, Vector3i characterPos) {
        Region3i region = Region3i.createBounded(spawnBlockPos, characterPos);
        return blockRegionChecker.allBlocksMatch(region, BlockRegionTransform.getTransformationThatDoesNothing(),
                airLikeCondition);
    }

    private Vector3f locationInfrontOf(LocationComponent location, float minDistance, float maxDistance,
                                       float maxAngle) {
        Vector3f result = location.getWorldPosition();
        Vector3f offset = new Vector3f(location.getWorldDirection());
        Quat4f randomRot = randomYAxisRotation(maxAngle);
        offset = randomRot.rotate(offset);
        float distanceRangeDelta = maxDistance - minDistance;
        float randomDistance = minDistance + random.nextFloat() * distanceRangeDelta;
        offset.scale(randomDistance);
        result.add(offset);
        return result;
    }

    private Quat4f randomYAxisRotation(float maxAngle) {
        float randomAngle = random.nextFloat() * maxAngle;
        // chance to have a rotation in other diration:
        if (random.nextBoolean()) {
            randomAngle = ((float) Math.PI * 2) - randomAngle;
        }
        return new Quat4f(new Vector3f(0, 1, 0), randomAngle);
    }

    private boolean isValidGooeySpawnPosition(Vector3i spawnPosition) {
        int minX = spawnPosition.getX() - 1;
        int maxX = spawnPosition.getX() + 1;
        int minZ = spawnPosition.getZ() - 1;
        int maxZ = spawnPosition.getZ() + 1;
        int groundY = spawnPosition.getY() - 2;

        Region3i groundRegion = Region3i.createFromMinMax(new Vector3i(minX, groundY, minZ), new Vector3i(maxX, groundY,
                maxZ));
        boolean groundExists = blockRegionChecker.allBlocksMatch(groundRegion, BlockRegionTransform.getTransformationThatDoesNothing(),
                groundLikeCondition);
        if (!groundExists) {
            return false;
        }
        int airMin = spawnPosition.getY();
        // require some air above, to prevent it spawning below something
        int airMax = airMin + 3;
        Region3i airRegion = Region3i.createFromMinMax(new Vector3i(minX, airMin, minZ), new Vector3i(maxZ, airMax,
                maxZ));
        boolean enoughAirAbove = blockRegionChecker.allBlocksMatch(airRegion, BlockRegionTransform.getTransformationThatDoesNothing(),
                airLikeCondition);
        if (!enoughAirAbove) {
            return false;
        }
        return true;
    }

}
