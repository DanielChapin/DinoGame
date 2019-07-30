package org.daniel.dinogame.gameobjects;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class DGPlayer {
	
	public Point position;
	public Dimension hitBox;
	
	private Dimension standingHitBox, crouchingHitBox;
	
	private int floorHeight;
	
	public boolean isJumping = false, isFalling = false;
	
	private static final int JUMP_SPEED = 52;
	private static final float GRAVITY = JUMP_SPEED / 2;
	
	private static final float MAX_JUMP_HEIGHT = 2.2f;
	private int maxJumpHeight;
	
	private static final int STALL_UPDATES = 10;
	private int stallUpdates;
	
	public DGPlayer(Point position, int height) {
		this.position = position;
		standingHitBox = new Dimension(height / 2, height);
		crouchingHitBox = new Dimension(height, height / 2);
		hitBox = standingHitBox;
		floorHeight = position.y;
		maxJumpHeight = floorHeight - (int) (height * MAX_JUMP_HEIGHT);
	}
	
	public boolean canJump() {
		return position.y == floorHeight && !isFalling;
	}
	
	public boolean doesOverlap(Rectangle rect) {
		return (new Rectangle(position.x, position.y - hitBox.height, hitBox.width, hitBox.height)).intersects(rect);
	}
	
	public void update() {
		if (isJumping && position.y >= maxJumpHeight) {
			if (position.y - JUMP_SPEED <= maxJumpHeight) {
				isJumping = false;
				stallUpdates = STALL_UPDATES;
			} else {
				position.y -= JUMP_SPEED;
			}
		} else if (isFalling) {
			stallUpdates = 0;
			if (position.y != floorHeight) {
				if ((position.y - floorHeight) % JUMP_SPEED != 0) {
					position.y += GRAVITY;
				}
				position.y += JUMP_SPEED;
			} else {
				hitBox = crouchingHitBox;
			}
		} else {
			if (position.y < floorHeight && !isJumping && stallUpdates == 0) {
				position.y += GRAVITY;
			}
			if (stallUpdates != 0) {
				stallUpdates--;
			}
			hitBox = standingHitBox;
		}
	}
	
	public void render(Graphics g) {
		g.fillRect(position.x, position.y - hitBox.height, hitBox.width, hitBox.height);
	}

}
