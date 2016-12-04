/**
 * TileMode.java   Aug 4, 2016, 6:16:33 PM
 */
package komorebi.projsoul.editor.modes;

import static komorebi.projsoul.editor.Buttons.BUTTON_SIZE;
import static komorebi.projsoul.engine.MainE.HEIGHT;
import static komorebi.projsoul.engine.MainE.WIDTH;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import komorebi.projsoul.editor.Editor;
import komorebi.projsoul.editor.Palette;
import komorebi.projsoul.engine.Draw;
import komorebi.projsoul.engine.KeyHandler;
import komorebi.projsoul.engine.MainE;
import komorebi.projsoul.engine.Playable;
import komorebi.projsoul.gameplay.Key;
import komorebi.projsoul.map.EditorMap;

/**
 * The tile editing part of the editor
 * 
 * @author Aaron Roy
 */
public class TileMode extends Mode implements Playable{

  private static int[][] selection;
  private static Palette pal = new Palette();
  
  private static boolean removeMode = false;
  
  private static String res = "res\\tilesets\\";

  @Override
  public void getInput() {
    pal.getInput();
  }
  
  @Override
  public void update(){
    pal.update();
    
    //Sets mouse tile to the one from the palette
    if(lButtonIsDown && checkMapBounds() && !isSelection){
  
      tiles[my][mx] = pal.getSelected();
  
      EditorMap.setUnsaved();
      if(Display.getTitle().charAt(Display.getTitle().length()-1) != '*'){
        Display.setTitle(Display.getTitle()+"*");
      }
    }
  
    //Sets palette's selected to mouse tile
    if(rButtonIsDown && checkMapBounds() && !rButtonWasDown){
      pal.setLoc(tiles[getMouseY()][getMouseX()]);
      clearSelection();
  
    }
  
    //Flood Fills tiles
    if(mButtonIsDown && !mButtonWasDown && checkMapBounds()){
      int mx = getMouseX();
      int my = getMouseY();
  
      flood(mx, my, tiles[my][mx]);
      EditorMap.setUnsaved();
    }
  
    //Creates a selection
    if(rStartDragging){
      initX = getMouseX();
      initY = getMouseY();
      
      if(initX < 0){
        initX = 0;
      }else if(initX >= tiles[0].length){
        initX = tiles[0].length-1;
      }
      
      if(initY < 0){
        initY = 0;
      }else if(initY >= tiles.length){
        initY = tiles.length-1;
      }
      
    }
  
    if(rIsDragging && checkMapBounds()){
      createSelection();
    }
  
    if(isSelection && lButtonIsDown && checkMapBounds() && !lButtonWasDown){
      for(int i = 0; i < selection.length; i++){
        for (int j = 0; j < selection[0].length; j++) {
          if(checkTileBounds(getMouseX()+j, getMouseY()+i)){
            tiles[getMouseY()+i][getMouseX()+j] = 
                selection[i][j];
          }
        }
      }
      EditorMap.setUnsaved();
    }
    
    if(KeyHandler.keyClick(Key.LBUTTON) && checkButtonBounds()){
      
      switch(Mouse.getX()/(32*MainE.scale)){
        case WIDTH/BUTTON_SIZE-3:
          JFileChooser chooser = new JFileChooser("res/tilesets/");
          FileNameExtensionFilter filter = new FileNameExtensionFilter(
              "Tileset Files (.tset)", "tset");
          chooser.setFileFilter(filter);
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setDialogTitle("Enter the name of the tileset to load");
          int returnee = chooser.showOpenDialog(null);
      
          KeyHandler.reloadKeyboard();
          
          if(returnee == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            pal.addTileset(path.substring(path.indexOf(res)+res.length(), 
                path.length()));
          }
          
          break;
        case WIDTH/BUTTON_SIZE-2:
          removeMode = !removeMode;
          break;
        default:
          //DEBUG Button fail text
          System.out.println("Event mode button failure pls");
      }
    }
    
    Mode.status.write(Editor.getMap().getUniqueTiles() + " unique tile(s)", 200, 1);
  
  }


  @Override
  public void render(){
    //Render selection
    if(selection != null){
      for (int i = 0; i < selection.length; i++) {
        for (int j = 0; j < selection[0].length; j++) {
          Draw.tile(EditorMap.getX()+tiles[0].length*SIZE+j*SIZE, 
              EditorMap.getY()+i*SIZE, 
              Draw.getTexX(selection[i][j]), Draw.getTexY(selection[i][j]), 
              Draw.getTexture(selection[i][j]));
        }
      }
      //Render preview block
      if(checkMapBounds()){
        Draw.rect(EditorMap.getX()+mx*SIZE, EditorMap.getY()+my*SIZE, 
            selection[0].length*SIZE, selection.length*SIZE, 
            16, 16,16,16, 2);
      }
    }
    
    EditorMap.renderGrid();
    
    pal.render();
    
    Draw.rect(WIDTH-BUTTON_SIZE*3, HEIGHT-BUTTON_SIZE, 32, 32, 32, 16, 48, 32, 2);
    if (!removeMode)
    {
      Draw.rect(WIDTH-BUTTON_SIZE*2, HEIGHT-BUTTON_SIZE, 
          32, 32, 48, 16, 64, 32, 2);
    } else
    {
      Draw.rect(WIDTH-BUTTON_SIZE*2, HEIGHT-BUTTON_SIZE,
          32, 32, 96, 0, 112, 16, 2);
    }
    if(checkButtonBounds()){
      int x = Mouse.getX()/(BUTTON_SIZE*MainE.getScale())*BUTTON_SIZE;
      int y = HEIGHT - BUTTON_SIZE;

      Draw.rect(x, y, BUTTON_SIZE, BUTTON_SIZE, 64, 0, 2);
    }
  }
  
  /**
   * Checks if the Mouse is in bounds of the buttons
   * 
   * @return Mouse is on a button
   */
  private boolean checkButtonBounds() {
    return (Mouse.getX()/MainE.getScale() > WIDTH-BUTTON_SIZE*3 &&
        Mouse.getX()/MainE.getScale() < WIDTH-BUTTON_SIZE &&
        Mouse.getY()/MainE.getScale() > HEIGHT-BUTTON_SIZE);
  }

  /**
   * Creates a new selection
   */
  private void createSelection() {
    selection = new int[Math.abs(getMouseY()-initY)+1]
        [Math.abs(getMouseX()-initX)+1];
    int firstX, lastX;
    int firstY, lastY;
  
    firstX = Math.min(initX, getMouseX());
    firstY = Math.min(initY, getMouseY());
  
    lastX = Math.max(initX, getMouseX());
    lastY = Math.max(initY, getMouseY());
  
    for(int i = 0; i <= lastY - firstY; i++){
      for(int j = 0; j <= lastX - firstX; j++){
        selection[i][j] =  tiles[firstY+i][firstX+j];
      }
    }
  
    isSelection = true;
  }

  public int[][] getSelection(){
    return selection;
  }

  public static void setSelection(int[][] sel){
    selection = sel;
  }




  /**
   * Recursive method that flood fills tiles
   * 
   * @param mouseX starting tile x
   * @param mouseY starting tile y
   * @param type tile to search and destroy
   */
  private void flood(int mouseX, int mouseY, int type) {
    if (mouseX < 0 || mouseX >= tiles[0].length ||
        mouseY < 0 || mouseY >= tiles.length){
      return;
    }
    if(tiles[mouseY][mouseX] != type || 
        tiles[mouseY][mouseX] == pal.getSelected()){
      return;
    }

    tiles[mouseY][mouseX] = pal.getSelected();
    flood(mouseX-1, mouseY,   type);
    flood(mouseX+1, mouseY,   type);
    flood(mouseX,   mouseY+1, type);
    flood(mouseX,   mouseY-1, type);
  }

  /**
   * Sets isSelection to s
   * 
   * @param selec value to set the selection to
   */
  public static void setIsSelection(boolean selec) {
    isSelection = selec;
  }

  /**
   * Clears the selection, making it disappear
   */
  public static void clearSelection(){
    selection = null;
    isSelection = false;
  }
  
  public static boolean isInRemoveMode()
  {
    return removeMode;
  }

  
  
}
