package com.TGstudios.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.TGstudios.entities.BulletShoot;
import com.TGstudios.entities.Enemy;
import com.TGstudios.entities.Entity;
import com.TGstudios.entities.Player;
import com.TGstudios.graficos.Spritesheet;
import com.TGstudios.graficos.UI;
import com.TGstudios.world.World;

public class Game extends Canvas implements Runnable,KeyListener{

	private static final long serialVersionUID = 1L;
	public static JFrame frame;
	private Thread thread;
	private boolean isRunning = true;
	public static final int WIDTH = 240;
	public static final int HEIGHT = 160;
	public static final int SCALE = 3;
	
	public static int CUR_LEVEL = 1;
	public static int MAX_LEVEL = 5;
	private BufferedImage image;
	
	public static List<Entity> entities;
	public static List<Enemy> enemies;
	public static List<BulletShoot> bullets;
	
	public static Spritesheet Spritesheet;
	
	public static World world;
	
	public static Player player;
	
	public static Random rand;
	
	public UI ui;
	
	public static String gameState = "MENU";
	private boolean showMessageGameOver = true;
	private int framesGameOver = 0;
	private boolean restartGame = false;
	
	public Menu menu;
	
	public static void main(String args[]){
		Game game = new Game();
		game.start();
	}

	
	public Game(){
		rand = new Random();
		addKeyListener(this);
		setPreferredSize(new Dimension(WIDTH*SCALE,HEIGHT*SCALE));
		initFrame();
		//Inicializando objetos.
		ui = new UI();
		image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_RGB);
		entities = new ArrayList<Entity>();
		enemies = new ArrayList<Enemy>();
		bullets = new ArrayList<BulletShoot>();
		
		Spritesheet = new Spritesheet("/Spritesheet.png");
		player = new Player(32,0,16,16, Spritesheet.getSprite(32, 0,16,16));
		entities.add(player);
		world = new World("/level1.png");
		
		
		menu = new Menu();
	}
	
	public void initFrame(){
		frame = new JFrame("Game #1");
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public synchronized void start(){
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}
	
	public synchronized void stop(){
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void tick(){
		if(gameState == "NORMAL") {
		this.restartGame = false;	
		for(int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.tick();
		}
		
		for(int i = 0; i < bullets.size(); i++) {
			bullets.get(i).tick();
		}
		
		
		}else if(gameState == "GAME_OVER") {
			this.framesGameOver++;
			if(this.framesGameOver == 30) {
				this.framesGameOver = 0;
				if(this.showMessageGameOver)
					this.showMessageGameOver = false;
					else
						this.showMessageGameOver = true;
			}
			
			if(restartGame) {
				this.restartGame = false;
				Game.gameState = "NORMAL";
				CUR_LEVEL = 1;
				String newWorld = "level"+CUR_LEVEL+".png";
				//System.out.println(newWorld);
				World.restartGame(newWorld);
			}
		}else if(gameState == "MENU") {
			
			menu.tick();
		}
	}
	

	

	
	
	
	
	
	public void render(){
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = image.getGraphics();
		g.setColor(new Color(0,0,0));
		g.fillRect(0, 0,WIDTH,HEIGHT);
		
		/*Renderizaï¿½ï¿½o do jogo*/
		//Graphics2D g2 = (Graphics2D) g;
		world.render(g);
		for(int i = 0; i < entities.size(); i++) {
			Entity e = entities.get(i);
			e.render(g);
		}
		for(int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(g);
		}
		ui.render(g);
		/***/
		g.dispose();
		g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0,WIDTH*SCALE,HEIGHT*SCALE,null);
		g.setFont(new Font("arial",Font.BOLD,20));
		g.setColor(Color.white);
		g.drawString("Munição: " + player.ammo,580,20);
		if(gameState == "GAME_OVER") {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color(0,0,0,100));
			g2.fillRect(0, 0, WIDTH*SCALE, HEIGHT*SCALE);
			g.setFont(new Font("arial",Font.BOLD,36));
			g.setColor(Color.white);
			g.drawString("Game Over",(WIDTH*SCALE) / 2 - 90,(HEIGHT* SCALE) / 2 - 20);
			g.setFont(new Font("arial",Font.BOLD,32));
			if(showMessageGameOver)
				g.drawString(">Pressione Enter para reiniciar<",(WIDTH*SCALE) / 2 - 230,(HEIGHT* SCALE) / 2 + 40);
		}else if(gameState == "MENU") {
			menu.render(g);
		}
		bs.show();
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		int frames = 0;
		double timer = System.currentTimeMillis();
		requestFocus();
		while(isRunning){
			long now = System.nanoTime();
			delta+= (now - lastTime) / ns;
			lastTime = now;
			if(delta >= 1) {
				tick();
				render();
				frames++;
				delta--;
			}
			
			if(System.currentTimeMillis() - timer >= 1000){
				System.out.println("FPS: "+ frames);
				frames = 0;
				timer+=1000;
			}
			
		}
		
		stop();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_D){
			player.right = true;
			player.dir = player.right_dir;
		}else if(e.getKeyCode() == KeyEvent.VK_A){
			player.left = true;
			player.dir = player.left_dir;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_W){
			player.up = true;
			player.dir = player.up_dir;
			if(gameState == "MENU") {
				menu.up = true;
			}
		}else if(e.getKeyCode() == KeyEvent.VK_S) {
			player.down = true;
			player.dir = player.down_dir;
			
			if(gameState == "MENU") {
				menu.down = true;
			}
			
		}
		
		if(e.getKeyCode() == KeyEvent.VK_X){
			player.shoot = true;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			this.restartGame = true;
			if(gameState == "MENU") {
				menu.enter = true;
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gameState = "MENU";
			menu.pause = true;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			player.isRunning = true;
			
		}
		
		
		
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_D){
			player.right = false;
		}else if(e.getKeyCode() == KeyEvent.VK_A){
			player.left = false;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_W){
			player.up = false;
		}else if(e.getKeyCode() == KeyEvent.VK_S) {
			player.down = false;
		}
		if(e.getKeyCode() == KeyEvent.VK_X){
			player.shoot = false;
		}
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
				player.isRunning = false;
		}
		}
		
	

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	

	
}
