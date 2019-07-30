package org.daniel.dinogame.gameobjects;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class DGObstacle {
	
	public Point position;
	public Dimension hitBox;
	
	private int speed;
	
	public DGObstacle(Point position, Dimension hitBox, int speed) {
		this.position = position;
		this.hitBox = hitBox;
		this.speed = speed;
	}
	
	public Rectangle getAsRectangle() {
		return new Rectangle(position.x, position.y - hitBox.height, hitBox.width, hitBox.height);
	}
	
	public void update() {
		position.x -= speed;
	}
	
	public void render(Graphics g) {
		g.fillRect(position.x, position.y - hitBox.height, hitBox.width, hitBox.height);
	}

}
