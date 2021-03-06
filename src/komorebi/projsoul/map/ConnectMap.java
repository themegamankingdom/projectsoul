/**
 * ConnectMap.java    Oct 21, 2016, 9:49:03 AM
 */
package komorebi.projsoul.map;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;

import komorebi.projsoul.editor.modes.connect.ConnectMode;
import komorebi.projsoul.editor.modes.connect.World;
import komorebi.projsoul.engine.Draw;
import komorebi.projsoul.engine.KeyHandler;
import komorebi.projsoul.engine.Renderable;

/**
 * A map file that really doesn't have much
 *
 * @author Aaron Roy
 */
public class ConnectMap implements Renderable{

  private int height, width;
  private static final int NUM_LAYERS = 4;

  private ArrayList<ConnectMap> maps = new ArrayList<ConnectMap>();
  private int[][][] tiles;
  private int tx, ty;
  private String filePath;
  private Rectangle area;

  /**
   * The different sides a map can be on
   *
   * @author Aaron Roy
   */
  public enum Side{
    DOWN, LEFT, UP, RIGHT;

    @Override
    public String toString(){
      switch(this){
        case DOWN:  return "down";
        case LEFT:  return "left";
        case RIGHT: return "right";
        case UP:    return "up";
        default:    return "bleh";
      }
    }

    /**
     * Takes in a string and returns its respective Side
     * 
     * @param s The input string
     * @return the corespondent Side, null if not found
     */
    public static Side toEnum(String s){
      for(Side side: values()){
        if(side.toString().equals(s)){
          return side;
        }
      }

      return null;
    }
  }

  private Side side;

  private String name;

  public static final int SIZE = 16;         //Width and height of a tile



  /**
   * Creates a map based on the map file given, in reference to a world
   * 
   * @param key The location of the map
   */
  public ConnectMap(String filePath, int x, int y)
  {

    area = new Rectangle();

    setTileLocation(x, y);
    this.filePath = filePath;    

    ArrayList<String> lines;
    try {
      lines = readFile(filePath);
      readNecessaryData(lines);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    KeyHandler.reloadKeyboard();
  }

  private ArrayList<String> readFile(String filePath) throws IOException
  {
    try {
      BufferedReader read = new BufferedReader(new FileReader(new File(filePath)));

      String str;
      ArrayList<String> lines = new ArrayList<String>();

      while ((str = read.readLine()) != null)
      {
        lines.add(str);
      }

      read.close();

      return lines;
    } catch (IOException e) {
      throw e;
    }
  }

  private void readNecessaryData(ArrayList<String> data)
  {
    boolean readNextLine = false;
    int layer = 0;

    height = Integer.parseInt(data.get(0));
    width = Integer.parseInt(data.get(1));
    
    setSize(width, height);

    tiles = new int[NUM_LAYERS][height][width];

    for (String line: data)
    {
      if (line.startsWith("#") && !line.startsWith("#movement"))
      {
        readNextLine = true;
      } else if (readNextLine)
      {
        readLayer(line, layer);
        layer++;
        readNextLine = false;
      }
    }
  }

  private void readLayer(String data, int layerNum)
  {
    String[] split = data.split(" ");
    int i = height - 1, j = 0;

    for (String term: split)
    {
      if (term.isEmpty())
        continue;

      int times;
      if (term.contains("^"))
      {
        times = Integer.valueOf(term.substring(term.indexOf("^") + 1));
        term = term.substring(0, term.indexOf("^"));
      }
      else
        times = 1;

      for (int repeat = 0; repeat < times; repeat++)
      {
        tiles[layerNum][i][j] = Integer.valueOf(term);
        j++;
        if (j >= width)
        {
          j = 0;
          i--;
        }
      }
    }

  }

  @Override
  public void update(){
    //There is really nothing to update
  }

  @Override
  public void render(){    

    for (int lay = 0; lay < tiles.length; lay ++) {
      for (int i = 0; i < tiles[lay].length; i++) {
        for (int j = 0; j < tiles[lay][i].length; j++) {
          if(EditorMap.checkTileInBounds(tx*SIZE+j*SIZE+ConnectMode.getX(), 
              ty*SIZE+i*SIZE+ConnectMode.getY())){
            Draw.tile(tx*SIZE+j*SIZE+ConnectMode.getX(), ty*SIZE+i*SIZE+ConnectMode.getY(),
                Draw.getTexX(tiles[lay][i][j]), Draw.getTexY(tiles[lay][i][j]), 
                Draw.getTexture(tiles[lay][i][j]));
            if(EditorMap.grid){
              Draw.rect(tx*SIZE+j*SIZE+ConnectMode.getX(), ty*SIZE+i*SIZE+ConnectMode.getY(), 
                  SIZE, SIZE, 0, 16, SIZE, 16+SIZE, 2);
            }
          }
        }
      }
    }
  }

  public float getX() {
    return tx*16;
  }

  public float getY() {
    return ty*16;
  }

  /**
   * @return The x position of the map relative to the parent map in tiles
   */
  public int getTileX(){
    return tx;
  }

  /**
   * @return The y position of the map relative to the parent map in tiles
   */
  public int getTileY(){
    return ty;
  }

  /**
   * Moves the map to the specified pixel location
   * 
   * @param nx The x to move to
   * @param ny The y to move to
   */
  public void setLoc(float nx, float ny){
    tx = (int) nx/16;
    ty = (int) ny/16;
  }

  /**
   * Moves the map to a new x or y dependidng on its side
   *
   * @param tx The tile x to try
   * @param ty The tile y to try
   */
  public void setTileLocation(int tx, int ty){
    this.tx = tx;
    this.ty = ty;
    area.setLocation(tx, ty);
  }

  public void updateLoc(float dx, float dy)
  {
    tx = (int) ((tx*16 + dx) / 16);
    ty = (int) ((ty*16 + dy) / 16);

  }


  public Side getSide(){
    return side;
  }

  public void setSide(Side side) {
    this.side = side;
  }

  public String getName(){
    return name;
  }

  public int getPxHeight(){
    return tiles.length*EditorMap.SIZE;
  }

  public int getPxWidth(){
    return tiles[0].length*EditorMap.SIZE;
  }

  public boolean equals(ConnectMap connect)
  {
    return (connect.getFilePath().equals(filePath));
  }

  public boolean matches(String s) {
    return (s.equals(filePath));
  }

  public String getFilePath()
  {
    return filePath;
  }

  public boolean hasNewMaps(ArrayList<ConnectMap> arr)
  {    
    for (ConnectMap c: maps)
    {     
      if (!arr.contains(c))
      {
        return true;
      }
    }

    return false;
  }

  public void gather(ArrayList<ConnectMap> arr)
  {   
    for (ConnectMap c: maps)
    {      
      if (!arr.contains(c))
      {
        arr.add(c);

        if (c.hasNewMaps(arr))
        {
          c.gather(arr);
        }
      }
    }
  }

  public ArrayList<ConnectMap> map()
  {    
    ArrayList<ConnectMap> collection = new ArrayList<ConnectMap>();
    collection.add(this);
    this.gather(collection);
    return collection;
  }

  public int getWidth()
  {
    return tiles[0].length;
  }

  public int getHeight()
  {
    return tiles.length;
  }

  public Rectangle getArea()
  {
    return area;
  }

  private void setSize(int width, int height)
  {
    area.setSize(width, height);
  }

  public boolean isConnectedTo(ConnectMap c)
  {
    return maps.contains(c);
  }

  public void breakConnection(ConnectMap c)
  {
    maps.remove(c);
  }

  public void addConnection(ConnectMap c)
  {
    maps.add(c);
  }


  public String toString()
  {
    String ret = filePath + " connects to:\n";

    for (ConnectMap c: maps)
    {
      ret += "\t" + c.getFilePath() + "\n";
    }

    return ret;

  }

  public void saveConnectionsToFile()
  {
    try {
      File temp = File.createTempFile("tmp", "");

      BufferedReader reader = new BufferedReader(new FileReader(
          new File(filePath)));
      BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

      String str;

      while ((str = reader.readLine())!=null)
      {        
        if (!str.startsWith("connect"))
        {
          writer.write(str + "\n");
        } 
      }

      reader.close();

      for (ConnectMap c: maps)
      {
        String line = "connect " + c.getFilePath().replace("res/maps/", "").
            replace(".map", "") + " ";

        line += World.connectSide(area, c.getArea()).toString() + " ";
        line += (c.getTileX()-tx) + " " + (c.getTileY()-ty);

        writer.write(line + "\n");
      }

      writer.close();

      File oldFile = new File(filePath);
      if (oldFile.delete())
      {
        temp.renameTo(oldFile);
      }

    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

  }

  public ArrayList<ConnectMap> getConnections()
  {
    return maps;
  }

  public void clearConnections()
  {
    maps.clear();
  }

}
