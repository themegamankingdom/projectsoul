/**
 * State.java  May 15, 2016, 11:43:10 PM
 */

package komorebi.projsoul.states;

import komorebi.projsoul.entities.player.Player;
import komorebi.projsoul.map.Map;
import komorebi.projsoul.engine.Save;

/**
 * Represents a state in the game
 * 
 * @author Aaron Roy
 */

public abstract class State{

  /**
   * Represents all of the states in the game
   * 
   * @author Aaron Roy
   */
  public enum States{
    GAME, EDITOR, MENU, PAUSE, SAVELIST, DEATH, BANKSTATE, SHOPSTATE, INVENTORYSTATE;
  }

  /**
   * Makes all of the objects in this state get input from the player
   */
  public abstract void getInput();
  
  /**
   * Makes all of the objects in this state update themselves
   */
  public abstract void update();
  
  /**
   * Renders all of the objects in this state 
   */
  public abstract void render();
}
