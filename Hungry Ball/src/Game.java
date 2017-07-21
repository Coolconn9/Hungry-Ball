import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Game extends JFrame implements KeyListener {

    //window vars
    private final int MAX_FPS;
    private final int WIDTH;
    private final int HEIGHT;

    //button stuff
    private Rectangle b1;
    private Rectangle b2;
    private byte buttonState = 0;

    //game states
    public enum GAME_STATES {
        MENU,
        GAME,
        SCORE
    }

    private GAME_STATES GameState = GAME_STATES.MENU;

    private BufferedImage icon = makeImage("iconImage.png");
    private BufferedImage logo = makeImage("logo.png");
    private BufferedImage lightbulb = makeImage("lightbulb.png");
    private BufferedImage textBox = makeImage("speechbubble.png");
    private boolean showText;

    //double buffer
    private BufferStrategy strategy;

    //loop variables
    private boolean isRunning = true;
    private long rest = 0;
    private Random random = new Random();
    private boolean pointsAreSpawned = false;
    private boolean foodIsSpawned = false;
    private boolean enemyGone = false;
    private boolean food1Gone = false;
    private boolean food2Gone = false;
    private boolean food3Gone = false;
    private boolean food4Gone = false;
    private boolean enemyCloseToLeft = false;
    private boolean enemyCloseToRight = false;
    private boolean enemyCloseToTop = false;
    private boolean enemyCloseToBottom = false;
    private boolean food1CloseToLeft = false;
    private boolean food1CloseToRight = false;
    private boolean food1CloseToTop = false;
    private boolean food1CloseToBottom = false;
    private boolean food2CloseToLeft = false;
    private boolean food2CloseToRight = false;
    private boolean food2CloseToTop = false;
    private boolean food2CloseToBottom = false;
    private boolean food3CloseToLeft = false;
    private boolean food3CloseToRight = false;
    private boolean food3CloseToTop = false;
    private boolean food3CloseToBottom = false;
    private boolean food4CloseToLeft = false;
    private boolean food4CloseToRight = false;
    private boolean food4CloseToTop = false;
    private boolean food4CloseToBottom = false;
    private boolean secondEnemyHasSpawned = false;
    private boolean isTraveling = false;
    private boolean hadPowerup = false;
    private boolean isInvincible = false;
    private boolean updatedHighScore = false;
    private ArrayList<Point> pointsList = new ArrayList<>();
    private ArrayList<Integer> keys = new ArrayList<>();

    private void handleKeys() {
        for (int key : keys) {
            switch (key) {
                case KeyEvent.VK_RIGHT:
                    angleSize = 0.0625f;
                    pressed = true;
                    break;
                case KeyEvent.VK_LEFT:
                    angleSize = -0.0625f;
                    pressed = true;
                    break;
            }
        }
    }

    //timing variables
    private float dt;
    private long lastFrame;
    private long startFrame;
    private float timer;
    private float powerupTimer;
    private float enemyEscapeTimer;
    private float food1EscapeTimer;
    private float food2EscapeTimer;
    private float food3EscapeTimer;
    private float food4EscapeTimer;
    private float invincibleTimer;

    //sprite1 variables
    private float x = 50;
    private float y = 100;
    private float angleSize = 0;
    private double angle = 0;
    private int score;
    private boolean hasPowerup = false;
    private Preferences highScorePrefs;
    private int highScore = 0;

    //enemy1 variables
    private float x2 = 1500;
    private float y2 = 700;
    private float x3;
    private float y3;
    private float x4;
    private float y4;
    private float x5;
    private float y5;
    private float x6;
    private float y6;
    private float x7 = 1600;
    private float y7 = 200;
    private float startX;
    private float startY;
    private float mx;
    private float my;
    private float s1;
    private float c1;
    private float s2;
    private float c2;
    private float newX;
    private float newY;
    private float dx;
    private float dy;
    private float abs;

    //point variables
    private int randomY = 0;
    private int randomX = 0;
    private BufferedImage coin = makeImage("coin.png");

    private boolean pressed = false;

    private Game(int width, int height, int fps) {
        super("Hungry Ball");
        this.MAX_FPS = fps;
        this.WIDTH = width;
        this.HEIGHT = height;
    }

    private void init() {
        //initialize JFrame
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ML ml = new ML();
        addMouseListener(ml);
        addMouseMotionListener(ml);
        b1 = new Rectangle(400, 600, 500, 300);
        b2 = new Rectangle(1000, 600, 500, 300);

        setIconImage(icon);

        setBounds(0, 0, WIDTH, HEIGHT);

        lastFrame = System.currentTimeMillis();

        setResizable(false);
        validate();
        setVisible(true);

        //create double buffer strategy
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        addKeyListener(this);
        setFocusable(true);

        timer = 0;
        powerupTimer = 0;
        enemyEscapeTimer = 0;
        food1EscapeTimer = 0;
        food2EscapeTimer = 0;
        food3EscapeTimer = 0;
        food4EscapeTimer = 0;
        invincibleTimer = 0;

        highScorePrefs = Preferences.userRoot().node(this.getClass().getName());
        highScore = highScorePrefs.getInt("highScore", 0);
    }

    private void update() {

        handleKeys();

        if (GameState == GAME_STATES.GAME) {

            //update sprite
            if (!hasPowerup) {
                x += 10 * Math.cos(angle);
                y += 10 * Math.sin(angle);
            } else {
                x += 12 * Math.cos(angle);
                y += 12 * Math.sin(angle);
            }
            if (!hasPowerup) {
                if (x < 50) x = 50;
                if (x > WIDTH - 125) x = WIDTH - 125;
                if (y < 70) y = 70;
                if (y > HEIGHT - 50) y = HEIGHT - 50;
            } else {
                if (x < (float) 62.5) x = (float) 62.5;
                if (x > (float) (WIDTH - 137.5)) x = (float) (WIDTH - 137.5);
                if (y < (float) 82.5) y = (float) 82.5;
                if (y > (float) (HEIGHT - 62.5)) y = (float) (HEIGHT - 62.5);
            }

            //update second enemy
            if (secondEnemyHasSpawned) {

                if (!isTraveling) {
                    startX = x7;
                    startY = y7;
                    mx = (x + x7) / 2;
                    my = (y + y7) / 2;
                    s1 = (x - x7) / (y7 - y);
                    c1 = my - mx * s1;
                    s2 = (float) (Math.tan(angle));
                    c2 = y - x * s2;
                    newX = (c1 - c2) / (s2 - s1);
                    newY = s2 * newX + c2;
                    dx = newX - x7;
                    dy = newY - y7;
                    abs = (float) (Math.sqrt(dx * dx + dy * dy));

                    isTraveling = true;
                } else {
                    x7 += dx * 10 / abs;
                    y7 += dy * 10 / abs;
                }
                if (x7 < 62.5) {
                    x7 = (float)62.5;
                    isTraveling = false;
                }
                if (x7 > WIDTH - 150) {
                    x7 = WIDTH - 150;
                    isTraveling = false;
                }
                if (y7 < 82.5) {
                    y7 = (float)82.5;
                    isTraveling = false;
                }
                if (y7 > HEIGHT - 62.5) {
                    y7 = (float)(HEIGHT - 62.5);
                    isTraveling = false;
                }

                if (x7 > newX && x7 > startX || x7 < newX && x7 < startX) {
                    isTraveling = false;
                }
                if (y7 > newY && y7 > startY || y7 < newY && y7 < startY) {
                    isTraveling = false;
                }
            }

            //update enemies and food
            if(hasPowerup) {
                if (x2 < 55) enemyCloseToLeft = true;
                if (x2 > WIDTH - 125) enemyCloseToRight = true;
                if (y2 < 75) enemyCloseToTop = true;
                if (y2 > HEIGHT - 55) enemyCloseToBottom = true;

                if (x3 < 55) food1CloseToLeft = true;
                if (x3 > WIDTH - 125) food1CloseToRight = true;
                if (y3 < 75) food1CloseToTop = true;
                if (y3 > HEIGHT - 55) food1CloseToBottom = true;

                if (x4 < 55) food2CloseToLeft = true;
                if (x4 > WIDTH - 125) food2CloseToRight = true;
                if (y4 < 75) food2CloseToTop = true;
                if (y4 > HEIGHT - 55) food2CloseToBottom = true;

                if (x5 < 55) food3CloseToLeft = true;
                if (x5 > WIDTH - 125) food3CloseToRight = true;
                if (y5 < 75) food3CloseToTop = true;
                if (y5 > HEIGHT - 55) food3CloseToBottom = true;

                if (x6 < 55) food4CloseToLeft = true;
                if (x6 > WIDTH - 125) food4CloseToRight = true;
                if (y6 < 75) food4CloseToTop = true;
                if (y6 > HEIGHT - 55) food4CloseToBottom = true;

                if (enemyCloseToLeft) {
                    enemyEscapeTimer += dt;
                    if (enemyEscapeTimer <= 1) x2 += 7;
                    else {
                        enemyCloseToLeft = false;
                        enemyEscapeTimer %= 1;
                    }
                }
                if (enemyCloseToRight) {
                    enemyEscapeTimer += dt;
                    if (enemyEscapeTimer <= 1) x2 -= 7;
                    else {
                        enemyCloseToRight = false;
                        enemyEscapeTimer %= 1;
                    }
                }
                if (enemyCloseToTop) {
                    enemyEscapeTimer += dt;
                    if (enemyEscapeTimer <= 1) y2 += 7;
                    else {
                        enemyCloseToTop = false;
                        enemyEscapeTimer %= 1;
                    }
                }
                if (enemyCloseToBottom) {
                    enemyEscapeTimer += dt;
                    if (enemyEscapeTimer <= 1) y2 -= 7;
                    else {
                        enemyCloseToBottom = false;
                        enemyEscapeTimer %= 1;
                    }
                }

                if (food1CloseToLeft) {
                    food1EscapeTimer += dt;
                    if (food1EscapeTimer <= 1) x3 += 7;
                    else {
                        food1CloseToLeft = false;
                        food1EscapeTimer %= 1;
                    }
                }
                if (food1CloseToRight) {
                    food1EscapeTimer += dt;
                    if (food1EscapeTimer <= 1) x3 -= 7;
                    else {
                        food1CloseToRight = false;
                        food1EscapeTimer %= 1;
                    }
                }
                if (food1CloseToTop) {
                    food1EscapeTimer += dt;
                    if (food1EscapeTimer <= 1) y3 += 7;
                    else {
                        food1CloseToTop = false;
                        food1EscapeTimer %= 1;
                    }
                }
                if (food1CloseToBottom) {
                    food1EscapeTimer += dt;
                    if (food1EscapeTimer <= 1) y3 -= 7;
                    else {
                        food1CloseToBottom = false;
                        food1EscapeTimer %= 1;
                    }
                }

                if (food2CloseToLeft) {
                    food2EscapeTimer += dt;
                    if (food2EscapeTimer <= 1) x4 += 7;
                    else {
                        food2CloseToLeft = false;
                        food2EscapeTimer %= 1;
                    }
                }
                if (food2CloseToRight) {
                    food2EscapeTimer += dt;
                    if (food2EscapeTimer <= 1) x4 -= 7;
                    else {
                        food2CloseToRight = false;
                        food2EscapeTimer %= 1;
                    }
                }
                if (food2CloseToTop) {
                    food2EscapeTimer += dt;
                    if (food2EscapeTimer <= 1) y4 += 7;
                    else {
                        food2CloseToTop = false;
                        food2EscapeTimer %= 1;
                    }
                }
                if (food2CloseToBottom) {
                    food2EscapeTimer += dt;
                    if (food2EscapeTimer <= 1) y4 -= 7;
                    else {
                        food2CloseToBottom = false;
                        food2EscapeTimer %= 1;
                    }
                }

                if (food3CloseToLeft) {
                    food3EscapeTimer += dt;
                    if (food3EscapeTimer <= 1) x5 += 7;
                    else {
                        food3CloseToLeft = false;
                        food3EscapeTimer %= 1;
                    }
                }
                if (food3CloseToRight) {
                    food3EscapeTimer += dt;
                    if (food3EscapeTimer <= 1) x5 -= 7;
                    else {
                        food3CloseToRight = false;
                        food3EscapeTimer %= 1;
                    }
                }
                if (food3CloseToTop) {
                    food3EscapeTimer += dt;
                    if (food3EscapeTimer <= 1) y5 += 7;
                    else {
                        food3CloseToTop = false;
                        food3EscapeTimer %= 1;
                    }
                }
                if (food3CloseToBottom) {
                    food3EscapeTimer += dt;
                    if (food3EscapeTimer <= 1) y5 -= 7;
                    else {
                        food3CloseToBottom = false;
                        food3EscapeTimer %= 1;
                    }
                }

                if (food4CloseToLeft) {
                    food4EscapeTimer += dt;
                    if (food4EscapeTimer <= 1) x6 += 7;
                    else {
                        food4CloseToLeft = false;
                        food4EscapeTimer %= 1;
                    }
                }
                if (food4CloseToRight) {
                    food4EscapeTimer += dt;
                    if (food4EscapeTimer <= 1) x6 -= 7;
                    else {
                        food4CloseToRight = false;
                        food4EscapeTimer %= 1;
                    }
                }
                if (food4CloseToTop) {
                    food4EscapeTimer += dt;
                    if (food4EscapeTimer <= 1) y6 += 7;
                    else {
                        food4CloseToTop = false;
                        food4EscapeTimer %= 1;
                    }
                }
                if (food4CloseToBottom) {
                    food4EscapeTimer += dt;
                    if (food4EscapeTimer <= 1) y6 -= 7;
                    else {
                        food4CloseToBottom = false;
                        food4EscapeTimer %= 1;
                    }
                }
            }

            if (pressed) angle += angleSize;

            //collision detection
            if (!hasPowerup) {
                if(!isInvincible) {
                    float dx = x - x2;
                    float dy = y - y2;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance < (50 + 55)) {
                        GameState = GAME_STATES.SCORE;
                    }
                }
            } else {
                if (!enemyGone) {
                    float dx = x - x2;
                    float dy = y - y2;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance < (75 + 55)) {
                        score += 10;
                        enemyGone = true;
                    }
                }
            }

            if(secondEnemyHasSpawned) {
                if (!hasPowerup) {
                    if (!isInvincible) {
                        float dx2 = x - x7;
                        float dy2 = y - y7;
                        float distance2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);
                        if (distance2 < (50 + 62.5)) {
                            GameState = GAME_STATES.SCORE;
                        }
                    }
                }
            }

            if (hasPowerup) {
                if (!food1Gone) {
                    float dx3 = x - x3;
                    float dy3 = y - y3;
                    float distance3 = (float) Math.sqrt(dx3 * dx3 + dy3 * dy3);
                    if (distance3 < (75 + 55)) {
                        score += 10;
                        food1Gone = true;
                    }
                }

                if (!food2Gone) {
                    float dx4 = x - x4;
                    float dy4 = y - y4;
                    float distance4 = (float) Math.sqrt(dx4 * dx4 + dy4 * dy4);
                    if (distance4 < (75 + 55)) {
                        score += 10;
                        food2Gone = true;
                    }
                }

                if (!food3Gone) {
                    float dx5 = x - x5;
                    float dy5 = y - y5;
                    float distance5 = (float) Math.sqrt(dx5 * dx5 + dy5 * dy5);
                    if (distance5 < (75 + 55)) {
                        score += 10;
                        food3Gone = true;
                    }
                }

                if (!food4Gone) {
                    float dx6 = x - x6;
                    float dy6 = y - y6;
                    float distance6 = (float) Math.sqrt(dx6 * dx6 + dy6 * dy6);
                    if (distance6 < (75 + 55)) {
                        score += 10;
                        food4Gone = true;
                    }
                }
            }

            //score
            for (int i = pointsList.size() - 1; i >= 0; i--) {
                float dx7 = x - pointsList.get(i).x;
                float dy7 = y - pointsList.get(i).y;
                float distance7 = (float) Math.sqrt(dx7 * dx7 + dy7 * dy7);
                if (!hasPowerup) {
                    if (distance7 < (50 + 25)) {
                        score++;
                        pointsList.remove(i);
                    }
                } else {
                    if (distance7 < (75 + 25)) {
                        score++;
                        pointsList.remove(i);
                    }
                }
            }

            //add points
            if (!pointsAreSpawned) {
                for (int i = 0; i < 10; i++) {
                    randomX = random.nextInt(1600);
                    randomY = random.nextInt(780) + 20;
                    pointsList.add(new Point(randomX, randomY));
                }
                pointsAreSpawned = true;
            }

            //powerup detection
            if (!hasPowerup) {
                if (pointsList.isEmpty()) {
                    hasPowerup = true;
                    secondEnemyHasSpawned = false;
                    for (int i = 0; i < 20; i++) {
                        randomX = random.nextInt(1600);
                        randomY = random.nextInt(780) + 20;
                        pointsList.add(new Point(randomX, randomY));
                    }
                }
            }

            //powerup timer
            if (hasPowerup) {
                powerupTimer += dt;
                if (powerupTimer >= 10) {
                    enemyGone = false;
                    food1Gone = false;
                    food2Gone = false;
                    food3Gone = false;
                    food4Gone = false;
                    enemyCloseToBottom = false;
                    enemyCloseToTop = false;
                    enemyCloseToLeft = false;
                    enemyCloseToRight = false;
                    food1CloseToBottom = false;
                    food1CloseToTop = false;
                    food1CloseToLeft = false;
                    food1CloseToRight = false;
                    food2CloseToBottom = false;
                    food2CloseToTop = false;
                    food2CloseToLeft = false;
                    food2CloseToRight = false;
                    food3CloseToBottom = false;
                    food3CloseToTop = false;
                    food3CloseToLeft = false;
                    food3CloseToRight = false;
                    food4CloseToBottom = false;
                    food4CloseToTop = false;
                    food4CloseToLeft = false;
                    food4CloseToRight = false;
                    hasPowerup = false;
                    if (!secondEnemyHasSpawned) {
                        secondEnemyHasSpawned = true;
                    }
                    powerupTimer %= 10;
                    hadPowerup = true;
                }
            }

            if(hadPowerup){
                invincibleTimer += dt;
                if(invincibleTimer <= 2){
                    isInvincible = true;
                } else {
                    isInvincible = false;
                    hadPowerup = false;
                    invincibleTimer %= 2;
                }
            }

            //update enemies according to powerup status
            if (hasPowerup) {
                if (!enemyCloseToLeft && !enemyCloseToRight && !enemyCloseToTop && !enemyCloseToBottom) {
                    //update enemies
                    double direct6 = Math.atan2(y - y2, x - x2);
                    x2 -= 7 * Math.cos(direct6);
                    y2 -= 7 * Math.sin(direct6);
                }

                //add more food
                if (!foodIsSpawned) {
                    x3 = x + 200;
                    y3 = y;
                    x4 = x;
                    y4 = y + 200;
                    x5 = x - 200;
                    y5 = y;
                    x6 = x;
                    y6 = y - 200;

                    foodIsSpawned = true;
                } else {
                    if (!food1CloseToLeft && !food1CloseToRight && !food1CloseToTop && !food1CloseToBottom) {
                        double direct2 = Math.atan2(y - y3, x - x3);
                        x3 -= 7 * Math.cos(direct2);
                        y3 -= 7 * Math.sin(direct2);
                    }

                    if (!food2CloseToLeft && !food2CloseToRight && !food2CloseToTop && !food2CloseToBottom) {
                        double direct3 = Math.atan2(y - y4, x - x4);
                        x4 -= 7 * Math.cos(direct3);
                        y4 -= 7 * Math.sin(direct3);
                    }

                    if (!food3CloseToLeft && !food3CloseToRight && !food3CloseToTop && !food3CloseToBottom) {
                        double direct4 = Math.atan2(y - y5, x - x5);
                        x5 -= 7 * Math.cos(direct4);
                        y5 -= 7 * Math.sin(direct4);
                    }

                    if (!food4CloseToLeft && !food4CloseToRight && !food4CloseToTop && !food4CloseToBottom) {
                        double direct5 = Math.atan2(y - y6, x - x6);
                        x6 -= 7 * Math.cos(direct5);
                        y6 -= 7 * Math.sin(direct5);
                    }
                }
            } else {
                //update enemies
                double direct7 = Math.atan2(y - y2, x - x2);
                x2 += 7 * Math.cos(direct7);
                y2 += 7 * Math.sin(direct7);
            }

            //random points spawn
            timer += dt;
            if (!hasPowerup) {
                if (timer >= 1.7) {

                    randomX = random.nextInt(1600);
                    randomY = random.nextInt(780) + 20;
                    pointsList.add(new Point(randomX, randomY));

                    timer %= 1.7;

                }
            } else {
                if (timer >= 1) {

                    randomX = random.nextInt(1600);
                    randomY = random.nextInt(800);
                    pointsList.add(new Point(randomX, randomY));

                    timer %= 1;

                }
            }

        }
    }

    private void resetGame() {
        //sprite1 variables
        x = 50;
        y = 100;
        angleSize = 0;
        angle = 0;
        score = 0;

        //enemy1 variables
        x2 = 1500;
        y2 = 700;

        //point variables
        randomY = 0;
        randomX = 0;
        pointsList.clear();
        pointsAreSpawned = false;

        //booleons
        hasPowerup = false;
        showText = false;
        foodIsSpawned = false;
        enemyGone = false;
        food1Gone = false;
        food2Gone = false;
        food3Gone = false;
        food4Gone = false;
        enemyCloseToBottom = false;
        enemyCloseToTop = false;
        enemyCloseToLeft = false;
        enemyCloseToRight = false;
        food1CloseToBottom = false;
        food1CloseToTop = false;
        food1CloseToLeft = false;
        food1CloseToRight = false;
        food2CloseToBottom = false;
        food2CloseToTop = false;
        food2CloseToLeft = false;
        food2CloseToRight = false;
        food3CloseToBottom = false;
        food3CloseToTop = false;
        food3CloseToLeft = false;
        food3CloseToRight = false;
        food4CloseToBottom = false;
        food4CloseToTop = false;
        food4CloseToLeft = false;
        food4CloseToRight = false;
        secondEnemyHasSpawned = false;
        isTraveling = false;
        hadPowerup = false;
        isInvincible = false;
        updatedHighScore = false;

        //timers
        timer = 0;
        powerupTimer = 0;
        enemyEscapeTimer = 0;
        food1EscapeTimer = 0;
        food2EscapeTimer = 0;
        food3EscapeTimer = 0;
        food4EscapeTimer = 0;
        invincibleTimer = 0;
    }

    private void draw() {
        //get canvas
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        if(GameState != GAME_STATES.GAME) {
            if(GameState == GAME_STATES.SCORE){
                //draw instructions
                if(showText){
                    g.setFont(new Font("Sans_Serif", Font.PLAIN, 25));
                    g.setColor(Color.white);
                    g.drawImage(textBox, null, 1400, 200);
                    g.drawString("Try to collect as many coins as you can!", 1425, 335);
                    g.drawString("But watch out, there are enemies who", 1425, 360);
                    g.drawString("guard the coins and want to kill you!", 1425, 385);
                    g.drawString("Use the left and right arrows to move", 1425, 410);
                    g.drawString("your ball and when you clear the screen", 1425, 435);
                    g.drawString("of all the coins, you earn a power up!", 1425, 460);
                    g.drawString("During the power up, you become bigger", 1425, 485);
                    g.drawString("and faster while its your turn to eat", 1425, 510);
                    g.drawString("the balls! Good luck and have fun!", 1425, 535);
                } else {
                    g.clearRect(1400, 100, 500, 463); //g.clearRect(1400, 200, 500, 363); //change dimensions to be the text box and then draw over it

                    //clear screen
                    g.setColor(Color.blue);
                    g.fillRect(0, 0, WIDTH, HEIGHT);

                    //draw Points
                    for (Point aPointsList : pointsList) g.drawImage(coin, null, aPointsList.x - 25, aPointsList.y - 25);

                    //draw enemies
                    g.setColor(Color.red);
                    g.fillOval((int) x2 - 55, (int) y2 - 55, 110, 110);
                    if(secondEnemyHasSpawned){
                        g.setColor(Color.black);
                        g.fillOval((int)(x7 - 62.5), (int)(y7 - 62.5), 125, 125);
                    }

                    //update and draw highScore
                    if(score > highScore){
                        highScore = score;
                        highScorePrefs.putInt("highScore", highScore);
                        try {
                            highScorePrefs.flush();
                        } catch (BackingStoreException e) {
                            e.printStackTrace();
                        }

                        //draw new highScore
                        g.setFont(new Font("Sans_Serif", Font.PLAIN, 75));
                        g.setColor(Color.green);
                        g.drawString("New High Score: " + highScore + "!", (WIDTH / 2) - 350, (HEIGHT / 2) - 300);

                        //draw score
                        g.setColor(Color.green);
                        g.setFont(new Font("Sans_Serif", Font.PLAIN, 100));
                        g.drawString(String.valueOf(score), 50, 150);

                        updatedHighScore = true;
                    } else {
                        if(!updatedHighScore) {
                            //draw high score
                            g.setFont(new Font("Sans_Serif", Font.PLAIN, 75));
                            g.setColor(Color.WHITE);
                            g.drawString("High Score: " + highScore, (WIDTH / 2) - 295, (HEIGHT / 2) - 300);

                            //draw score
                            g.setColor(Color.black);
                            g.setFont(new Font("Sans_Serif", Font.PLAIN, 100));
                            g.drawString(String.valueOf(score), 50, 150);
                        } else{
                            //draw new highScore
                            g.setFont(new Font("Sans_Serif", Font.PLAIN, 75));
                            g.setColor(Color.green);
                            g.drawString("New High Score: " + highScore + "!", (WIDTH / 2) - 350, (HEIGHT / 2) - 300);

                            //draw score
                            g.setColor(Color.green);
                            g.setFont(new Font("Sans_Serif", Font.PLAIN, 100));
                            g.drawString(String.valueOf(score), 50, 150);
                        }
                    }

                    //draw Game Over
                    g.setFont(new Font("Sans_Serif", Font.PLAIN, 100));
                    g.setColor(Color.black);
                    g.drawString("Game Over", (WIDTH / 2) - 300, HEIGHT / 2);
                }
                g.drawImage(lightbulb, null, 1750, 100);
            }
            switch (buttonState % 4) {
                case 0:
                /*draw button #1 unmodified*/
                    g.setColor(new Color(150, 150, 150));
                    g.fillRect(b1.x, b1.y, b1.width, b1.height);
                    break;
                case 1:
                /*draw button #1 with mouse over*/
                    g.setColor(new Color(100, 100, 100));
                    g.fillRect(b1.x, b1.y, b1.width, b1.height);
                    break;
                case 2:
                /*draw button #1 with press*/
                    g.setColor(new Color(75, 75, 75));
                    g.fillRect(b1.x, b1.y, b1.width, b1.height);
                    break;
                case 3:
                /*draw button #1 with press and mouse over*/
                    g.setColor(new Color(75, 75, 75));
                    g.fillRect(b1.x, b1.y, b1.width, b1.height);
                    break;
            }
            switch (buttonState / 4) {
                case 0:
                /*draw button #2 unmodified*/
                    g.setColor(new Color(150, 150, 150));
                    g.fillRect(b2.x, b2.y, b2.width, b2.height);
                    break;
                case 1:
                /*draw button #2 with mouse over*/
                    g.setColor(new Color(100, 100, 100));
                    g.fillRect(b2.x, b2.y, b2.width, b2.height);
                    break;
                case 2:
                /*draw button #2 with press*/
                    g.setColor(new Color(75, 75, 75));
                    g.fillRect(b2.x, b2.y, b2.width, b2.height);
                    break;
                case 3:
                /*draw button #2 with press and mouse over*/
                    g.setColor(new Color(75, 75, 75));
                    g.fillRect(b2.x, b2.y, b2.width, b2.height);
                    break;
            }

            g.setFont(new Font("Sans_Serif", Font.PLAIN, 50));
            g.setColor(Color.white);
            g.drawString("Play", 600, 775);
            g.drawString("Quit", 1200, 775);
        }

        switch (GameState) {
            case MENU:
                //draw logo
                g.drawImage(logo, null, 500, 0);

                //draw instructions
                if(showText){
                    g.setFont(new Font("Sans_Serif", Font.PLAIN, 25));
                    g.setColor(Color.black);
                    g.drawImage(textBox, null, 1400, 200);
                    g.drawString("Try to collect as many coins as you can!", 1425, 335);
                    g.drawString("But watch out, there are enemies who", 1425, 360);
                    g.drawString("guard the coins and want to kill you!", 1425, 385);
                    g.drawString("Use the left and right arrows to move", 1425, 410);
                    g.drawString("your ball and when you clear the screen", 1425, 435);
                    g.drawString("of all the coins, you earn a power up!", 1425, 460);
                    g.drawString("During the power up, you become bigger", 1425, 485);
                    g.drawString("and faster while its your turn to eat", 1425, 510);
                    g.drawString("the balls! Good luck and have fun!", 1425, 535);
                } else {
                    g.clearRect(1400, 0, 500, 563);
                }
                g.drawImage(lightbulb, null, 1750, 100);
                break;
            case GAME:
                //clear screen
                g.setColor(Color.blue);
                g.fillRect(0, 0, WIDTH, HEIGHT);

                //draw points
                if (!pointsAreSpawned) {

                    for (int i = 0; i < 7; i++) {
                        randomX = random.nextInt(1600);
                        randomY = random.nextInt(800);
                        pointsList.add(new Point(randomX, randomY));
                    }

                    pointsAreSpawned = true;

                }

                for (Point aPointsList : pointsList) g.drawImage(coin, null, aPointsList.x - 25, aPointsList.y - 25);

                //draw sprite
                if(!hasPowerup) {
                    g.setColor(Color.white);
                    g.fillOval((int) x - 50, (int) y - 50, 100, 100);
                } else if(powerupTimer < 8 || powerupTimer * 4 % 2 < 1) {
                        g.setColor(Color.green);
                        g.fillOval((int) x - 75, (int) y - 75, 125, 125);
                }

                //draw enemies
                if(!hasPowerup) {
                    g.setColor(Color.red);
                } else {
                    g.setColor(Color.yellow);
                }
                if(!enemyGone) g.fillOval((int) x2 - 55, (int) y2 - 55, 110, 110);
                if(secondEnemyHasSpawned){
                    g.setColor(Color.black);
                    g.fillOval((int)(x7 - 62.5), (int)(y7 - 62.5), 125, 125);
                }

                //draw food
                if(hasPowerup) {
                    if(!food1Gone) g.fillOval((int) (x3 - 55), (int) (y3 - 55), 110, 110);
                    if(!food2Gone) g.fillOval((int) (x4 - 55), (int) (y4 - 55), 110, 110);
                    if(!food3Gone) g.fillOval((int) (x5 - 55), (int) (y5 - 55), 110, 110);
                    if(!food4Gone) g.fillOval((int) (x6 - 55), (int) (y6 - 55), 110, 110);
                }

                //draw score
                g.setColor(Color.black);
                g.setFont(new Font("Sans_Serif", Font.PLAIN, 100));
                g.drawString(String.valueOf(score), 50, 150);
                break;
        }

        g.dispose();
        strategy.show();

    }


    private void run() {
        init();

        while (isRunning) {
            //new loop, clock the start
            startFrame = System.currentTimeMillis();
            //calculate delta time
            dt = (float) (startFrame - lastFrame) / 1000;
            //log the current time
            lastFrame = startFrame;

            //call update and draw methods
            update();
            draw();

            //dynamic thread sleep, only sleep the time we need to cap the framerate
            rest = (1000 / MAX_FPS) - (System.currentTimeMillis() - startFrame);
            if (rest > 0) {
                try {
                    Thread.sleep(rest);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private BufferedImage makeImage(String path) {
        try {
            return ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        Game game = new Game(2000, 1000, 60);
        game.run();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (!keys.contains(keyEvent.getKeyCode())) {
            keys.add(keyEvent.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

        for (int i = keys.size() - 1; i >= 0; i--) {

            if (keys.get(i) == keyEvent.getKeyCode()) {
                keys.remove(i);
            }

        }

        pressed = false;
    }

    private class ML implements MouseListener, MouseMotionListener {
        @Override
        public void mousePressed(MouseEvent e) {
            Point mouse = getMousePosition();
            if (mouse != null) {
                if (mouse.x > b1.x && mouse.x < b1.x + b1.width && mouse.y > b1.y && mouse.y < b1.y + b1.height) {
                    buttonState = 3;
                } else if (mouse.x > b2.x && mouse.x < b2.x + b2.width && mouse.y > b2.y && mouse.y < b2.y + b2.height) {
                    buttonState = 12;
                } else {
                    buttonState = 0;
                }

            }

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (buttonState % 4 == 3) {
                if(GameState != GAME_STATES.GAME) {
                    resetGame();
                    GameState = GAME_STATES.GAME;
                }
            } else if (buttonState / 4 == 3) {
                if(GameState != GAME_STATES.GAME) {
                    System.exit(0);
                }
            }

            if(e.getX() >= 1750 && e.getX() <= 1850 && e.getY() >= 100 && e.getY() <= 200){
                showText = !showText;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point mouse = getMousePosition();
            if (mouse != null) {
                if (mouse.x > b1.x && mouse.x < b1.x + b1.width && mouse.y > b1.y && mouse.y < b1.y + b1.height) {
                    buttonState = 1;
                } else if (mouse.x > b2.x && mouse.x < b2.x + b2.width && mouse.y > b2.y && mouse.y < b2.y + b2.height) {
                    buttonState = 4;
                } else {
                    buttonState = 0;
                }

            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point mouse = getMousePosition();
            if (mouse != null) {
                if (mouse.x > b1.x && mouse.x < b1.x + b1.width && mouse.y > b1.y && mouse.y < b1.y + b1.height) {
                    buttonState |= 1;
                    buttonState &= 11;
                } else if (mouse.x > b2.x && mouse.x < b2.x + b2.width && mouse.y > b2.y && mouse.y < b2.y + b2.height) {
                    buttonState |= 4;
                    buttonState &= 14;
                } else {
                    buttonState &= 10;
                }
            }
        }
    }
}