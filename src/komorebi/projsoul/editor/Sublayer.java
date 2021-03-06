package komorebi.projsoul.editor;

import komorebi.projsoul.editor.controls.CheckBox;
import komorebi.projsoul.editor.controls.RadioButton;
import komorebi.projsoul.editor.controls.TextField;
import komorebi.projsoul.editor.history.SublayerNameChangedRevision;
import komorebi.projsoul.editor.modes.Mode;
import komorebi.projsoul.engine.Draw;
import komorebi.projsoul.engine.Key;
import komorebi.projsoul.engine.KeyHandler;
import komorebi.projsoul.map.EditorMap;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

public class Sublayer {

  private Rectangle pencil, minus, draggable;
  private int y;

  private RadioButton visible;
  private CheckBox checkbox;
  private TextField text;

  private LayerType type;

  private boolean merge;

  private int[][] tiles;

  private boolean queueForRemoval;


  public Sublayer(LayerType type)
  {
    this(type, LayerType.generateName(type));
  }
  
  public Sublayer(LayerType type, String name)
  {
    pencil = new Rectangle(110, y, 26, 26);
    minus = new Rectangle(136, y, 26, 26);
    draggable = new Rectangle(20, y, 90, 16);
    visible = new RadioButton(164, y+5);
    checkbox = new CheckBox(136, y);
    text = new TextField(20, y);

    this.type = type;
    text.setText(name);

    tiles = new int[EditorMap.getHeight()][EditorMap.getWidth()];
  }

  /**
   * Sets the y location of the sublayer's textbox and buttons
   * @param y
   */
  public void setLocation(int y)
  {
    pencil.setBounds(110, y, 26, 26);
    minus.setBounds(136, y, 26, 26);
    draggable.setBounds(20, y, 90, 16);
    visible.setLocation(164, y+5);
    checkbox.setLocation(136, y);
    text.setLocation(20,y);
  }

  public LayerType getType()
  {
    return type;
  }

  public void setTiles(int[][] tiles)
  {
    this.tiles = tiles;
  }

  public void update()
  {
    if (KeyHandler.bufferedKeyClick(Key.LBUTTON))
    {      
      if (!merge)
      {
        updateWhenNotMerging();
      }
    }


    if (!merge)
    {
      visible.update();
    } else
    {
      checkbox.update();
    }

    if (text.isFocused())
    {
      text.update();
    }

  }
  
  private void updateWhenNotMerging()
  {
    if (pencil.contains(Mode.getFloatMouseX(), Mode.getFloatMouseY()))
    {
      switchIsEditingName();
    } else if (minus.contains(Mode.getFloatMouseX(), 
        Mode.getFloatMouseY()) && confirmDelete())
    {
      queueForRemoval = true;
      KeyHandler.tempDisable(Key.LBUTTON);

      boolean layPush = false;

      for (Layer l: Editor.getMap().getLayerControl().getLayers())
      {
        if (l.getSubs().contains(this))
        {
          boolean push = false;

          for (Sublayer s: l.getSubs())
          {
            if (push)
            {
              // s.push(-32);
            }

            if (s == this)
            {
              push = true;
            }
          }

          layPush = true;
          l.pushButtons(-32);
        }

        if (!layPush)
        {
          //l.push(-32);
        }

      } 
    }
  }

  public void setMerging(boolean b)
  {
    merge = b;
  }

  public void render()
  {    
    if (!merge && (text.showPencil() || !text.isFocused()))
    {
      Draw.drawIfInBounds(Draw.LAYER_MANAGER, pencil.x, pencil.y, pencil.width, pencil.height, 
          0, 106, 13, 119, 2);
    }

    if (!merge)
    {
      Draw.drawIfInBounds(Draw.LAYER_MANAGER, minus.x, minus.y, minus.width, minus.height, 
          13, 106, 26, 119, 2);
      visible.render();
    } else
    {
      checkbox.render();
    }

    text.render();
  }

  public void showTiles()
  {
    for (int i = 0; i < tiles.length; i++)
    {
      for (int j = 0; j < tiles[0].length; j++)
      {
        if(EditorMap.checkTileInBounds(EditorMap.getX()+j*16*Editor.zoom(), 
            EditorMap.getY()+i*16*Editor.zoom())){
          Draw.tileZoom(EditorMap.getX()+j*16, EditorMap.getY()+i*16, Draw.getTexX(tiles[i][j]), 
              Draw.getTexY(tiles[i][j]), Draw.getTexture(tiles[i][j]), Editor.zoom(),
              EditorMap.getX(), EditorMap.getY());
        }
      }
    }
  }

  public RadioButton getRadioButton()
  {
    return visible;
  }

  public TextField getTextField()
  {
    return text;
  }

  public boolean isQueuedForRemoval()
  {
    return queueForRemoval; 
  }

  public void kill()
  {
    queueForRemoval = false;
  }


  public int[][] getTiles()
  {
    return tiles;
  }

  public int getY()
  {
    return pencil.y;
  }

  public boolean draggableArea(float mx, float my)
  {
    return draggable.contains(new Point((int) mx, (int) my)); 
  }

  public CheckBox getCheckBox()
  {
    return checkbox;
  }

  public static Sublayer merge(Sublayer[] merge)
  {
    Sublayer ret = new Sublayer(merge[0].getType());

    int[][] tiles;

    try 
    {
      tiles = new int[merge[0].getTiles().length]
          [merge[0].getTiles()[0].length];
    } catch (ArrayIndexOutOfBoundsException e)
    {
      return null;
    }

    for (Sublayer sub: merge)
    {
      for (int i = 0; i < sub.getTiles().length; i++)
      {
        for (int j = 0; j < sub.getTiles()[0].length; j++)
        {
          if (sub.getTiles()[i][j] != 0)
          {
            tiles[i][j] = sub.getTiles()[i][j];
          }
        }
      }
    }

    ret.setTiles(tiles);
    return ret;

  }

  private static boolean confirmDelete()
  {
    int returnee = JOptionPane.showConfirmDialog(null, 
        "Are you sure you want to delete this sublayer?");

    return (returnee == JOptionPane.YES_OPTION);

  }

  private void createRenamedRevision()
  {
    Editor.getMap().addRevision(new SublayerNameChangedRevision(
        text.getPreviousText(), text.getText(), this));
  }
 
  private void unfocusAllTextBoxesExcept(TextField text)
  {
    for (Layer l: Editor.getMap().getLayerControl().getLayers())
    {
      for (Sublayer sub: l.getSubs())
      {
        if (sub.getTextField()!=text)
        {
          sub.getTextField().setFocused(false);
        }
      }
    }
  }
  
  private void switchIsEditingName()
  {
    text.setFocused(!text.isFocused());

    if (text.isFocused())
    {    
      unfocusAllTextBoxesExcept(text);
    } else if (text.wasChanged())
    {
      createRenamedRevision();
      text.setUnchanged();
    }

    KeyHandler.tempDisable(Key.LBUTTON);
  }
}
