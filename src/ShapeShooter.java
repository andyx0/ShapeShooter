import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class ShapeShooter extends JPanel implements KeyListener, MouseListener {
	public static final int WIDTH = 525;
	public static final int HEIGHT = 700;

	private int gamemode;
	private int playerX = 400;
	private int playerY = 125;
	private int bulletSpeed;
	private int enemySpawnRate = 3000;
	private int highScore = 0;
	private int currentScore;
	private double enemySpeed = 2;
	private boolean gameRunning = false;
	private Color playerColor;
	private BufferedImage img;
	private Graphics2D g2;
	private Timer menuPlayer;
	private Timer enemySpawner;
	private Timer theGame;
	private Timer score;
	private JButton[] gamemodes = {new JButton("Easy"), new JButton("Normal"), new JButton("Hard")};
	private JButton button = new JButton("Play Again");
	ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();

	public ShapeShooter() {
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D) img.getGraphics();
		g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize() * 2f));
		drawCenteredString("Select a Difficulty", new Rectangle(0, 0, WIDTH, HEIGHT - 420));
		//setLayout(null);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(new Insets(250, 0, 0, 0)));
		for (int i = 0; i < gamemodes.length; i++) {
			int k = i + 1;
			gamemodes[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gamemode = k;
					menuPlayer.stop();
					reset();
				}
			});
			gamemodes[i].setFont(new Font(gamemodes[i].getFont().getName(), Font.PLAIN, gamemodes[i].getFont().getSize() + 15));
			//gamemodes[i].setBounds(190, 200 + 100 * i, 150, 50);
			gamemodes[i].setAlignmentX(Component.CENTER_ALIGNMENT);
			gamemodes[i].setForeground(Color.yellow);
			gamemodes[i].setBackground(Color.blue);
			gamemodes[i].setBorderPainted(false);
			gamemodes[i].setFocusable(false);
			gamemodes[i].setVisible(true);
			gamemodes[i].setOpaque(true);
			add(gamemodes[i]);
			add(Box.createRigidArea(new Dimension(0, 60)));
		}
		button.setFont(new Font(button.getFont().getName(), Font.PLAIN, button.getFont().getSize() + 5));
		menuPlayer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playerColor = getRandomColor();
				g2.setColor(playerColor);
				g2.fill(new Rectangle(playerX, playerY, 40, 40));
				repaint();
			}	
		});
		menuPlayer.start();
		enemySpawner = new Timer(enemySpawnRate, new EnemyGenerator());
		theGame = new Timer(50, new GameAction());
		score = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentScore += gamemode * 5;
			}
		});
	}

	private Color getRandomColor() {
		int R = (int) (Math.random() * 256);
		int G = (int) (Math.random() * 256);
		int B = (int) (Math.random() * 256);
		Color color = new Color(R, G, B);
		color = Color.getHSBColor(new Random().nextFloat(), 1.0f, 1.0f);
		return color;
	}

	private void reset() {
		removeAll();
		currentScore = 0;
		playerX = 250;
		playerY = 350;
		bulletSpeed = 4 * gamemode;
		enemySpawnRate = 3000 / gamemode;
		enemySpeed = 2 * gamemode;
		gameRunning = true;
		enemySpawner.restart();
		theGame.restart();
		score.restart();
	}

	private void drawCenteredString(String text, Rectangle rect) {
		FontMetrics metrics = g2.getFontMetrics(g2.getFont());
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		int y = rect.y + (rect.height - metrics.getHeight()) / 2 + metrics.getAscent();
		g2.drawString(text, x, y);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, null);
	}

	class GameAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			g2.clearRect(0, 0, WIDTH, HEIGHT);
			for (int i = 0; i < bullets.size(); i++) {
				g2.setColor(bullets.get(i).bulletColor);
				bullets.get(i).move();
				if (bullets.get(i).x < 0 || bullets.get(i).x > getWidth() || bullets.get(i).y < 0 || bullets.get(i).y > getHeight()) {
					bullets.remove(i);
					i--;
				}
			}
			playerColor = getRandomColor();
			g2.setColor(playerColor);
			g2.fill(new Rectangle(playerX, playerY, 40, 40));
			g2.setColor(Color.white);
			for (int i = 0; i < enemies.size(); i++) {
				enemies.get(i).move(playerX + 20, playerY + 20);
				if (Math.abs(playerX + 20 - enemies.get(i).xPoints[1]) < 30 && Math.abs(playerY + 20 - (enemies.get(i).yPoints[1] + 10)) < 30) {
					gameRunning = false;
					enemies.clear();
					bullets.clear();
					enemySpawner.stop();
					theGame.stop();
					score.stop();
					g2.clearRect(0, 0, getWidth(), getHeight());
					g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize() * 1.5f));
					drawCenteredString("GAME OVER", new Rectangle(0, 0, WIDTH, HEIGHT - 300));
					g2.setFont(g2.getFont().deriveFont(g2.getFont().getSize() / 1.5f));
					if (currentScore > highScore) {
						highScore = currentScore;
						drawCenteredString("New Record!", new Rectangle(0, 0, WIDTH, HEIGHT - 50));
					}
					drawCenteredString("High Score: " + highScore, new Rectangle(0, 0, WIDTH, HEIGHT - 150));
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							reset();
						}
					});
					button.setBounds(190, 375, 150, 50);
					button.setForeground(Color.yellow);
					button.setBackground(Color.blue);
					button.setBorderPainted(false);
					button.setFocusable(false);
					button.setVisible(true);
					button.setOpaque(true);
					add(button);
					break;
				}
				for (int j = 0; j < bullets.size(); j++) {
					Enemy en = enemies.get(i);
					Bullet b = bullets.get(j);
					if (Math.abs(b.x - en.xPoints[1]) < 15 && Math.abs(b.y - (en.yPoints[1] + 10)) < 15) {
						currentScore += 50 * gamemode;
						enemies.remove(i);
						bullets.remove(j);
						i--;
						break;
					}
				}
			}
			g2.drawString("Score: " + currentScore, getWidth() - 150, 50);
			repaint();
		}
	}

	class EnemyGenerator implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int side = (int) (Math.random() * 2);
			int side2 = (int) (Math.random() * 2);
			if (side % 2 == 0) { // top and bottom
				enemies.add(new Enemy((int) (Math.random() * getWidth()), getHeight() * (side2 % 2)));
			} else { // sides
				enemies.add(new Enemy(getWidth() * (side2 % 2), (int) (Math.random() * getHeight())));
			}
			if (enemySpeed < 3 * gamemode) {
				enemySpeed += 0.2;
			}
			if (enemySpawnRate > 2000 / gamemode) {
				enemySpawnRate -= 100;
				enemySpawner.setDelay(enemySpawnRate);
			}
		}
	}

	class Enemy {
		int[] xPoints, yPoints;

		Enemy(int x, int y) {
			xPoints = new int[] {x - 10, x, x + 10};
			yPoints = new int[] {y + 10, y - 10, y + 10};
			g2.fill(new Polygon(xPoints, yPoints, 3));
		}

		public void move(int px, int py) {
			double angle = Math.atan2(py - (yPoints[1] + 10), px - xPoints[1]);
			for (int i = 0; i < xPoints.length; i++) {
				xPoints[i] += (int) (Math.cos(angle) * enemySpeed);
				yPoints[i] += (int) (Math.sin(angle) * enemySpeed);
			}
			g2.fill(new Polygon(xPoints, yPoints, 3));
		}
	}

	class Bullet {
		int x, y, vx, vy, d;
		Color bulletColor;

		Bullet(int x, int y, int vx, int vy, int diameter) {
			this.x = x;
			this.y = y;
			this.vx = vx;
			this.vy = vy;
			this.d = diameter;
			bulletColor = playerColor;
			g2.fillOval(x - d / 2, y - d / 2, d, d);
		}

		public void move() {
			x += vx;
			y += vy;
			g2.fillOval(x - d / 2, y - d / 2, d, d);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (gameRunning) {
			if (e.getKeyCode() == KeyEvent.VK_SPACE && getMousePosition() != null) {
				double angle = Math.atan2(getMousePosition().getY() - (playerY + 40), getMousePosition().getX() - (playerX + 20));
				bullets.add(new Bullet(playerX + 20, playerY + 20, (int) (bulletSpeed * Math.cos(angle)), (int) (bulletSpeed * Math.sin(angle)), 10));
			}
			if (e.getKeyCode() == KeyEvent.VK_A && playerX + 20 > 50) {
				playerX -= 20;
			}
			if (e.getKeyCode() == KeyEvent.VK_D && playerX + 20 < getWidth() - 60) {
				playerX += 20;
			}
			if (e.getKeyCode() == KeyEvent.VK_W && playerY + 20 > 50) {
				playerY -= 20;
			}
			if (e.getKeyCode() == KeyEvent.VK_S && playerY + 20 < getHeight() - 50) {
				playerY += 20;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (gameRunning) {
			double angle = Math.atan2(e.getY() - (playerY + 40), e.getX() - (playerX + 20));
			bullets.add(new Bullet(playerX + 20, playerY + 20, (int) (bulletSpeed * Math.cos(angle)), (int) (bulletSpeed * Math.sin(angle)), 10));
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Game");
		ShapeShooter project = new ShapeShooter();
		frame.add(project);
		frame.addKeyListener(project);
		frame.addMouseListener(project);
		frame.setSize(ShapeShooter.WIDTH, ShapeShooter.HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}