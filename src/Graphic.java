//imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;

enum Tiles {
    WA, M1, M2, M3, X1, X2, X3, X4, X5, FL, R, O, Y, G, B, P
}
//TILESET, MENUBAR, CURSOR, INFOBOX, NUMBERS, SELLBOX, SELLTEXT
public class Graphic extends JFrame implements MouseListener, MouseMotionListener, KeyListener {
    //A87717, 5F430F, 3B2A0B
    public int pX = 0, pY = 0, mX = 0, mY = 0, bX = 0, bY = 0;
    public int tempx, tempy, gX = 0, gY = 0, scale = 3, gscale = 3;
    public int[] state = {3, 3, 3, 3, 3, 3};
    int tH = 16, tW = 16, width = 27, height = 17;
    boolean sell = false, buy = false, shift = false, alt = false, ctrl = false;
    static game a = new game();
    public int scrx = (a.mW-width)*tW/2, scry = (a.mH-height)*tH/2;
    ArrayList<tile> draw = a.all;
    Toolkit toolkit = Toolkit.getDefaultToolkit();

    Cursor c;

    public Graphic() {
        super("Money Mine");
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        c = toolkit.createCustomCursor(getImg("cursor"), new Point(7, 7), "cursor");
        add(new GamePane());
    }

    public static void main(String[] args) {
        a.gen(300, 8);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Graphic frame = new Graphic();
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            }
        });
    }

    public Image getImg(String s) {
        return toolkit.getImage(this.getClass().getResource("Resources/"+s+".png"));
    }

    public class GamePane extends JPanel {

        public BufferedImage[] img = new BufferedImage[1];
        int ticks = 0;

        public GamePane() {
            setBackground(Color.BLACK);
            //setCursor(c);
            Timer timer = new Timer(50, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ticks++;
                    a.update(ticks%20 == 0);
                    updateMouse();
                    repaint();
                }
            });
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width * tW * scale, height * tH * scale);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setTransform(AffineTransform.getScaleInstance(scale, scale));
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setTransform(AffineTransform.getScaleInstance(gscale, gscale));
            for(tile t : draw)
                if(t.x * tW > scrx - tW && t.y * tH > scry - tH &&
                        t.x * tW < scrx + (width * tW) + tW && t.y * tH < scry + (width * tW) + tW)
                    for(Tiles ts : t.image())
                        drawTile(g2d, ts, (t.x * tW)-scrx, (t.y * tH)-scry);
            g2.drawImage(getImg("menubar"), 0, 0, this);
            tile t = a.map[mX][mY];
            if(a.show(t) && ((t.gem > 0 && !t.wall) || t.wall))
                drawInfoBox(g2, mX, mY, gX+3, gY+3);
            if(sell || buy) drawSellBoxes(g2);
            drawVals(g2);
            g2d.dispose();
        }

        protected void drawTile(Graphics2D g, Tiles t, int x, int y) {
            int mx = t.ordinal()%4; int my = t.ordinal()/4;
            g.drawImage(getImg("tileset"), x, y, x+tW, y+tH, mx*tW,
                    my*tH, mx*tW+tW, my*tH+tH, this);
        }

        protected void drawInfoBox(Graphics2D g, int x, int y, int sx, int sy) {
            tile t = a.map[x][y];
            g.drawImage(getImg("infobox"), sx, sy, this);
            g.setFont(new Font("Helvetica", Font.PLAIN, 5));
            String[] d = t.description();
            for (int i = 0; i < d.length; i++) {
                g.drawString(d[i], sx+3, sy+(i*7)+7);
            }
        }

        protected void drawNum(Graphics2D g, int n, int x, int y) {
            int mx = n%5, my = n/5, nW = 8, nH = 12;
            g.drawImage(getImg("numbers"), x, y, x+nW, y+nH, mx*nW,
                    my*nH, mx*nW+nW, my*nH+nH, this);
        }

        protected void drawVals(Graphics2D g) {
            for(int i = 0; i < 6; i++){
                drawNumbers(g, 4, 29+(i*48), 2, a.gems[i]);
                if(sell || buy){
                    if((a.market[i] < 1 || a.price[i] > 999) && buy)
                        g.drawImage(getImg("out"), (i*48)+37, 19, this);
                    else if(state[i] != 3) drawNumbers(g, 3, 37+(i*48), 19, a.price[i]);
                }
            }
            drawNumbers(g, 9, 322, 2, a.money);
        }

        protected void drawNumbers(Graphics2D g, int l, int sx, int sy, int num){
            for(int j = 0; j < l; j++) drawNum(g, ((int)(num/Math.pow(10,l-j-1)))%10,
                    sx+(j*9), sy);
        }

        protected void drawSellBoxes(Graphics2D g) {
            for(int i = 0; i < 6; i++) g.drawImage(getImg("sellbox"), (i*48)+27, 17,
                    (i*48)+66, 33, 0, state[i]*16, 39, state[i]*16+16, this);
            int my = 0; if(buy) my = 1;
            g.drawImage(getImg("sellbuy"), 315, 17, 359, 33, 0,
                    my*16, 39, my*16+16, this);
        }

        private void updateMouse() {
            pX = MouseInfo.getPointerInfo().getLocation().x-getLocationOnScreen().x;
            pY = MouseInfo.getPointerInfo().getLocation().y-getLocationOnScreen().y;
            mX = (pX+scrx*scale)/tW/scale; mY = (pY+scry*scale)/tH/scale;
            bX = (int) pX/scale; bY = (int) pY/scale; gX = (int) pX/gscale; gY = (int) pY/gscale;
            if(sell || buy) for(int i = 0; i < 6; i++)
                if(bX > (i*48)+27 && bX < (i*48)+66 && bY < 33 && bY > 16 && state[i] != 3)
                    state[i] = 1; else state[i] = 0;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(sell || buy) for(int i = 0; i < 6; i++)
            if(bX > (i*48)+27 && bX < (i*48)+66 && bY < 33 && bY > 16){
                int n, b = a.gems[i], c = a.money, d = a.price[i], f = a.market[i];
                if(shift){ if(sell) n = b; else n = (int) c/d; }
                else if(alt && ((sell && b > 9) || (buy && c > 9*d && f > 9*d))){
                    if(ctrl && ((sell && b > 999) || (buy && c > 999*d && f > 999*d))) n = 1000;
                    else n = 10;
                }
                else if(ctrl && ((sell && b > 99) || (buy && c > 99*d && f > 99*d))) n = 100;
                else if(((sell && b > 0) || (buy && c > 0 && f > 0))) n = 1;
                else n = 0;
                if(sell){
                    for(int j = 0; j < n; j++){
                        a.money += a.price[i]; a.updatePrices();
                    }
                    a.gems[i] -= n; a.market[i] += n;
                } else {
                    for(int j = 0; j < n; j++){
                        a.money -= a.price[i] * n; a.updatePrices();
                    }
                    a.gems[i] += n; a.market[i] -= n;
                }
            }
        if(bX > 303 && bX < 312 && bY > 3 && bY < 12){
            //menu button code
        } else {
            tile t = a.map[mX][mY];
            if(!t.wall && t.gem > 0 && a.money >= t.cost && t.mine < 3){
                a.money -= t.cost; t.mine++; a.gpt[t.gem-1] -= t.yield;
                t.updateCost(a.sx, a.sy); t.updateYield(); a.gpt[t.gem-1] += t.yield;
                if(!a.unlocked[t.gem]) a.unlocked[t.gem] = true;
            } else if(t.wall && a.money >= t.cost && a.show(t)){
                a.money -= t.cost; a.smash(t);
                t.updateCost(a.sx, a.sy);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(tempx != pX || tempy != pY){
            scrx -= (pX - tempx)/scale;
            scry -= (pY - tempy)/scale;
            tempx = pX; tempy = pY;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        tempx = pX; tempy = pY;
        //System.out.println(bX + ", " + bY);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == 61) scale++;
        else if(e.getKeyCode() == 45) scale--;
        else if(e.getKeyCode() == 83){ sell = !sell; buy = false; }
        else if(e.getKeyCode() == 66){ buy = !buy; sell = false; }
        else if(e.getKeyCode() == 16) shift = true;
        else if(e.getKeyCode() == 17) ctrl = true;
        else if(e.getKeyCode() == 18) alt = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == 16) shift = false;
        else if(e.getKeyCode() == 17) ctrl = false;
        else if(e.getKeyCode() == 18) alt = false;
    }
    //Unecessary Overrides
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}