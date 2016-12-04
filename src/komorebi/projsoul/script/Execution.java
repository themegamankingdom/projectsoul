/**
 * Execution.java Jun 11, 2016, 11:58:10 AM
 */
package komorebi.projsoul.script;

import komorebi.projsoul.audio.AudioHandler;
import komorebi.projsoul.audio.Song;
import komorebi.projsoul.engine.Main;
import komorebi.projsoul.engine.ThreadHandler;
import komorebi.projsoul.engine.ThreadHandler.NewThread;
import komorebi.projsoul.entities.Face;
import komorebi.projsoul.entities.NPC;
import komorebi.projsoul.entities.NPCType;
import komorebi.projsoul.gameplay.Item.Items;
import komorebi.projsoul.map.Map;
import komorebi.projsoul.map.TileList;
import komorebi.projsoul.script.Task.TaskWithBoolean;
import komorebi.projsoul.script.Task.TaskWithBranch;
import komorebi.projsoul.script.Task.TaskWithBranches;
import komorebi.projsoul.script.Task.TaskWithLocation;
import komorebi.projsoul.script.Task.TaskWithNumber;
import komorebi.projsoul.script.Task.TaskWithNumberAndLocation;
import komorebi.projsoul.script.Task.TaskWithString;
import komorebi.projsoul.script.Task.TaskWithStringArray;
import komorebi.projsoul.script.Task.TaskWithTask;
import komorebi.projsoul.states.Game;

/**
 * 
 * @author Andrew Faulkenberry
 * @version 
 */
public class Execution implements Runnable {

  private Lock lock = new Lock();

  private InstructionList list;
  private NPC npc;

  private boolean isBlank;
  private boolean loop;
  /**
   * Creates an execution object which will begin on the "Main" branch
   * @param myNpc The NPC the execution should affect
   * @param toDo The list of instructions to be executed on the new thread
   */
  public Execution(NPC myNpc, InstructionList toDo)
  {


    list = toDo;
    npc = myNpc;


    isBlank = false;
    loop = false;
  }

  /**
   * Creates an execution object which will begin on the "Main" branch
   * @param myNpc The NPC the execution should affect
   * @param toDo The list of instructions to be executed on the new thread
   * @param loop Whether the script should automatically loop
   */
  public Execution(NPC myNpc, InstructionList toDo, boolean loop)
  {

    list = toDo;
    npc = myNpc;

    isBlank = false;

    this.loop = loop;
  }


  /**
   * Creates an execution object
   * @param myNpc The NPC the execution should affect
   * @param toDo The list of instructions to be executed on the new thread
   * @param firstBranch the branch on which the execution will begin
   */
  public Execution(NPC myNpc, InstructionList toDo, String firstBranch)
  {
    list = toDo;
    npc = myNpc;

    isBlank = false;
  }

  @Override
  public void run() {

    if (!isBlank)
    {
      if (loop)
      {
        while (true) 
        {
          execute(); 
        }
      }
      else 
      {
        execute();
      }
    }
  }
  public Lock getLock()
  {
    return lock;
  }


  /**
   * Checks whether the Thread has been interrupted before continuing
   * @return True if the thread can continue, false if it should stop
   */
  public boolean check()
  {
    try
    {     
      NewThread newThread = (NewThread) Thread.currentThread();
      return !newThread.flagged();

    } catch (ClassCastException e)
    {
      //DEBUG Thread names
      System.out.println(Thread.currentThread().getName());
    }
    return false;
  }

  /**
   * Executes the entire ArrayList of Tasks
   */
  public void execute() {

    for (int j = 0; j < list.getInstructions().size(); j++)
    {
      if (!check())
      {
        ThreadHandler.giveLock((NewThread) Thread.currentThread(), lock);
        lock.pauseThread();
      }

      execute(list.getInstructions().get(j));


    }

  }

  /**
   * Executes a single task
   * @param task The task to be executed
   */
  public void execute(Task task)
  {        
    TaskWithNumber taskNum;
    TaskWithString taskStr;
    TaskWithBoolean taskBool;
    TaskWithTask taskTask;
    TaskWithLocation taskLoc;
    TaskWithNumberAndLocation taskNumLoc;
    TaskWithStringArray taskStrArr;
    TaskWithBranch taskBr;
    TaskWithBranches taskBrs;
    Task nextTask;

    boolean run;
   
    switch (task.getInstruction())
    {
      case WALK_DOWN:
        npc.walk(Face.DOWN, 1, lock);
        break;
      case WALK_LEFT:
        npc.walk(Face.LEFT, 1, lock);
        break;
      case WALK_RIGHT:
        npc.walk(Face.RIGHT, 1, lock);
        break;
      case WALK_UP:
        npc.walk(Face.UP, 1, lock);
        break;
      case TURN_LEFT:
        npc.turn(Face.LEFT);
        break;
      case TURN_RIGHT:
        npc.turn(Face.RIGHT);
        break;
      case TURN_UP:
        npc.turn(Face.UP);
        break;
      case TURN_DOWN:
        npc.turn(Face.DOWN);
        break;
      case WAIT:
        taskNum = (TaskWithNumber) task;
        npc.pause(taskNum.getNumber(), lock);
        break;
      case FREEZE:
        taskNum = (TaskWithNumber) task;
        Main.getGame().pause(taskNum.getNumber(), lock);
        break;
      case JOG_LEFT:
        npc.jog(Face.LEFT, 1, lock);
        break;
      case JOG_RIGHT:
        npc.jog(Face.RIGHT, 1, lock);
        break;
      case JOG_UP:
        npc.jog(Face.UP, 1, lock);
        break;
      case JOG_DOWN:
        npc.jog(Face.DOWN, 1, lock);
        break;
      case CHANGE_SPRITE:
        taskStr = (TaskWithString) task;
        npc.setNPCType(NPCType.toEnum(taskStr.getString()));
        break;
      case SET_LOCATION:
        taskLoc = (TaskWithLocation) task;
        npc.setTileLocation(taskLoc.getX(), taskLoc.getY());
        break;
      case LOCK:
        Map.getPlayer().stop();
        Map.getPlayer().lock();
        break;
      case UNLOCK:
        Map.getPlayer().unlock();
        break;
      case SAY:
        taskStr = (TaskWithString) task;
        taskStr.setString(taskStr.getString().replace("@me", 
            Map.getPlayer().getCharacter().getName()));
                
        npc.say(taskStr.getString(), lock);
        
        taskStr.setString(taskStr.getString().replace(Map.getPlayer().getCharacter().getName(), 
            "@me"));
        break;
      case ASK:
        taskStrArr = (TaskWithStringArray) task;
        
        String[] oldStrings = taskStrArr.getStrings();
        String[] newStrings = new String[oldStrings.length];
        
        for (int i = 0; i < oldStrings.length; i++)
        {
          newStrings[i] = oldStrings[i].replace("@me", 
              Map.getPlayer().getCharacter().getName());
        }
        
        String answer = npc.ask(newStrings, this, lock);
        
        for (String s: taskStrArr.getStrings())
        {
          s.replace(Map.getPlayer().getCharacter().getName(), "@me");
        }        
        
        (new Execution(npc, taskStrArr.getTask(answer).getBranch())).run();
         break;
      case RUN_BRANCH:
        taskBr = (TaskWithBranch) task;
        (new Execution(npc, taskBr.getBranch())).run();
        break;
      case RUN_BRANCHES:
        taskBrs = (TaskWithBranches) task;
        for (TaskWithBranch oneTask: taskBrs.getBranches())
        {
          oneTask.getBranch().setTaskWithBranches(taskBrs);
          ThreadHandler.newThread((new NewThread(new Execution(npc, 
              oneTask.getBranch()))));
        }
        taskBrs.setLockandLock(lock);
        break;
      case FADE_OUT:
        Fader.fadeOut(lock);
        break;
      case FADE_IN:
        Fader.fadeIn(lock);
        break;
      case RUN_SCRIPT:
        taskStr = (TaskWithString) task;
        AreaScript script = Game.getMap().getScript(taskStr.getString());
        script.run();
        break;
      case LOAD_MAP:
        taskStr = (TaskWithString) task;
        Game.setMap(new Map("res/maps/" + taskStr.getString() + ".map"));
        break;
      case RETILE:
        taskNumLoc = (TaskWithNumberAndLocation) task;
        Game.getMap().setTile(taskNumLoc.getNumber(), 
            taskNumLoc.getX(), taskNumLoc.getY());
        break;
      case CLYDE_WALK_LEFT:
        Map.getPlayer().walk(Face.LEFT, 1, lock);
        break;
      case CLYDE_WALK_RIGHT:
        Map.getPlayer().walk(Face.RIGHT, 1, lock);
        break;
      case CLYDE_WALK_UP:
        Map.getPlayer().walk(Face.UP, 1, lock);
        break;
      case CLYDE_WALK_DOWN:
        Map.getPlayer().walk(Face.DOWN, 1, lock);
        break;
      case CLYDE_PAUSE:
        taskNum = (TaskWithNumber) task;
        Map.getPlayer().pause(taskNum.getNumber(), lock);
        break;
      case SIMUL_RUN_BRANCH:
        taskBr = (TaskWithBranch) task;
        Execution ex = new Execution(npc, taskBr.getBranch());

        ThreadHandler.newThread(new NewThread(ex));
        break;
      case CLYDE_TURN_LEFT:
        Map.getPlayer().turn(Face.LEFT);
        break;
      case CLYDE_TURN_RIGHT:
        Map.getPlayer().turn(Face.RIGHT);
        break;
      case CLYDE_TURN_UP:
        Map.getPlayer().turn(Face.UP);
        break;
      case CLYDE_TURN_DOWN:
        Map.getPlayer().turn(Face.DOWN);
        break;
      case ALIGN_LEFT:
        Map.getPlayer().align(Face.LEFT, lock);
        break;
      case ALIGN_RIGHT:
        Map.getPlayer().align(Face.RIGHT, lock);
        break;
      case ALIGN_DOWN:
        Map.getPlayer().align(Face.DOWN, lock);
        break;
      case ALIGN_UP:
        Map.getPlayer().align(Face.UP, lock);
        break;
      case ALIGN:
        Map.getPlayer().align(npc, lock);
        break;
      case PLAY_SONG:
        taskStr = (TaskWithString) task;
        AudioHandler.play(Song.get(taskStr.getString()));
        break;
      case GO_TO:
        taskLoc = (TaskWithLocation) task;
        npc.goTo(true, taskLoc.getX(), lock);
        npc.goTo(false, taskLoc.getY(), lock);
        break;
      case CLYDE_GO_TO:
        taskLoc = (TaskWithLocation) task;
        Map.getPlayer().goToPixX(taskLoc.getX()*16, lock);
        Map.getPlayer().goToPixY(taskLoc.getY()*16, lock);
        break;
      case STOP_SONG:
        AudioHandler.stop();
        break;
      case GIVE_ITEM:
        Items item;
        taskStr = (TaskWithString) task;

        Main.getGame().receiveItem(item = Items.getItem(taskStr.getString()));
        npc.say("You received " + item.getPrintString() + "!", lock);
        break;
      case PAY_MONEY:
        taskNum = (TaskWithNumber) task;
        Main.getGame().giveMoney(taskNum.getNumber());
        break;
      case PAY_CONFIDENCE:
        taskNum = (TaskWithNumber) task;
        Main.getGame().giveConfidence(taskNum.getNumber());
        break;
      case END:
        if (npc != null)
        {
          npc.disengage();
        }

        reset();

        ThreadHandler.remove((NewThread) Thread.currentThread());
        break;
      case IF_MONEY:
        taskTask = (TaskWithTask) task;
        nextTask = getNextTask(task);

        run = Main.getGame().getMoney() > taskTask.getPredicate();
        if (taskTask.isReversed()) 
        {
          run = !run;
        }

        if (run)
        {
          execute(taskTask.getTask());

          if (nextTask != null)
          {
            if (nextTask.getInstruction() == Instructions.ELSE)
            {
              taskBool = (TaskWithBoolean) nextTask;
              taskBool.setIfTrue(true);
            }
          }
        }
        break;
      case IF_CONFIDENCE:
        taskTask = (TaskWithTask) task;

        nextTask = getNextTask(task);

        run = Main.getGame().getConfidence() > taskTask.getPredicate();
        if (taskTask.isReversed())
        {
          run = !run;
        }

        if (run)
        {
          execute(taskTask.getTask());

          if (nextTask != null)
          {
            if (nextTask.getInstruction() == Instructions.ELSE)
            {
              taskBool = (TaskWithBoolean) nextTask;
              taskBool.setIfTrue(true);
            }
          }
        }
        break;
      case IF_BOOLEAN:
        taskTask = (TaskWithTask) task;
        nextTask = getNextTask(task);

        run = Main.getGame().checkFlag(taskTask.getPredicate());
        if (taskTask.isReversed()) 
        {  
          run = !run;
        }
        if (run)
        {
          execute(taskTask.getTask());

          if (nextTask != null)
          {
            if (nextTask.getInstruction() == Instructions.ELSE)
            {
              taskBool = (TaskWithBoolean) nextTask;
              taskBool.setIfTrue(true);
            }
          }
        }
        break;
      case IF_CHAR:
        taskTask = (TaskWithTask) task;
        nextTask = getNextTask(task);
        
        System.out.println(Map.currentPlayer());
        run = Map.currentPlayer() == taskTask.getCharacter();
        if (taskTask.isReversed())
        {
          run = !run;
        }
        
        if (run)
        {
          System.out.println("Run if");
          execute(taskTask.getTask());
          
          if (nextTask != null)
          {
            if (nextTask.getInstruction() == Instructions.ELSE)
            {
              taskBool = (TaskWithBoolean) nextTask;
              taskBool.setIfTrue(true);
            }
          }
        } else
        {
          if (nextTask != null)
          {
            if (nextTask.getInstruction() == Instructions.ELSE)
            {
              taskBool = (TaskWithBoolean) nextTask;
              taskBool.setIfTrue(false);
            }
          }
        }
        
        break;
      case ELSE:
        taskBool = (TaskWithBoolean) task;
        
        if (!taskBool.ifTrue())
        {
          execute(taskBool.getTask());
          taskBool.setIfTrue(false);
        }
        break;
      case FLAG_BOOLEAN:
        taskNum = (TaskWithNumber) task;
        Main.getGame().setFlag(taskNum.getNumber(), true);
        break;
      case BLOCK:
        taskLoc = (TaskWithLocation) task;
        Game.getMap().setCollision(taskLoc.getX(), taskLoc.getY(), false);
        break;
      case SYNC:
        if (list.getSuperTask() == null)
        {
          System.out.println("The \"sync\" keyword cannot be used in a thread" +
              " without a parent thread.");
        } else
        {
          list.getSuperTask().sync(list);
        }

        break;
      case UNBLOCK:
        taskLoc = (TaskWithLocation) task;
        Game.getMap().setCollision(taskLoc.getX(), taskLoc.getY(), true);
        break;
      default:
        break;

    }

  }

  public void setLoopable(boolean b)
  {
    loop = b;
  }

  public Task getNextTask(Task task)
  {
    int index = list.getInstructions().indexOf(task);
    if (index == list.getInstructions().size() - 1)
      return null;
    return list.getInstructions().get(index+1);
  }

  public void reset()
  {
    for (Task t: list.getInstructions())
    {
      if (t instanceof TaskWithBranches)
      {
        ((TaskWithBranches) t).reset();
      }
    }
  }


}
