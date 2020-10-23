package com.TGstudios.entities;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.TGstudios.graficos.Spritesheet;
import com.TGstudios.main.Game;
import com.TGstudios.world.Camera;
import com.TGstudios.world.World;

public class Player extends Entity{
	
	public boolean right,up,left,down;
	public int right_dir =0, left_dir=1, up_dir=2, down_dir=3;
	public int dir;
	public double speed = 2;
	
	private int frames = 0,maxFrames = 10,index = 0,maxIndex = 3;
	private boolean moved = false;
	private BufferedImage[] rightPlayer;
	private BufferedImage[] leftPlayer;
	private BufferedImage[] upPlayer;
	private BufferedImage[] downPlayer;
	
	private BufferedImage playerDamage;
	
	private boolean Bow = false;
	public int ammo = 0;
	
	public boolean isDamaged = false;
	public boolean isRunning = false;
	
	private int damageFrames = 0;
	
	public boolean shoot = false;
	
	public double life = 100,maxlife=100;
	public double stamina = 100,maxStamina=100;

	public Player(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
		
		rightPlayer = new BufferedImage[4];
		leftPlayer = new BufferedImage[4];
		upPlayer = new BufferedImage[4];
		downPlayer = new BufferedImage[4];
		
		playerDamage = Game.Spritesheet.getSprite(96, 16, 16,16);
		
		for(int i =0; i < 4; i++){
			rightPlayer[i] = Game.Spritesheet.getSprite(32 + (i*16), 16, 16, 16);
		}
		for(int i =0; i < 4; i++){
			leftPlayer[i] = Game.Spritesheet.getSprite(32 + (i*16), 32, 16, 16);
		}
		for(int i =0; i < 4; i++){
			upPlayer[i] = Game.Spritesheet.getSprite(32 + (i*16), 48, 16, 16);
		}
		for(int i =0; i < 4; i++){
			downPlayer[i] = Game.Spritesheet.getSprite(32 + (i*16), 0, 16, 16);
		}
		
	}
	
	public void tick(){
		moved = false;
		if(right && World.isFree((int)(x+speed),this.getY())) {
			moved = true;
			dir = right_dir;
			x+=speed;
			if(isRunning == true && World.isFree((int)(x+(speed+3)),this.getY())) {
				if( stamina > 0 ){
					stamina -=0.1;
				x+=speed+1;
			}
			}
		}
		else if(left && World.isFree((int)(x-speed),this.getY())) {
			moved = true;
			dir = left_dir;
			x-=speed;
			if(isRunning == true && World.isFree((int)(x-(speed+3)),this.getY())) {
				if( stamina > 0 ){
					stamina -=0.45;
				x-=speed+1;
			}
		}
		}
		if(up && World.isFree(this.getX(),(int)(y-speed))){
			moved = true;
			dir = up_dir;
			y-=speed;
			if(isRunning == true && World.isFree(this.getX(),(int)(y-(speed+3)))) {
				if( stamina > 0 ){
					stamina -=0.45;
				y-=speed+1;
			}
			}
		}
		else if(down && World.isFree(this.getX(),(int)(y+speed))){
			moved = true;
			dir = down_dir;
			y+=speed;
			if(isRunning == true && World.isFree(this.getX(),(int)(y+(speed+3)))) {
				if( stamina > 0 ){
					stamina -=0.45;
				y+=speed+1;
			}
			}
		}
		
		if(moved) {
			frames++;
			if(frames == maxFrames) {
				frames = 0;
				index++;
				if(index > maxIndex)
					index = 0;
			}
		}
		
		checkCollisionLifePack();
		checkCollisionAmmo();
		checkCollisionGun();
		doorCheck();
		
		if(isDamaged) {
			this.damageFrames++;
			if(this.damageFrames == 8) {
				this.damageFrames = 0;
				isDamaged = false;
			}
		}
	
		if (isRunning == false && stamina < maxStamina) {
			stamina+=0.05;
		}
		
		if(shoot) {
			shoot = false;
			if(Bow && ammo > 0) {
			ammo--;
			//Criar bala e atirar!
			
			int dx = 0;
			int px = 0;
			int py = 0;
			int dy = 0;
			if(dir == right_dir) {
				px = 4;
				py=5;
				dx = 1;
			}else if(dir == left_dir) {
				px = -12;
				py=5;
				dx = -1;
			}
			if(dir == up_dir) {
				py = -1;
				px = -3;
				dy = -1;
			}else if(dir == down_dir) {
				py = 8;
				px = -5;
				dy = 1;
				
			}
			
			BulletShoot bullet = new BulletShoot(this.getX()+px+10,this.getY()+py,3,3,null,dx,dy);
			Game.bullets.add(bullet);
			}
		}
		
			
		
		if(life<=0) {
			//Game over!
			life = 0;
			Game.gameState = "GAME_OVER";
		}
		
		updateCamera();
	}
		
	
	
	public void updateCamera() {
		Camera.x = Camera.clamp(this.getX() - (Game.WIDTH/2),0,World.WIDTH*16 - Game.WIDTH);
		Camera.y = Camera.clamp(this.getY() - (Game.HEIGHT/2),0,World.HEIGHT*16 - Game.HEIGHT);
	}
	
	public void checkCollisionGun() {
		for(int i = 0; i < Game.entities.size(); i++){
			Entity atual = Game.entities.get(i);
			if(atual instanceof Weapon) {
				if(Entity.isColidding(this, atual)) {
					Bow = true;
					//System.out.println("Pegou a arma!");
			
					Game.entities.remove(atual);
				}
			}
		}
	}
	
	public void checkCollisionAmmo() {
		for(int i = 0; i < Game.entities.size(); i++){
			Entity atual = Game.entities.get(i);
			if(atual instanceof Bullet) {
				if(Entity.isColidding(this, atual)) {
					ammo+=30;
					//System.out.println("Municao atual:" + ammo);
					Game.entities.remove(atual);
				}
			}
		}
	}
	
	
	public void doorCheck() {
		for(int i = 0; i < Game.entities.size(); i++){
			Entity atual = Game.entities.get(i);
			if(atual instanceof Doors) {
				if(Entity.isColidding(this, atual) && Game.enemies.size() == 0 ) {
							//Avançar para o próximo level!
							Game.CUR_LEVEL++;
							if(Game.CUR_LEVEL > Game.MAX_LEVEL){
								Game.CUR_LEVEL = 1;
							}
							String newWorld = "level"+Game.CUR_LEVEL+".png";
							//System.out.println(newWorld);
							World.restartGame(newWorld);
						} 
				}
			}
		}
	
		
	public void checkCollisionLifePack(){
		for(int i = 0; i < Game.entities.size(); i++){
			Entity atual = Game.entities.get(i);
			if(atual instanceof Lifepack) {
				if(Entity.isColidding(this, atual) && life < 100) {
					life+=10;
					if(life > 100)
						life = 100;
					Game.entities.remove(atual);
				}
			}
		}
	}

	
	public void render(Graphics g) {
		if(!isDamaged) {
			if(dir == right_dir) {
				g.drawImage(rightPlayer[index], this.getX() - Camera.x,this.getY() - Camera.y, null);
				if(Bow) {
					//Desenhar arma para direita.
					g.drawImage(Entity.GUN_RIGHT, this.getX()+8 - Camera.x,this.getY() - Camera.y, null);
				}
			}else if(dir == left_dir) {
				g.drawImage(leftPlayer[index], this.getX() - Camera.x,this.getY() - Camera.y, null);
				if(Bow) {
					//Desenhar arma para esquerda.
					g.drawImage(Entity.GUN_LEFT, this.getX()-8 - Camera.x,this.getY() - Camera.y, null);
				}
			}
				
				if(dir == up_dir) {
					g.drawImage(upPlayer[index], this.getX() - Camera.x,this.getY() - Camera.y, null);
					} else if(dir == down_dir) {
						g.drawImage(downPlayer[index], this.getX() - Camera.x,this.getY() - Camera.y, null);
					}


			} else {
			g.drawImage(playerDamage, this.getX()-Camera.x, this.getY() - Camera.y,null);
			
			}
		}
	}


