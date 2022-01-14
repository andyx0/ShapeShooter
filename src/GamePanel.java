import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
	
	private BufferedImage img;
	private Graphics2D g2;

	public GamePanel() {
		img = new BufferedImage(GameWindow.WIDTH, GameWindow.HEIGHT, BufferedImage.TYPE_INT_RGB);
		g2 = (Graphics2D)img.getGraphics();
	}
	
	public void draw() {
		g2.setColor(Color.blue);
		g2.fillRect(0, 0, GameWindow.WIDTH, GameWindow.HEIGHT);
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(img,0,0,null);
		JButton b = new JButton("Easy");
		b.setBounds(175, 200, 150, 50);
		//b.setOpaque(true);
		add(b);
		b.setVisible(true);
	}
}