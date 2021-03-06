/**
 * Shooter.java    Dec 20, 2016, 9:15:13 AM
 */
package komorebi.projsoul.entities.enemy;

import komorebi.projsoul.ai.node.composite.MemSequence;
import komorebi.projsoul.ai.node.composite.Priority;
import komorebi.projsoul.ai.node.composite.Sequence;
import komorebi.projsoul.ai.node.leaf.IdleBehavior;
import komorebi.projsoul.ai.node.leaf.LineUpBehavior;
import komorebi.projsoul.ai.node.leaf.MoveToTarget;
import komorebi.projsoul.ai.node.leaf.RunBehavior;
import komorebi.projsoul.ai.node.leaf.ShootBehavior;
import komorebi.projsoul.ai.node.leaf.WalkBehavior;
import komorebi.projsoul.ai.node.leaf.conditions.IsOutofMagic;
import komorebi.projsoul.ai.node.leaf.conditions.IsPlayerInRange;
import komorebi.projsoul.attack.projectile.ProjectileAttack;
import komorebi.projsoul.attack.projectile.StraightShot;
import komorebi.projsoul.engine.Animation;
import komorebi.projsoul.engine.Arithmetic;
import komorebi.projsoul.engine.Draw;
import komorebi.projsoul.entities.Face;
import komorebi.projsoul.map.EditorMap;
import komorebi.projsoul.map.EditorMap.Modes;
import komorebi.projsoul.map.MapHandler;

/**
 * An enemy that tries to line up with the player and shoot him
 *
 * @author Aaron Roy
 */
public class Shooter extends MagicEnemy {

  protected float currDist;         //Calculated distance between this enemy and the player

  protected static final float RUN_SPEED = 1.5f;
  protected static final float WALK_SPEED = 0.5f;

  private static final int TOLERANCE = 8;

  //DEBUG radius
  protected int red, green, blue;
  
  private ProjectileAttack<StraightShot> projectile;
  
  //TODO Implement
  private Animation shootAni;
  private Animation normalAni;

  protected final int aggroDistance;
  protected final int runDistance;

  private static final int MAX_IDLE = 60;
  private static final int MAX_WALK = 120;
  private static final int MAX_SHOOT = 120;

  //Stats
  public static final int baseAttack = 25;
  public static final int baseMagicAttack = 30;
  public static final int baseDefense = 50;
  public static final int baseHealth = 250;
  public static final int baseMagic = 50;
  
  public static final int SHOOT_COST = 5;
  
  private static final float PROJ_SPEED = 2;

  private Priority root;

  /**
   * Creates an enemy that tries to run away from the player and shoot at a distance
   * 
   * @param x The x location (in the map) of the enemy
   * @param y The y location (in the map) of the enemy
   * @param type The sprite of this enemy
   * @param radius The distance the enemy tries to be away from the player
   */
  public Shooter(float x, float y, EnemyType type, int radius) {
    this(x, y, type, radius, 1);
  }


  /**
   * Creates an enemy that tries to run away from the player and shoot at a distance
   * 
   * @param x The x location (in the map) of the enemy
   * @param y The y location (in the map) of the enemy
   * @param type The sprite of this enemy
   * @param radius The distance the enemy tries to be away from the player
   * @param level The level of this enemy
   */
  public Shooter(float x, float y, EnemyType type, int radius, int level) {
    super(x, y, type, level);

    aggroDistance = 16*radius;
    runDistance = aggroDistance/4;

    regAni.stop();

    red = (int)(Math.random()*255);
    green = (int)(Math.random()*255);
    blue = (int)(Math.random()*255);
    
    projectile = new ProjectileAttack<StraightShot>(new StraightShot());
        
    root = new Priority(
        new Sequence(                                //Tired
            new IsOutofMagic(this, SHOOT_COST),
            new MoveToTarget(this, WALK_SPEED/2)
            ),
        new Sequence(                                //Run Away
            new IsPlayerInRange(this, runDistance),
            new RunBehavior(this, RUN_SPEED)
            ),
        new Sequence(                                //Line up
            new IsPlayerInRange(this, aggroDistance),
            new Sequence(
                new LineUpBehavior(this, RUN_SPEED, TOLERANCE),
                new ShootBehavior(this, MAX_SHOOT, PROJ_SPEED, aggroDistance)
                )
            ),
        new MemSequence(                             //Walk around
            new IdleBehavior(this, MAX_IDLE),
            new WalkBehavior(this, MAX_WALK, WALK_SPEED)
            )
        );

  }

  @Override
  public void update(){
    if(!hurt){
      dx = 0;
      dy = 0;

      targetX = MapHandler.getPlayer().getX();
      targetY = MapHandler.getPlayer().getY();

      currDist = Arithmetic.distanceBetween(x,y,targetX,targetY);

//      behaviors.get(currState).update();
//      decideWhetherToSwitchStates();
      root.update();
    }

    super.update();

  }
  
  public void refreshDirection(){
    if(Math.abs(dx) < Math.abs(dy)){
      if (dx < 0) {
        direction = Face.LEFT;
      }
      else if (dy > 0) {
        direction = Face.RIGHT;
      }
    }
    else {
      if (dy < 0) {
        direction = Face.DOWN;
      }
      else if (dy > 0) {
        direction = Face.UP;
      }
    }
  }


  /**
   * Shoots a projectile
   * 
   * @param aDx X velocity (in px/sec)
   * @param aDy Y velocity (in px/sec)
   */
  public boolean shoot(float aDx, float aDy){
    if (magic > SHOOT_COST){
      projectile.newAttack(x, y, aDx, aDy, direction, magicAttack);
      magic -= SHOOT_COST;
      return true;
    }
    return false;
  }
  
  @Override
  public void render() {
    //TODO Better implementation
    if(EditorMap.getMode() == Modes.EVENT){
      Draw.circ(x, y, aggroDistance, red, blue, green, 64);
      Draw.circ(x, y, runDistance, red, blue, green, 64);

    }
    if(MapHandler.isHitBox){
      Draw.circCam(x, y, aggroDistance, red, blue, green, 64);
      Draw.circCam(x, y, runDistance, red, blue, green, 64);
    }

    super.render();
  }

  @Override
  public int xpPerLevel() {
    return 100;
  }

  @Override
  public int baseAttack() {
    return baseAttack;
  }
  
  @Override
  public int baseMagicAttack() {
    return baseMagicAttack;
  }
  
  @Override
  public int baseMagic() {
    return baseMagic;
  }

  @Override
  public int baseDefense() {
    return baseDefense;
  }

  @Override
  public int baseHealth() {
    return baseHealth;
  }

  @Override
  public String getBehavior(){
    return "shooter";
  }

}
