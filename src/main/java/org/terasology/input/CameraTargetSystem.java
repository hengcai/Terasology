/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.input;

import com.google.common.base.Objects;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector3i;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;

import javax.vecmath.Vector3f;
import java.util.Arrays;

/**
 * @author Immortius
 */
public class CameraTargetSystem implements ComponentSystem {

    // TODO: This should come from somewhere, probably player entity?
    public static final float TARGET_DISTANCE = 5f;

    @In
    private LocalPlayer localPlayer;
    @In
    private BlockEntityRegistry blockRegistry;

    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPos = null;
    private Vector3f hitPosition = new Vector3f();
    private Vector3f hitNormal = new Vector3f();
    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    public boolean isTargetAvailable() {
        return target.exists() || targetBlockPos != null;
    }

    public EntityRef getTarget() {
        if (!target.exists() && targetBlockPos != null) {
            target = blockRegistry.getOrCreateEntityAt(targetBlockPos);
        }
        return target;
    }

    public Vector3f getHitPosition() {
        return new Vector3f(hitPosition);
    }

    public Vector3f getHitNormal() {
        return new Vector3f(hitNormal);

    }

    public void setFilter(CollisionGroup... filter) {
        this.filter = Arrays.copyOf(filter, filter.length);
    }

    public void update() {
        // Repair lost target
        // TODO: Improvements to temporary chunk handling will remove the need for this
        if (!target.exists() && targetBlockPos != null) {
            target = blockRegistry.getOrCreateEntityAt(targetBlockPos);
        }
        boolean lostTarget = false;
        if (!target.exists()) {
            targetBlockPos = null;
            lostTarget = true;
        }

        // TODO: This will change when camera are handled better (via a component)
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

        BulletPhysics physicsRenderer = CoreRegistry.get(BulletPhysics.class);
        HitResult hitInfo = physicsRenderer.rayTrace(new Vector3f(camera.getPosition()), new Vector3f(camera.getViewingDirection()), TARGET_DISTANCE, filter);
        Vector3i newBlockPos = null;

        EntityRef newTarget = EntityRef.NULL;
        if (hitInfo.isHit()) {
            newTarget = hitInfo.getEntity();
            hitPosition = hitInfo.getHitPoint();
            hitNormal = hitInfo.getHitNormal();
            if (hitInfo.isWorldHit()) {
                newBlockPos = new Vector3i(hitInfo.getBlockPosition());
            }
        }
        if (!Objects.equal(target, newTarget) || lostTarget) {
            EntityRef oldTarget = target;
            oldTarget.send(new CameraOutEvent());
            newTarget.send(new CameraOverEvent());
            localPlayer.getCharacterEntity().send(new CameraTargetChangedEvent(oldTarget, newTarget));
        }
        target = newTarget;
        targetBlockPos = newBlockPos;
    }

    public String toString() {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (targetBlockPos != null) {
            return String.format("From: %f %f %f, Dir: %f %f %f, Hit %d %d %d %f %f %f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z, camera.getViewingDirection().x, camera.getViewingDirection().y, camera.getViewingDirection().z, targetBlockPos.x, targetBlockPos.y, targetBlockPos.z, hitPosition.x, hitPosition.y, hitPosition.z);
        }
        return "";
    }

    public Vector3i getTargetBlockPosition() {
        if (targetBlockPos != null) {
            return new Vector3i(targetBlockPos);
        }
        return new Vector3i(hitPosition, 0.5f);
    }
}
