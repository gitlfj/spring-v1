package com.lfj.game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * snake
 *
 * @author lfj
 * @date 2020/11/17 15:21
 */
public class SnakeGame extends MyFrame {

    /**
     * 蛇
     */
    private MySnake mySnake = new MySnake(100, 150);

    /**
     * 食物
     */
    private Food food = new Food();


    public static void main(String[] args) {
        new SnakeGame().loadFrame();
    }

    @Override
    public void loadFrame() {
        super.loadFrame();
        // 新增按键监听
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                mySnake.keyPressed(e);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        // 绘制背景图片
        g.drawImage(ImageUtils.getImageMap().get("background"), 0, 0, null);
        if (mySnake.isLive) {
            // 绘制蛇
            mySnake.draw(g);
            if (food.isLive) {
                // 绘制食物
                food.draw(g);
                // 食物是否被吃
                food.eaten(mySnake);
            }else {
                food = new Food();
            }
        }else {
            // 游戏结束
            g.drawImage(ImageUtils.getImageMap().get("fail"), 300, 250, null);
        }
        // 展示分数
        drawScore(g);
        if (!mySnake.isLive) {
            System.exit(0);
        }
    }

    /**
     * 绘制分数
     *
     * @param g Graphics
     */
    private void drawScore(Graphics g) {
        g.setFont(new Font("Courier New", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("SCORE:" + mySnake.score, 700, 100);
    }

}

/**
 * 窗口页面
 */
class MyFrame extends Frame {


    public void loadFrame() {
        //设置窗体标题
        this.setTitle("java 贪吃蛇");
        //设置窗体大小
        this.setSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        //设置背景
        this.setBackground(Color.BLACK);
        //居中
        this.setLocationRelativeTo(null);
        //设置可关闭
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        //设置可见
        this.setVisible(true);
        new SnakeThread().start();
    }

    /**
     * 防止图片闪烁，使用双重缓存
     */
    private Image backImg = null;

    @Override
    public void update(Graphics g) {
        if (backImg == null) {
            backImg = createImage(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        }
        Graphics graphics = backImg.getGraphics();
        Color c = graphics.getColor();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        graphics.setColor(c);
        paint(graphics);
        g.drawImage(backImg, 0, 0, null);
    }

    /**
     * 多线程
     */
    class SnakeThread extends Thread {
        @Override
        public void run() {
            for (; ; ) {
//                System.out.println("线程运行。。。");
                repaint();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

/**
 * 蛇
 */
class MySnake extends SnakeObject {

    /**
     * 蛇头图片（未旋转）
     */
    private static final BufferedImage IMG_SNAKE_HEAD = (BufferedImage) ImageUtils.getImageMap().get("snake_head");
    /**
     * 移动速度
     */
    private int speed;
    /**
     * 长度
     */
    private int length;
    /**
     *
     */
    private int num;
    /**
     * 轨迹
     */
    private static java.util.List<Point> bodyPoints = new LinkedList<Point>();

    /**
     * 分数
     */
    public int score = 0;
    /**
     * 旋转后的蛇头图片
     */
    private static BufferedImage newImgSnakeHead;

    /**
     *  方向
     */
    private boolean up, down, left, right = true;


    MySnake(int x, int y) {
        this.isLive = true;
        this.x = x;
        this.y = y;
        this.image = ImageUtils.getImageMap().get("zhan");
        this.width = image.getWidth(null);
        this.height = image.getHeight(null);
        this.speed = 5;
        this.length = 1;
        this.num = width / speed;
        newImgSnakeHead = IMG_SNAKE_HEAD;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    /**
     * 在界面上画蛇
     *
     * @param g Graphics
     */
    @Override
    public void draw(Graphics g) {
        // 蛇吃到身体
        eatBody();
        // 蛇碰到边界
        outOfBounds();
        // 保存轨迹
        bodyPoints.add(new Point(x, y));
        //当保存的轨迹点的个数为蛇的长度+1的num倍时
        if (bodyPoints.size() == (this.length + 1) * num) {
            //移除第一个
            bodyPoints.remove(0);
        }
        // 绘制蛇头
        g.drawImage(newImgSnakeHead, x, y, null);
        // 绘制蛇身体
        drawBody(g);
        // 让蛇动起来
        move();
    }

    /**
     * 绘制蛇身
     * @param g Graphics
     */
    private void drawBody(Graphics g) {
        //前num个存储的是蛇头的当前轨迹坐标
        int length = bodyPoints.size() - 1 - num;
        //从尾部添加
        for (int i = length; i >= num; i -= num) {
            Point p = bodyPoints.get(i);
            System.out.println(" x= " + p.x + "y= " + p.y);
            g.drawImage(image, p.x, p.y, null);
        }
    }

    /**
     *  按键移动蛇
     */
    private void move() {
        if (up) {
            y -= speed;
        }else if (down) {
            y += speed;
        }else if (left) {
            x -= speed;
        }else if(right) {
            x += speed;
        }
    }

    /**
     * 处理是否吃到到身体问题
     */
    private void eatBody() {
        for (Point point : bodyPoints) {
            for (Point point2 : bodyPoints) {
                if (point.equals(point2) && point != point2) {
                    //食物死亡
                    this.isLive = false;
                }
            }
        }
    }

    /**
     * 处理出界问题
     */
    private void outOfBounds() {
        boolean xOut = (x <= 0 || x >= (Constants.GAME_WIDTH - width));
        boolean yOut = (y <= 40 || y >= (Constants.GAME_HEIGHT - height));
        if (xOut || yOut) {
            isLive = false;
        }
    }

    /**
     * 按键监听
     * @param e KeyEvent
     */
    void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (!down) {
                    up = true;
                    down = false;
                    left = false;
                    right = false;
                    //旋转图片
                    newImgSnakeHead = (BufferedImage) ImageUtils.rotateImage(IMG_SNAKE_HEAD, -90);
                }
            break;
            case KeyEvent.VK_DOWN :
                if (!up) {
                    down = true;
                    up = false;
                    left = false;
                    right = false;
                    // 旋转图片
                    newImgSnakeHead = (BufferedImage) ImageUtils.rotateImage(IMG_SNAKE_HEAD, 90);
                }
                break;
            case KeyEvent.VK_LEFT :
                if (!right) {
                    down = false;
                    up = false;
                    left = true;
                    right = false;
                    // 旋转图片
                    newImgSnakeHead = (BufferedImage) ImageUtils.rotateImage(IMG_SNAKE_HEAD, -180);
                }
                break;
            case KeyEvent.VK_RIGHT :
                if (!left) {
                    down = false;
                    up = false;
                    left = false;
                    right = true;
                    newImgSnakeHead = IMG_SNAKE_HEAD;
                }
                break;
            default:
                break;
        }

    }

}

/**
 * 食物
 */
class Food extends SnakeObject {

    Food() {
        this.isLive = true;
        this.image = ImageUtils.getImageMap().get("zhan");
        this.width = image.getWidth(null);
        this.height = image.getHeight(null);
        // 食物的坐标
        this.x = (int) (Math.random() * (Constants.GAME_WIDTH - width + 10));
        this.y = (int) (Math.random() * (Constants.GAME_HEIGHT - 40 - height) + 40);
    }

    /**
     *  食物被吃，被吃的食物消失，产生新的食物
     */
    void eaten(MySnake mySnake) {
        // 如果蛇头和食物相交 and 蛇活着， 食物活着，则食物被吃了
        if (mySnake.getRectangle().intersects(this.getRectangle()) && isLive && mySnake.isLive) {
            // 食物死亡
            this.isLive = false;
            // 长度加1
            mySnake.setLength(mySnake.getLength() + 1);
            // 加分
            mySnake.score += 10;
        }
    }


    /**
     * 在界面上画食物
     *
     * @param g Graphics
     */
    @Override
    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

}

abstract class SnakeObject {

    /**
     * 横坐标
     */
    int x;

    /**
     * 纵坐标
     */
    int y;

    /**
     * 图片
     */
    Image image;

    /**
     * 图片宽度
     */
    int width;

    /**
     * 图片高度
     */
    int height;

    /**
     * 是否存活
     */
    boolean isLive;

    /**
     * 在界面上画蛇
     *
     * @param g Graphics
     */
    public abstract void draw(Graphics g);

    /**
     * 获取图片对应的矩形
     *
     * @return Rectangle
     */
    Rectangle getRectangle() {
        return new Rectangle(x, y, width, height);
    }

}

