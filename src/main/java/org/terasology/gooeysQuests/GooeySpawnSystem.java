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

import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

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

    private static final Predicate<Block> BLOCK_IS_AIR_LIKE = block -> block.isPenetrable() && !block.isLiquid();
    private static final Predicate<Block> BLOCK_IS_GROUND_LIKE = block -> !block.isPenetrable();

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private WorldProvider worldProvider;

    private Random random = new Random();

    private EntityRef charactertoSpawnGooeyAt;

    @Override
    public void initialise() {

    }

    @Override
    public void update(float delta) {
        tryToSpawnGooey();
    }


    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef character) {
        charactertoSpawnGooeyAt= character;
    }

    private void tryToSpawnGooey() {
        int gooeyCount = entityManager.getCountOfEntitiesWith(GooeyComponent.class);
        if (gooeyCount > 0) {
            return;
        }
        if (charactertoSpawnGooeyAt == null || !charactertoSpawnGooeyAt.isActive()){
            return;
        }
        LocationComponent characterLocation = charactertoSpawnGooeyAt.getComponent(LocationComponent.class);
        if (characterLocation == null) {
            return;
        }
        Vector3f spawnPos = tryFindingGooeySpawnLocationInfrontOfCharacter(charactertoSpawnGooeyAt);
        if (spawnPos == null) {
            return;
        }

        Vector3f spawnPosToCharacter = characterLocation.getWorldPosition().sub(spawnPos);
        Quat4f rotation = distanceDeltaToYAxisRotation(spawnPosToCharacter);
        Prefab gooeyPrefab = assetManager.getAsset("GooeysQuests:gooey", Prefab.class).get();

        EntityRef entity = entityManager.create(gooeyPrefab, spawnPos, rotation);
        NPCMovementComponent movementComponent = entity.getComponent(NPCMovementComponent.class);

        float yaw = (float) Math.atan2(spawnPosToCharacter.x, spawnPosToCharacter.z);
        movementComponent.yaw = 180f +  yaw * TeraMath.RAD_TO_DEG;
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
        Vector3i min = new Vector3i(spawnBlockPos);
        min.min(characterPos);
        Vector3i max = new Vector3i(spawnBlockPos);
        max.max(characterPos);
        return allBlocksInAABBMatch(min.x, max.x, min.y, max.y, min.z, max.z, BLOCK_IS_AIR_LIKE);
    }

    private Vector3f locationInfrontOf(LocationComponent location, float minDistance, float maxDistance,
                                       float maxAngle) {
        Vector3f result = location.getWorldPosition();
        Vector3f offset = new Vector3f(location.getWorldDirection());
        Quat4f randomRot =randomYAxisRotation(maxAngle);
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
        return new Quat4f(new Vector3f(0,1,0), randomAngle);
    }

    private boolean isValidGooeySpawnPosition(Vector3i spawnPosition) {
        int minX = spawnPosition.getX() - 1;
        int maxX = spawnPosition.getX() + 1;
        int minZ = spawnPosition.getZ() - 1;
        int maxZ = spawnPosition.getZ() + 1;
        int groundY = spawnPosition.getY() - 2;
        boolean groundExists = allBlocksInAABBMatch(minX, maxX, groundY, groundY, minZ, maxZ, BLOCK_IS_GROUND_LIKE);
        if (!groundExists) {
            return false;
        }
        int airMin = spawnPosition.getY();
        // require some air above, to prevent it spawning below something
        int airMax = airMin + 3;
        boolean enoughAirAbove = allBlocksInAABBMatch(minX, maxX, airMin, airMax, minZ, maxZ, BLOCK_IS_AIR_LIKE);
        if (!enoughAirAbove) {
            return false;
        }
        return true;
    }

    private boolean allBlocksInAABBMatch(int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
                                      Predicate<Block> condition) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = worldProvider.getBlock(x ,y, z);
                    if (!condition.test(block)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Command(shortDescription = "Respawns gooey infront of the player", runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String respawnGooey(@Sender EntityRef sender) {
        for (EntityRef existinGooey: entityManager.getEntitiesWith(GooeyComponent.class)) {
            existinGooey.destroy();
        }

        ClientComponent clientComponent = sender.getComponent(ClientComponent.class);

        EntityRef character = clientComponent.character;
        LocationComponent characterLocation = character.getComponent(LocationComponent.class);
        if (characterLocation != null) {
            charactertoSpawnGooeyAt = character;
            return "Trying to respawn gooey";
        } else {
            return "Character has no location";
        }
    }
}
