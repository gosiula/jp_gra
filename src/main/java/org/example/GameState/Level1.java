package org.example.GameState;
import java.awt.*;
import org.example.Entity.*;
import org.example.Main.GamePanel;
import org.example.TileMap.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.example.Entity.Ghost;
import static org.example.Music.Music.*;

// INITIALIZATION, UPDATING AND DRAWING OF LEVEL 1 STATE (BACKGROUND, PLAYER, ENEMIES, EXPLOSIONS, HUD, SOUNDS)
public class Level1 extends GameState {
    // adding the back buffer
    private final BufferedImage backBuffer;
    // tilemap
    private TileMap tileMap;

    // background
    private Background bg;
    private Background bg2;

    // player
    private Player player;

    // ArrayList of the enemies
    private ArrayList<Enemy> enemies;

    // ArrayList of the explosions
    private ArrayList<Explosion> explosions;

    // hud with the information about health, fireballs and earned points
    private HUD hud;

    // final points
    private static int final_points;

    // arraylists with tomatoes and avocados
    private ArrayList<Tomato> tomatoList;
    private ArrayList<Avocado> avocadoList;

    // egg
    private Egg egg;

    // the boolean saying if the q button was pressed
    boolean qKeyPressed = false;

    // Level1 constructor
    public Level1(GameStateManager gsm) {
        this.gsm = gsm;
        initialization();

        // initialization of the back buffer
        backBuffer = new BufferedImage(GamePanel.WIDTH, GamePanel.HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    // initialization of level 1
    public void initialization() {
        tileMap = new TileMap(30);

        // initialization of the tiles and the map
        tileMap.loadTiles("/TileSets/tile_set.png");
        tileMap.loadMap("/Maps/map.txt");
        // adding enemies and setting their position
        addingEnemies();
        addingTomatoes();
        addingAvocados();

        // setting position of the tileMap
        tileMap.setPosition(0, 0);

        // setting tween which makes the screen movement smoother
        tileMap.setTween(1);

        // initialization of the background
        bg = new Background("/Backgrounds/background_sky.png", 0.1);
        bg2 = new Background("/Backgrounds/background_mountains.png", 0);

        // initialization of the egg
        egg  = new Egg(tileMap);

        // initialization of the player
        player = new Player(tileMap);

        // position of the player
        player.setPosition(100, 100);

        // initialization of explosions
        explosions = new ArrayList<>();

        // initialization of the hud with the information about health, fireballs and earned points
        hud = new HUD(player, gsm);
    }

    private void checkTomatoCollisions() {
        for (int i = 0; i < tomatoList.size(); i++) {
            Tomato tomato = tomatoList.get(i);
            if (player.intersects(tomato)) {
                scoreSound();

                // removing the collected tomato
                tomatoList.remove(i);
                i--;

                // adding 2 points for the tomato
                player.addPoints(2);
            }
        }
    }

    private void checkAvocadoCollisions() {
        for (int i = 0; i < avocadoList.size(); i++) {
            Avocado avocado = avocadoList.get(i);
            if (player.intersects(avocado)) {
                scoreSound();

                // removing the collected avocado
                avocadoList.remove(i);
                i--;

                // adding 4 points for the avocado
                player.addPoints(4);
            }
        }
    }

    private void addingTomatoes() {
        tomatoList = new ArrayList<>();
        Tomato tomato;
        // locations of the tomatoes
        Point[] points = new Point[] {
                new Point(365, 136),
                new Point(710, 256),
                new Point(1160, 136),
                new Point(1400, 166),
                new Point(1905, 196)
        };

        // adding tomatoes to the chosen locations
        for (Point point : points) {
            tomato = new Tomato(tileMap);
            tomato.setPosition(point.x, point.y);
            tomatoList.add(tomato);
        }
    }

    private void addingAvocados() {
        avocadoList = new ArrayList<>();
        Avocado avocado;
        // locations of the avocados
        Point[] points = new Point[] {
                new Point(530, 134),
                new Point(975, 254),
                new Point(1650, 64),
                new Point(2120, 224),
                new Point(2455, 254)
        };

        // adding avocados to the chosen locations
        for (Point point : points) {
            avocado = new Avocado(tileMap);
            avocado.setPosition(point.x, point.y);
            avocadoList.add(avocado);
        }
    }


    private void addingEnemies() {
        // initialization of the enemies
        enemies = new ArrayList<>();

        Ghost g;

        // locations of the ghosts
        Point[] points = new Point[] {
                new Point(780, 248),
                new Point(990, 248),
                new Point(1200, 248),
                new Point(1440, 98),
                new Point(1920, 148),
                new Point(2100, 198),
                new Point(2160, 198),
                new Point(2310, 248),
                new Point(2430, 248),
                new Point(2520, 248)
        };

        // adding ghosts to the chosen locations
        for (Point point : points) {
            g = new Ghost(tileMap);
            g.setPosition(point.x, point.y);
            enemies.add(g);
        }
    }

    public static int getFinalPoints() {
        return final_points;
    }

    // updating level 1
    public void update() {
        // getting final points of the player
        final_points = player.getPoints();

        // updating the player
        player.update();
        tileMap.setPosition(
                GamePanel.WIDTH / (float)2 - player.getx(),
                GamePanel.HEIGHT / (float)2 - player.gety()
        );

        // setting background
        bg.setPosition(tileMap.getx(), tileMap.gety());
        bg2.setPosition(tileMap.getx(), tileMap.gety());

        // egg position
        egg.setPosition(3070,198);

        // checking is the enemies are attacked by the player
        player.checkAttack(enemies);

        // updating all enemies
        for(int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            e.update();

            // if the enemy is dead, it is removed from the screen, so it causes an explosion
            if(e.isDead()) {
                enemies.remove(i);
                i--;
                explosions.add(new Explosion(e.getx(), e.gety()));
                player.addPoints(10); // Dodaj punkty za zniszczenie potwora
            }
        }

        // updating explosions
        for(int i = 0; i < explosions.size(); i++) {
            explosions.get(i).update();
            // removing the explosion after it blows up
            if(explosions.get(i).shouldRemove()) {
                explosions.remove(i);
                i--;
            }
        }

        // checking if the player collected any tomatoes
        checkTomatoCollisions();

        // checking if the player collected any avocados
        checkAvocadoCollisions();

        // checking if the player is dead
        if (player.isDead()) {
            player.setPosition(100,100);

            // emptying the lists
            enemies = new ArrayList<>();
            avocadoList = new ArrayList<>();
            tomatoList = new ArrayList<>();

            // stopping the threads
            GamePanel.stopThreads();

            stopBgMusic();
            gameOverSounds();

            // going to game over state
            gsm.setState(GameStateManager.GAME_OVER_STATE);

            // making the player alive again to be ready for the next game
            player.isAlive(false);

            // starting the threads again
            GamePanel.startThreads();
        }
        else if(player.getx() > 3000 && !player.isDead() && isQKeyPressed()) {
            player.setPosition(100, 100);

            // emptying the lists
            enemies = new ArrayList<>();
            avocadoList = new ArrayList<>();
            tomatoList = new ArrayList<>();

            // stopping the threads
            GamePanel.stopThreads();
            stopBgMusic();
            congratulationsSound();

            // going to the congratulations state
            gsm.setState(GameStateManager.CONGRATULATIONS_STATE);

            // starting the threads again
            GamePanel.startThreads();
        }
    }

    private boolean isQKeyPressed() {
        return qKeyPressed;
    }

    // drawing level 1
    public void draw(Graphics2D g) {
        // create a back buffer graphics context
        Graphics2D backBufferGraphics = (Graphics2D) backBuffer.getGraphics();

        // drawing on the back buffer
        backBufferGraphics.setColor(Color.BLACK);
        backBufferGraphics.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        // drawing the background on the back buffer
        bg.draw(backBufferGraphics);
        bg2.draw(backBufferGraphics);

        // drawing the tilemap on the back buffer
        tileMap.draw(backBufferGraphics);

        // drawing the player on the back buffer
        player.draw(backBufferGraphics);

        // drawing th egg on the back buffer
        egg.draw(backBufferGraphics);

        // drawing the tomatoes on the back buffer
        for (Tomato tomato : tomatoList) {
            tomato.draw(backBufferGraphics);
        }

        // drawing the avocados on the back buffer
        for (Avocado avocado : avocadoList) {
            avocado.draw(backBufferGraphics);
        }

        // drawing the enemies on the back buffer
        for (Enemy enemy : enemies) {
            enemy.draw(backBufferGraphics);
        }

        // drawing the explosions on the back buffer
        for (Explosion explosion : explosions) {
            explosion.setMapPosition((int) tileMap.getx(), (int) tileMap.gety());
            explosion.draw(backBufferGraphics);
        }

        // drawing the hud on the back buffer
        hud.draw(backBufferGraphics);

        // draw the back buffer onto the main graphics context
        g.drawImage(backBuffer, 0, 0, null);
    }

    // handling the keys pressed in level 1 state
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_Q) {
            qKeyPressed = true;
        }
        switch (k) {
            case KeyEvent.VK_LEFT -> player.setLeft(true);
            case KeyEvent.VK_RIGHT -> player.setRight(true);
            case KeyEvent.VK_UP -> player.setJumping(true);
            case KeyEvent.VK_SPACE -> player.setGliding(true);
            case KeyEvent.VK_S -> player.setPunching();
            case KeyEvent.VK_F -> player.setFiring();
            default -> {
            }
        }
    }

    // handling the keys released in level 1 state
    public void keyReleased(int k) {
        if (k == KeyEvent.VK_Q) {
            qKeyPressed = false;
        }
        switch (k) {
            case KeyEvent.VK_LEFT -> player.setLeft(false);
            case KeyEvent.VK_RIGHT -> player.setRight(false);
            case KeyEvent.VK_UP -> player.setJumping(false);
            case KeyEvent.VK_SPACE -> player.setGliding(false);
            default -> {
            }
        }
    }
}
