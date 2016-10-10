package komorebi.projsoul.entities;

import komorebi.projsoul.engine.Animation;

public class Sierra extends Player {

public Sierra(float x, float y) {
    
    super(x,y);
    
    character = Characters.SIERRA;
    
    upAni =    new Animation(6, 8, 12);
    downAni =  new Animation(6, 8, 12);
    leftAni =  new Animation(6, 8, 12);
    rightAni = new Animation(6, 8, 12);

    hurtUpAni = new Animation(2,8,26,29,12);
    hurtDownAni = new Animation(2,8,28,31,12);
    hurtRightAni = new Animation(2,8,24,31,12);
    hurtLeftAni = new Animation(2,8,24,31,12);

    downAni.add(88,355,15,32);
    downAni.add(108,355,15,32);
    downAni.add(128,356,16,31);
    downAni.add(149,355,15,32);
    downAni.add(169,355,15,32);
    downAni.add(189,356,16,31);

    upAni.add(6,131,21,32);
    upAni.add(30,131,24,32);
    upAni.add(58,132,23,31);
    upAni.add(84,131,21,32);
    upAni.add(109,131,24,32);
    upAni.add(137,132,23,31);

    rightAni.add(8,91,16,32,0,true);
    rightAni.add(30,91,16,32,0,true);
    rightAni.add(52,92,22,31,0,true);
    rightAni.add(78,92,20,32,0,true);
    rightAni.add(102,92,16,32,0,true);
    rightAni.add(123,93,18,31,0,true);

    leftAni.add(8,91,16,32);
    leftAni.add(30,91,16,32);
    leftAni.add(52,92,22,31);
    leftAni.add(78,92,20,32);
    leftAni.add(102,92,16,32);
    leftAni.add(123,93,18,31);

    hurtUpAni.add(374,227);
    hurtUpAni.add(376, 263);

    hurtDownAni.add(314, 224);
    hurtDownAni.add(314, 261);

    hurtRightAni.add(346,225,true);
    hurtRightAni.add(347,261,true);

    hurtLeftAni.add(346,225);
    hurtLeftAni.add(347,261);
    
  }
}
