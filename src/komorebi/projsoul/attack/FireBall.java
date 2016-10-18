package komorebi.projsoul.attack;

import java.awt.Rectangle;

import komorebi.projsoul.engine.Animation;
import komorebi.projsoul.entities.Enemy;
import komorebi.projsoul.entities.Face;
import komorebi.projsoul.states.Game;

public class FireBall {
  
  private float x, y;
  private float dx, dy;
  
  private Animation leftAttack;
  private Animation rightAttack;
  private Animation upAttack;
  private Animation downAttack;

  private Face currentDir;
  
  private Rectangle area;
  
  private int attack;
  private boolean destroyMe;
    
  public FireBall(float x, float y, float dx, float dy, Face dir, int attack)
  {    
    this.x = x;
    this.y = y;
    this.dx = dx;
    this.dy = dy;
    
    this.currentDir = dir;
    
    downAttack = new Animation(4,8,12,false);
    leftAttack = new Animation(4,8,12,false);
    rightAttack = new Animation(4,8,12,false);
    upAttack = new Animation(4,8,12,false);

    rightAttack.add(810,2,11,8);
    rightAttack.add(827,1,15,9);
    rightAttack.add(848,1,18,9);
    rightAttack.add(874,0,20,10);
    
    leftAttack.add(810,2,11,8,0,true);
    leftAttack.add(827,1,15,9,0,true);
    leftAttack.add(848,1,18,9,0,true);
    leftAttack.add(874,0,20,10,0,true);
    
    upAttack.add(810,2,11,8,1,false);
    upAttack.add(827,1,15,9,1,false);
    upAttack.add(848,1,18,9,1,false);
    upAttack.add(874,0,20,10,1,false);
    
    downAttack.add(810,2,11,8,1,true);
    downAttack.add(827,1,15,9,1,true);
    downAttack.add(848,1,18,9,1,true);
    downAttack.add(874,0,20,10,1,true);
    
    this.attack = attack;
    
    area = new Rectangle((int) x, (int) y, 11, 8);
  
  }
  
  public void play()
  {
    switch (currentDir)
    {
      case DOWN:
        downAttack.playCam(x, y);
        break;
      case LEFT:
        leftAttack.playCam(x, y);
        break;
      case RIGHT:
        rightAttack.playCam(x, y);
        break;
      case UP:
        rightAttack.playCam(x, y);
        break;
      default:
        break;
    } 
  
  }
  
  public void update()
  {
    
    switch (currentDir)
    {
      case DOWN:
        area.setSize((int) downAttack.getCurrentFrameSX(),
            (int) downAttack.getCurrentFrameSY()); 
        break;
      case LEFT:
        area.setSize((int) leftAttack.getCurrentFrameSX(),
            (int) downAttack.getCurrentFrameSY());
        break;
      case RIGHT:
        area.setSize((int) rightAttack.getCurrentFrameSX(),
            (int) downAttack.getCurrentFrameSY()); 
        break;
      case UP:
        area.setSize((int) upAttack.getCurrentFrameSX(),
            (int) downAttack.getCurrentFrameSY()); 
        break;
      default:
        break;
    } 
    
    for (Enemy enemy: Game.getMap().getEnemies())
    {

        enemy.updateHits(enemy.getHitBox().intersects(new Rectangle((int) (x+dx), (int) (y+dx),
            (int) area.getWidth(), (int) area.getHeight())), attack, currentDir);
        
        if (enemy.getHitBox().intersects(new Rectangle((int) (x+dx), (int) (y+dx),
            (int) area.getWidth(), (int) area.getHeight())))
        {
          destroyMe = true;
        }
        
    }
    
    x += dx;
    y += dy;
    
    area.setLocation((int) x, (int) y); 
  }
  
  public boolean destroyed()
  {
    return destroyMe;
  }
}
