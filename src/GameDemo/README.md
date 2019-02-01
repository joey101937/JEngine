# GameDemo Package
GameDemo package is a collection of simple examples designed to show how different aspects of JEngine can be used. Generally these examples will be very simple. Example projects in this package are not so much 'Games'; more like technical demos meant to display usages of different JEngine options. Keeping things simple is done so that people can follow the project's logic easily and understant how to implement JEngine features into their own project.

### Audio Demo
Demonstrates simple usage of SoundEffects. There is a global music sound effect as well as explosion sound effects that are played when the mouse is clicked. Explosions are linked to the game they are created in; note these explosion sounds do not continue playing when the scene is swapped. This is done by pressing G on the keybord.

### Sandbox Demo
Demonstrates misc features and options menu. You can enabled/disable debug and other tech options during runtime. There are two scenes which are swapped between using G. One is just the player character and a dummy character and the other is the same setup but with a number of birds spawning throughout the world at random locations. the player can click to have their character shoot a bullet that explodes and damages birds it hits. Birds will die after taking two hits. The dummy character cannot be hit. 

WASD to move the character, click to shoot. G to swap scenes, and X to pull up options menu again.

### Tank Demo
Demonstrates using subobjects and rotation based movement on a player controled character. Tank have turret subobject that plays a recoil animation when fired as well as creating a muzzle flash animated sticker visual effect. The tank may only fire again after this animation is completed. This animation is very subtle and may be overshadowed by the muzzle flash effect so you can disable that for clarity if needed.

### Space Demo
Demonstrates moving a single GameObject2 between different scenes and maintaining motion between them. Swap scenes with G key. Spaceship will constantly follow your mouse. Clicking will play explosion effect.

### SideScrollerDemo_TERRAIN
Demonstrates a simple sidesroll system using pathinglayer terrain for ground. move character left/right with A and D keys; jump with space bar.