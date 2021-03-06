package komorebi.projsoul.entities.player;

import static komorebi.projsoul.engine.KeyHandler.button;
import komorebi.projsoul.attack.Charge;
import komorebi.projsoul.attack.ElementalProperty;
import komorebi.projsoul.attack.MeleeAttack;
import komorebi.projsoul.engine.Animation;
import komorebi.projsoul.engine.Key;
import komorebi.projsoul.engine.KeyHandler;
import komorebi.projsoul.engine.KeyHandler.Control;
import komorebi.projsoul.gameplay.Camera;
import komorebi.projsoul.gameplay.HUD;
import komorebi.projsoul.gameplay.MagicBar;

/**
 * The earth professor Bruno, which there can be only one of
 *
 * @author Andrew Faulkenberry
 * @author Aaron Roy
 */
public class Bruno extends Player {

  public static int attack = 45, defense = 60, 
      maxHealth = 55, maxMagic = 40, money = 0;
  public static int level = 1, xp = 0, nextLevelUp = 10;
  
  public static final int TACKLE_COST = -2;
  
  private MeleeAttack<Charge> melee = new MeleeAttack<Charge>(new Charge());
  
  /**
   * Creates Bruno
   * 
   * @param x X pixel location
   * @param y Y pixel location
   */
  public Bruno(float x, float y) {
    super(x,y);
    charProperty = ElementalProperty.EARTH;
    character = Characters.BRUNO;

    characterDeathAni = new Animation(3,45,12,false);
    characterDeathAni.add(195,654,32,32,1,false);
    characterDeathAni.add(165,654,25,32,1,false);
      
    health = new HUD(maxHealth, money, maxMagic);

    attack1 = melee;
    
    initializeSprites();
  }

  @Override
  public void renderAttack() {
    if (attack1 == melee)
    {
      melee.getAttackInstance().play(getX(), y);
    }
   
    
  }
  
  @Override
  public void update()
  {
    
    super.update();
    
    //DEBUG shake shake Key.U
    if(KeyHandler.keyClick(Key.U)){
      System.out.println("Shake");
      Camera.shake(180, 5, 1);
    }
    
    if (isAttacking && attack1 == melee)
    {
      melee.update();
      setX(melee.getAttackInstance().getX());
      y = melee.getAttackInstance().getY();
      
      if (melee.getAttackInstance().isStopped())
      {
        isAttacking = false;
        noContact = false;
      }
    }
    
    if (button(Control.ATTACK) && !isAttacking)
    {
      isAttacking = true;
      
      float aDx = 0f, aDy = 0f;
      
      switch (dir)
      {
        case UP:
          aDy = 5f;
          break;
        case DOWN:
          aDy = -5f;
          break;
        case LEFT:
          aDx = -5f;
          break;
        case RIGHT:
          aDx = 5f;
          break;
        default:
          break;
      }
      
      if (attack1 == melee)
      {
        attack1.newAttack(getX(), y, aDx, aDy, dir, attack);
        isAttacking = true;
        
        health.changeMagicBy(TACKLE_COST);
      }
      
    }
  }

  @Override
  public void levelUp() {

    level++;

    Bruno.xp-=nextLevelUp;
    nextLevelUp = getRequiredExp(level);

    int nAtt = (int) (Math.random()*3 + 1);
    int nDef = (int) (Math.random()*3 + 1);

    int nMag = (int) (Math.random()*8 + 3);
    int nHth = (int) (Math.random()*8 + 3);

    attack += nAtt;
    defense += nDef;
    maxMagic += nMag;
    maxHealth += nHth;

    health.addToMaxMagic(nMag);
    health.addToMaxHealth(nHth);
  }

  @Override
  public void giveXP(int xp) {
    Bruno.xp += xp;

    while (Bruno.xp >= nextLevelUp)
    {
      levelUp();
    }
  }
  
  public boolean isCharging()
  {
    return noContact && isAttacking;
  }
  
  public static void addDefense(int def)
  {
    defense+=def;
  }
  
  public static void subDefense(int def)
  {
    defense-=def;
  }

}
