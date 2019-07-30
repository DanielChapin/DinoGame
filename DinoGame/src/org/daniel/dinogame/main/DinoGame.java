package org.daniel.dinogame.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import org.daniel.dinogame.gameobjects.DGObstacle;
import org.daniel.dinogame.gameobjects.DGPlayer;

public class DinoGame extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	private Canvas canvas = new Canvas();
	
	private static final double FPS = 60.0d;
	
	private static final Color BACKGROUND_COLOR = new Color(0xEFEFEF);
	private static final Color PRIMARY_COLOR = new Color(0xA0A0A0);
	
	private static final Font SCORE_FONT = new Font(Font.MONOSPACED, Font.BOLD, 30);
	private static final Font START_SCREEN_PRIMARY_FONT = new Font(Font.MONOSPACED, Font.BOLD, 60);
	private static final Font START_SCREEN_SECONDARY_FONT = new Font(Font.MONOSPACED, Font.BOLD, 35);
	
	private static final String START_SCREEN_PRIMARY_MESSAGE = "[SPACE], [UP], or [DOWN] to START";
	private static final String START_SCREEN_SECONDARY_MESSAGE = "By: DANIEL CHAPIN";
	
	private static final int MIN_OBSTACLE_WAIT_TIME = 50;
	private static final int MAX_OBSTACLE_WAIT_TIME = 80;
	
	private static final float FLOOR_HEIGHT = 0.6f;
	private static final float DINO_HEIGHT = 0.1f;
	private static final float DINO_X_POSITION = 0.1f;
	private int maxHeight;
	private int dinoHeight;
	
	private static final float[] OBSTACLE_HEIGHTS = {0.75f, 0.0f};
	private static final float[] OBSTACLE_DIMENSIONS = {
			1.1f, 1.2f,
			0.5f, 1.0f,
	};
	private int[] obstacleHeights;
	private Dimension[] obstacleDimensions;
	
	private int nextObstacleType;
	
	private int ticks = 0;
	private int ticksUntilObstacle = MIN_OBSTACLE_WAIT_TIME;
	
	private static final int DEFAULT_SPEED = 20;
	private int speedModifier = 0;
	
	private float hue = 0.0f;
	
	private static final int[] CHEAT_CODE = {
			38, 37, 40, 
			37, 40, 
			40, 37, 39,
			37, 40,
			37,
			40, 37, 38,
	};
	private int[] cheatInput;
	private boolean cheatActivated = false;
	
	private boolean running = false;
	
	private DGPlayer player;
	private DGObstacle[] obstacles = new DGObstacle[0];

	public static void main(String[] args) {
		new DinoGame();
	}
	
	private DinoGame() {
		Thread gameThread = new Thread(this, "Dino Game");
		gameThread.start();
	}

	@Override
	public void run() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setExtendedState(MAXIMIZED_BOTH);
		setUndecorated(true);
		
		add(canvas);
		
		setVisible(true);
		
		canvas.createBufferStrategy(3);
		
		maxHeight = (int) (FLOOR_HEIGHT * getHeight());
		dinoHeight = (int) (DINO_HEIGHT * getHeight());
		
		obstacleHeights = new int[OBSTACLE_HEIGHTS.length];
		obstacleDimensions = new Dimension[OBSTACLE_HEIGHTS.length];
		for (int i = 0; i < OBSTACLE_HEIGHTS.length; i++) {
			obstacleHeights[i] = (int) (maxHeight - dinoHeight * OBSTACLE_HEIGHTS[i]);
			obstacleDimensions[i] = new Dimension(
				(int) (dinoHeight * OBSTACLE_DIMENSIONS[i * 2]),
				(int) (dinoHeight * OBSTACLE_DIMENSIONS[i * 2 + 1])
			);
		}
		nextObstacleType = (int) (2 * Math.random());
		
		cheatInput = new int[CHEAT_CODE.length];
		
		player = new DGPlayer(new Point((int) (DINO_X_POSITION * getWidth()), maxHeight), dinoHeight);
		
		addPlayerInput();
		
		startGameLoop();
	}
	
	private void startGameLoop() {
		long now = System.nanoTime(), lastLoop = now;
		double loopTime = 1000000000.0 / FPS;
		while (true) {
			if ((now = System.nanoTime()) - lastLoop >= loopTime) {
				update();
				render();
				lastLoop = now;
			}
		}
	}
	
	private void addPlayerInput() {
		addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_UP:
					if (!running) {
						speedModifier = 0;
						running = true;
					} else if (player.canJump()) {
						player.isJumping = true;
					}
					break;
				case KeyEvent.VK_DOWN:
					if (!running) {
						speedModifier = 0;
						running = true;
					} else {
						player.isFalling = true;
						player.isJumping = false;
					}
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int[] temp = cheatInput;
				for (int i = 1; i < cheatInput.length; i++) {
					cheatInput[i - 1] = temp[i];
				}
				cheatInput[cheatInput.length - 1] = e.getKeyCode();
				boolean cheatCodeCorrect = false;
				for (int i = 0; i < cheatInput.length; i++) {
					if (cheatInput[i] == CHEAT_CODE[i]) {
						cheatCodeCorrect = true;
					} else {
						cheatCodeCorrect = false;
						break;
					}
				}
				if (cheatCodeCorrect) {
					cheatActivated = !cheatActivated;
				}
				switch (e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_UP:
					break;
				case KeyEvent.VK_DOWN:
					player.isFalling = false;
					break;
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
		});
	}
	
	private void update() {
		if (running) {
			if (obstacles.length != 0 && obstacles[0].position.x <= -1 * obstacles[0].hitBox.width) {
				removeObstacle();
				speedModifier++;
			}
			if (++ticks == ticksUntilObstacle) {
				ticks = 0;
				ticksUntilObstacle = MIN_OBSTACLE_WAIT_TIME + (int) ((double) (MAX_OBSTACLE_WAIT_TIME - MIN_OBSTACLE_WAIT_TIME) * Math.random());
				addObstacle(nextObstacleType);
				nextObstacleType = (int) (2 * Math.random());
			}
			for (DGObstacle obstacle : obstacles) {
				obstacle.update();
				if (player.doesOverlap(obstacle.getAsRectangle())) {
					running = false;
					obstacles = new DGObstacle[0];
					player = new DGPlayer(new Point((int) (DINO_X_POSITION * getWidth()), maxHeight), dinoHeight);
				}
			}
			player.update();
		}
		hue += 0.01f;
	}
	
	private void render() {
		BufferStrategy buffer = canvas.getBufferStrategy();
		Graphics graphics = buffer.getDrawGraphics();
		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillRect(0, 0, getWidth(), getHeight());
		if (running) {
			graphics.setColor((!cheatActivated) ? PRIMARY_COLOR : Color.getHSBColor(hue, 1.0f, 0.5f));
			if (cheatActivated && running) {
				graphics.setFont(START_SCREEN_PRIMARY_FONT);
				String cheat = ((nextObstacleType == 0) ? "Floating" : "Standing") + ": " + Integer.toString((int) ((float) ticks / ticksUntilObstacle * 100)) + "%";
				graphics.drawString(
						cheat,
						(int) (getWidth() / 2 - graphics.getFontMetrics().getStringBounds(cheat, graphics).getWidth() / 2),
						getHeight() / 2 - (2 * graphics.getFontMetrics().getHeight()) - 10
				);
			}
			graphics.drawLine(0, maxHeight, getWidth(), maxHeight);
			player.render(graphics);
			for (DGObstacle obstacle : obstacles) {
				obstacle.render(graphics);
			}
			graphics.setFont(SCORE_FONT);
			graphics.drawString("Score: " + Integer.toString(speedModifier), 10, 10 + graphics.getFontMetrics().getHeight() / 2);
		} else {
			graphics.setColor(Color.getHSBColor(hue, 1f, 1f));
			graphics.setFont(START_SCREEN_PRIMARY_FONT);
			graphics.drawString(
				START_SCREEN_PRIMARY_MESSAGE,
				(int) (getWidth() / 2 - graphics.getFontMetrics().getStringBounds(START_SCREEN_PRIMARY_MESSAGE, graphics).getWidth() / 2),
				getHeight() / 2 - graphics.getFontMetrics().getHeight() - 10
			);
			if (speedModifier != 0) {
				String scoreText = "Score: " + Integer.toString(speedModifier);
				graphics.drawString(
						scoreText,
						(int) (getWidth() / 2 - graphics.getFontMetrics().getStringBounds(scoreText, graphics).getWidth() / 2),
						getHeight() / 2 - (2 * graphics.getFontMetrics().getHeight()) - 10
				);
			}
			graphics.setFont(START_SCREEN_SECONDARY_FONT);
			graphics.drawString(
				START_SCREEN_SECONDARY_MESSAGE,
				(int) (getWidth() / 2 - graphics.getFontMetrics().getStringBounds(START_SCREEN_SECONDARY_MESSAGE, graphics).getWidth() / 2),
				getHeight() / 2 + graphics.getFontMetrics().getHeight() + 10
			);
		}
		graphics.dispose();
		buffer.show();
	}
	
	private void addObstacle(int obstacleType) {
		DGObstacle[] temp = obstacles;
		obstacles = new DGObstacle[obstacles.length + 1];
		for (int i = 0; i < temp.length; i++) {
			obstacles[i] = temp[i];
		}
		
		obstacles[obstacles.length - 1] = new DGObstacle(
			new Point(getWidth(), obstacleHeights[obstacleType]),
			obstacleDimensions[obstacleType],
			DEFAULT_SPEED + speedModifier
		);
	}
	
	private void removeObstacle() {
		DGObstacle[] temp = obstacles;
		obstacles = new DGObstacle[obstacles.length - 1];
		for (int i = 1; i < temp.length; i++) {
			obstacles[i - 1] = temp[i];
		}
	}
	
}
