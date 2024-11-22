![[Pasted image 20241122164549.png]]
Collision between the player and blocks needs to be clipped when the player should be teleport
The way this should be done, is essentially via clipping from a plane projected with normal facing to the center of the portal, created from the vertices of the player bounding box to the edge of the portal
This plane should also be cut at the portal quad

The reason this needs to happen, is so the player doesn't collide with blocks behind the portal, as that would be dumb

On the target side, a similar thing should happen; from the destination position of the player
Collision should also occur against blocks that are clipped by the portal quad on the target side, so that if there's say, a grass block, which the portal is in, the player doesn't clip into the grass block