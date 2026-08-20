package GameDemo.Minotaur.Levels;

import GameDemo.Minotaur.MinotaurArt;
import static GameDemo.Minotaur.Levels.LevelDef.Spawn.*;

/**
 * The four floors of the labyrinth, authored in tiles.
 *
 * The run is shaped as a difficulty curve rather than four variations on the same fight. Floor
 * one teaches one verb at a time in a safe place before ever using it against you. Floor two
 * takes the ground away and adds an enemy that attacks from above. Floor three adds an enemy
 * that cannot be beaten by mashing, and puts lava under the fight. Floor four is one room.
 *
 * <h3>Two kinds of danger, kept distinct</h3>
 * <b>Gaps</b> are holes in the floor: falling in costs health and puts you back on the last
 * safe ground. <b>Spike strips</b> sit on flat floor and knock you clear when touched. Mixing
 * the two — spikes at the bottom of a pit you cannot climb out of — is the one arrangement that
 * turns a hazard into an inescapable damage loop, so it does not appear here.
 *
 * A running jump clears roughly five tiles; the mid-air jump roughly doubles that. Gaps are
 * sized against those numbers, and anything wider is bridged with a ledge.
 *
 * @author Joseph
 */
public final class LevelLibrary {

    private LevelLibrary() {}

    public static final int COUNT = 4;

    public static LevelDef get(int index) {
        switch (index) {
            case 0: return floorOne();
            case 1: return floorTwo();
            case 2: return floorThree();
            default: return floorFour();
        }
    }

    /* ==================================================================
     *  I — Sunscorched Ruins
     *  Teaches move, jump, swing, dash and the armour rule, each in its own
     *  pocket of the level before the fights start layering them.
     * ================================================================== */

    private static LevelDef floorOne() {
        LevelDef d = new LevelDef("Sunscorched Ruins", "Floor I", MinotaurArt.DESERT,
                108, 23, 18, 1001L);
        d.spawnAt(3).exitAt(103);

        // ---- ground ----
        d.ground(0, 31);
        //  gap 31..36 — five tiles, clearable with a plain running jump
        d.ground(36, 23);
        d.spikes(46, 2);
        d.block(59, 16, 11, 2);                           // terrace the spitters shoot from
        d.ground(59, 20);
        d.ledge(54, 14, 4);
        d.ledge(71, 13.5, 4);
        //  gap 79..84
        d.ground(84, 24);
        d.spikes(89, 2);

        // ---- teaching ----
        d.sign(6, 14.8, "A  /  D   —   move\nSPACE   —   jump");
        d.sign(15, 14.8, "LEFT CLICK   —   swing\nKeep clicking for a three-hit chain");
        d.sign(27, 14.6, "SPACE again in mid-air\nfor a second jump");
        d.sign(43, 14.6, "SHIFT   —   dash\nYou cannot be hit while dashing");
        d.sign(65, 12.2, "RIGHT CLICK   —   heavy swing\nSlow, but it breaks armour");
        d.sign(93, 14.6, "E   —   rage roar, once the meter is full\nIt hits everything and heals you");

        // ---- fights ----
        d.enemyOnFloor(BARREL, 17).enemyOnFloor(BARREL, 18.4).enemyOnFloor(BARREL, 19.8);
        d.enemyOnFloor(IMP, 24).enemyOnFloor(IMP, 26.5);
        d.enemyOnFloor(IMP, 39).enemyOnFloor(IMP, 42).enemyOnFloor(BARREL, 44);
        d.enemy(SPITTER, 62, 14).enemy(SPITTER, 66.5, 14);
        d.enemyOnFloor(IMP, 56).enemyOnFloor(IMP, 75);
        d.enemyOnFloor(SENTINEL, 70);
        d.enemy(HEALTH, 73, 12);
        d.enemyOnFloor(BARREL, 86).enemyOnFloor(BARREL, 87.4);
        d.enemyOnFloor(IMP, 94).enemyOnFloor(IMP, 96).enemyOnFloor(IMP, 98);
        d.enemy(SPITTER, 100, 15);
        d.enemyOnFloor(SENTINEL, 99);

        // ---- dressing ----
        d.pillar(12, 18, 7, false).pillar(22, 18, 5, false).pillar(50, 18, 8, false);
        d.pillar(64, 16, 4, false).pillar(88, 18, 6, false).pillar(101, 18, 9, false);
        d.pillar(38, 18, 10, true).pillar(85, 18, 11, true);
        d.brazier(20, 18).brazier(58, 18).brazier(78, 16).brazier(102, 18);
        return d;
    }

    /* ==================================================================
     *  II — The Hollow Deep
     *  Takes the floor away. Ledges, drops, and an enemy that owns the air.
     * ================================================================== */

    private static LevelDef floorTwo() {
        LevelDef d = new LevelDef("The Hollow Deep", "Floor II", MinotaurArt.CAVERN,
                118, 23, 18, 2002L);
        d.spawnAt(3).exitAt(113);

        // Vertical rule for the whole floor: a plain jump clears three tiles, so no ledge or
        // step is ever placed more than three tiles above the surface it is reached from. The
        // mid-air jump is for crossing width, not for making required height possible.
        d.ground(0, 20);
        //  gap 20..25
        d.ground(25, 13);                                 // 25..38
        d.ledge(30, 15, 5);
        //  gap 38..44, bridged by a ledge for anyone who does not want to commit to the jump
        d.ledge(39.5, 15.5, 4);
        d.ground(44, 14);                                 // 44..58
        d.block(49, 15, 7, 3);                            // stepped shelf
        d.ledge(59.5, 15.5, 4);
        //  gap 58..64
        d.ground(64, 15);                                 // 64..79
        d.block(68, 15, 4, 3);
        d.ledge(73, 12, 5);                               // reached from the step's roof
        d.ledge(81, 15, 5);
        //  gap 79..86
        d.ground(86, 32);                                 // 86..118
        d.block(94, 15, 6, 3);
        d.ledge(104, 13, 6);                              // optional high road over the last fight
        d.spikes(90, 2);
        d.spikes(109, 2);

        d.sign(6, 14.6, "Ledges can be dropped through:\nhold S and press SPACE");
        d.sign(46, 11.4, "Bats dive in a straight line.\nStep aside, then punish the climb.");

        d.enemyOnFloor(IMP, 10).enemyOnFloor(BARREL, 13).enemyOnFloor(IMP, 15);
        d.enemy(BAT, 22, 11).enemy(BAT, 33, 9);
        d.enemy(SPITTER, 32, 12).enemy(SPITTER, 41, 11);
        d.enemy(BAT, 45, 8).enemy(BAT, 52, 10);
        d.enemyOnFloor(IMP, 46).enemyOnFloor(IMP, 53);
        d.enemy(SPITTER, 52, 13);
        d.enemy(HEALTH, 55, 13);
        d.enemyOnFloor(SENTINEL, 56);
        d.enemy(BAT, 62, 9).enemy(BAT, 70, 8.5).enemy(BAT, 77, 7);
        d.enemyOnFloor(IMP, 66).enemyOnFloor(IMP, 70).enemyOnFloor(IMP, 74);
        d.enemy(SPITTER, 74, 10);
        d.enemy(HEALTH, 84, 11);
        d.enemyOnFloor(SENTINEL, 93).enemyOnFloor(SENTINEL, 100);
        d.enemy(BAT, 96, 9).enemy(BAT, 102, 10);
        d.enemy(SPITTER, 97, 12).enemy(SPITTER, 106, 11);
        d.enemyOnFloor(IMP, 103).enemyOnFloor(IMP, 105).enemyOnFloor(IMP, 107);

        d.pillar(8, 18, 9, false).pillar(28, 18, 6, false).pillar(66, 18, 12, false);
        d.pillar(90, 18, 8, false).pillar(110, 18, 10, false);
        d.pillar(35, 18, 13, true).pillar(88, 18, 12, true);
        d.brazier(5, 18).brazier(50, 15).brazier(68, 15).brazier(94, 15).brazier(112, 18);
        return d;
    }

    /* ==================================================================
     *  III — Ember Halls
     *  Lava under the fights, sentinels in the way, and the first enemy that
     *  fights the way the player does.
     * ================================================================== */

    private static LevelDef floorThree() {
        LevelDef d = new LevelDef("Ember Halls", "Floor III", MinotaurArt.EMBER,
                122, 23, 18, 3003L);
        d.spawnAt(3).exitAt(117);

        d.ground(0, 20);
        d.lava(20, 20, 6);                                // fills the gap 20..26
        d.ledge(21.5, 15, 3.5);                           // stepping stone
        d.ground(26, 17);                                 // 26..43
        d.block(31, 15, 8, 3);
        d.lava(43, 20, 6);
        d.ledge(45, 15, 3.5);
        d.ground(49, 18);                                 // 49..67
        d.block(53, 15, 5, 3);
        d.block(61, 16, 6, 2);
        d.ledge(58, 12, 5);                               // reached from the step's roof
        d.lava(67, 20, 7);
        d.ledge(68.5, 15.5, 3.5);
        d.ledge(72.5, 13.5, 3.5);
        d.ground(74, 21);                                 // 74..95
        d.block(80, 15, 7, 3);
        d.block(89, 15, 5, 3);
        d.lava(95, 20, 5);
        d.ledge(96.5, 15, 3.5);
        d.ground(100, 22);                                // 100..122
        d.spikes(104, 2);

        d.sign(6, 14.6, "Lava hurts. The ledges do not.");
        d.sign(53, 10, "Cursed minotaurs swing like you do.\nWatch the wind-up, then dash through it.");

        d.enemyOnFloor(BARREL, 9).enemyOnFloor(IMP, 12).enemyOnFloor(IMP, 14);
        d.enemyOnFloor(SENTINEL, 17);
        d.enemy(SPITTER, 30, 13).enemy(SPITTER, 36, 13);
        d.enemyOnFloor(IMP, 33).enemyOnFloor(IMP, 35).enemyOnFloor(IMP, 41);
        d.enemy(BAT, 34, 9).enemy(BAT, 46, 8);
        d.enemy(HEALTH, 39, 13);
        d.enemyOnFloor(CURSED, 52);
        d.enemy(SPITTER, 62, 14).enemy(BAT, 60, 8);
        d.enemyOnFloor(SENTINEL, 64);
        d.enemyOnFloor(IMP, 56).enemyOnFloor(IMP, 59);
        d.enemy(HEALTH, 66, 13);
        d.enemy(BAT, 71, 9).enemy(BAT, 76, 10);
        d.enemyOnFloor(CURSED, 79).enemyOnFloor(SENTINEL, 85);
        d.enemy(SPITTER, 83, 12).enemy(SPITTER, 91, 11);
        d.enemyOnFloor(IMP, 87).enemyOnFloor(IMP, 90).enemyOnFloor(BARREL, 93);
        d.enemy(HEALTH, 101, 13);
        d.enemyOnFloor(CURSED, 107).enemyOnFloor(CURSED, 112);
        d.enemyOnFloor(SENTINEL, 110);
        d.enemy(BAT, 106, 9).enemy(BAT, 114, 9);

        d.pillar(10, 18, 10, false).pillar(46, 18, 7, false).pillar(78, 18, 11, false);
        d.pillar(102, 18, 9, false).pillar(115, 18, 12, false);
        d.pillar(29, 18, 13, true).pillar(97, 18, 12, true);
        d.brazier(8, 18).brazier(33, 15).brazier(55, 14).brazier(82, 15).brazier(114, 18);
        return d;
    }

    /* ==================================================================
     *  IV — Heart of the Labyrinth
     *  One room, two walls to slam a charging Warlord into, and just enough
     *  height to escape a shockwave.
     * ================================================================== */

    private static LevelDef floorFour() {
        LevelDef d = new LevelDef("Heart of the Labyrinth", "Floor IV", MinotaurArt.LABYRINTH,
                54, 23, 18, 4004L);
        d.spawnAt(4).exitAt(49).bossFloor();

        d.ground(0, 54);
        d.block(0, 8, 2, 10);                             // arena walls, for the charge
        d.block(52, 8, 2, 10);
        d.ledge(11, 13.5, 6);
        d.ledge(37, 13.5, 6);
        d.block(24, 15, 6, 3);                            // centre dais

        d.sign(7, 14.4, "It charges in a straight line.\nMake it hit a wall.");

        d.enemy(BOSS, 40, 12);
        d.enemy(HEALTH, 13, 12);
        d.enemy(HEALTH, 41, 12);
        d.enemyOnFloor(BARREL, 9).enemyOnFloor(BARREL, 46);

        d.pillar(3, 18, 14, false).pillar(18, 18, 13, false).pillar(34, 18, 13, false);
        d.pillar(51, 18, 14, false);
        d.brazier(9, 18).brazier(26, 15).brazier(44, 18);
        return d;
    }
}
