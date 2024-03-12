package mysweeper;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import static mysweeper.MySweeper.*;

public class MySweeper extends JFrame{
    
    public MySweeper(){}
    
    public MySweeper(String s){
        super(s);
    }
    
    final static int SCRWIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    final static int SCRHEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    final static int TABLEWIDTH = 501, TABLEHEIGHT = TABLEWIDTH;
    final static int FRAMEWIDTH = TABLEWIDTH + 40, FRAMEHEIGHT = TABLEHEIGHT + 120;
    static Table table;
    static MySweeper frame;
    static boolean start = true;
    static boolean inGame = true;
    static int totalMineCount = 70;
    static JLabel label;
    static JSpinner spinner;
    
    public static void main(String[] args) {
        frame = new MySweeper("A minesweeper remake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocation((SCRWIDTH-FRAMEWIDTH)/2, (SCRHEIGHT-FRAMEHEIGHT)/2);
        table = new Table();
        frame.add(table);
        frame.setSize(FRAMEWIDTH, FRAMEHEIGHT);
        frame.setResizable(false);
        frame.getContentPane().setBackground(new Color(160, 190, 50));
        
        JLabel mineLabel = new JLabel("Mines:");
        mineLabel.setBounds(25, 15, 200, 50);
        mineLabel.setFont(new Font("Curier", 20, 20));
        frame.add(mineLabel);
        
        spinner = new JSpinner(new SpinnerNumberModel(totalMineCount,15,180,1));
        spinner.setBounds(90,20,50,40);
        spinner.setFont(new Font("Curier", 20, 20));
        spinner.setEnabled(true);
        spinner.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                totalMineCount = (Integer)spinner.getValue();
            }
        });
        frame.add(spinner);
        
        label = new JLabel("Flags: 0");
        label.setBounds(205, 15, 200, 50);
        label.setFont(new Font("Curier", 20, 20));
        frame.add(label);
        
        JButton restart = new JButton("New game");
        restart.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                start = true;
                table.setup();
                inGame = true;
                label.setText("Flags: 0");
                spinner.setEnabled(true);
                table.repaint();
            }
        });
        restart.setBounds(360, 20, 140, 40);
        restart.setOpaque(true);
        restart.setBackground(Color.ORANGE);
        restart.setFont(new Font("Curier", 20, 20));
        frame.add(restart);
        
        frame.setVisible(true);   
    }
}

class Table extends JPanel implements MouseListener{
 
    Field[][] field = new Field[20][20];
    
    Table(){
        this.setSize(new Dimension (TABLEWIDTH,TABLEHEIGHT));
        this.setLocation(12, 71);
        this.addMouseListener(this);
        setup();
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Field[] f : field){
            for (Field ff: f){
                ff.draw(g2d);
            }
        }
    }
    
    void setup(){
        for (int x = 0; x<20; x++){
            for (int y = 0; y<20; y++){
                field[x][y] = new Field(x,y);
            }
        }
    }
    
    void setMines(int a, int b){
        int mineNumber = 0;
        do{
            int x = (int)(Math.random()*20);
            int y = (int)(Math.random()*20);
            if ((x>=a-1 && x<=a+1) && (y>=b-1 && y<=b+1)){
                continue;
            } 
            if (field[x][y].isMine() == false){
                field[x][y].setMine();
                mineNumber++;
            } 
        } while(mineNumber <totalMineCount);
        int count = 0;
        for (Field[] f : field){
            for (Field ff: f){
                if (ff.isMine()) continue;
                for (int i = ff.x-1; i<=ff.x+1; i++){
                    for(int j = ff.y-1; j <=ff.y+1; j++){
                        if (i<0 || j<0 || i>=20 || j>=20) continue;
                        if (field[i][j].isMine()) count++;
                    }
                }
                ff.setNeighbours(count);
                count = 0;
            }
        }
    }
    
    int countFlags(){
        int count = 0;
        for (Field f[]: field){
            for (Field ff: f){
                if (ff.isFlagged()) count++;
            }
        }
        return count;
    }
     
    @Override
    public void mouseReleased(MouseEvent e) {
        int i = e.getX()/25;
        int j = e.getY()/25;
        if (i>=field.length || j >=field[i].length) return;
        if(e.getButton() == 1 && ! field[i][j].isFlagged() && inGame){
            if(start){
                setMines(i,j);
                spinner.setEnabled(false);
                start = false;
            }
            if (field[i][j].setKnown()) gameOver();
        } else if (e.getButton()==3 && !field[i][j].isKnown() && !start && inGame){
            field[i][j].setFlag(!field[i][j].isFlagged());
            String s1 = Integer.toString(countFlags());
            label.setText("Flags: " + s1);
        }
        winCheck();
        repaint();
    }
    
    void gameOver(){

        for (Field [] f : field){
            for (Field ff : f){
                if (ff.isMine() && !ff.isKnown()){
                    ff.setKnown();
                }
            }
        }
        label.setText("Game over");
        inGame = false;
    }
    
    void winCheck(){
        
        for (Field [] f : field){
            for (Field ff : f){
                if ((ff.isMine() && ff.isFlagged())||(!ff.isMine() && ff.isKnown())) {
                    continue;
                }
                return;
            }
        }
        label.setText("Well done!");
        inGame = false;
    }
    
    @Override
    public void mouseEntered(MouseEvent e){}
    @Override
    public void mouseExited(MouseEvent e){}
    @Override
    public void mouseClicked(MouseEvent e){}
    @Override
    public void mousePressed(MouseEvent e){}
}

class Field{

    final int x, y;
    private final int xLoc, yLoc;
    private int neighbours;
    private boolean hasMine;
    private boolean visible = false;
    private boolean flagged = false;
    
    Field(int x, int y){
        this.x = x;
        this.y = y;
        this.xLoc = this.x * 25;
        this.yLoc = this.y * 25;
    }
    
    void setMine(){
        hasMine = true;
    }
    
    boolean isMine(){
        return hasMine;
    }
    
    void setNeighbours(int i){
        neighbours = i;
    }
    
    int getNeighbours(){
        return neighbours;
    }
    
    boolean setKnown(){
        boolean explode = false;
        visible = true;
        if (hasMine){
            explode = true;
        }else if (neighbours == 0){
            for (int i = this.x-1; i<=this.x+1; i++){
                for (int j = this.y-1; j<=this.y+1; j++){
                    if(i<0 || j<0 || i>=20 || j>=20) continue;
                    if(i == x && j == y) continue;
                    if(!table.field[i][j].isKnown() && !table.field[i][j].isFlagged()){
                        table.field[i][j].setKnown();
                    }
                }
            } 
        }
        return explode;
    }
    
    boolean isKnown(){
        return visible;
    }
    
    void setFlag(boolean b){
        flagged = b;
    }
    
    boolean isFlagged(){
        return flagged;
    }
    
    void draw (Graphics2D g2){
        g2.translate (xLoc, yLoc);
        if (visible) {
            if (hasMine) {
                g2.setColor(Color.RED);
                g2.fillRect(0, 0, 25, 25);
                g2.setColor(Color.BLACK);
                g2.translate(12.5, 12.5);
                g2.fillOval(-4, -4, 9, 9);
                for (int i = 0; i < 8; i++) {
                    g2.drawLine(-8, 0, 9, 0);
                    g2.rotate(Math.PI / 4);
                }
                g2.translate(-12.5, -12.5);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, 25, 25);
            }

            if (neighbours > 0) {
                g2.translate(8, 20);
                String str = String.valueOf(neighbours);
                g2.setFont(new Font("Curier", 20, 20));
                switch (neighbours) {
                    case 1:
                        g2.setColor(Color.BLUE);
                        break;
                    case 2:
                        g2.setColor(Color.RED);
                        break;
                    case 3:
                        g2.setColor(Color.ORANGE);
                        break;
                    case 4:
                        g2.setColor(Color.GREEN);
                        break;
                    case 5:
                        g2.setColor(Color.CYAN);
                        break;
                    case 6:
                        g2.setColor(Color.BLACK);
                        break;
                    case 7:
                        g2.setColor(Color.MAGENTA);
                        break;
                    case 8:
                        g2.setColor(Color.PINK);
                        break;
                }
                g2.drawString(str, 0, 0);
                g2.translate(-8, -20);
            }
        } else{
            g2.setColor(new Color (50,170,100));
            g2.fillRect(0, 0, 25, 25);
            if (flagged){
                g2.setColor(Color.RED);
                g2.drawLine(7, 5, 9, 22);
                g2.drawLine(8, 5, 10, 22);
                g2.fillPolygon(new int[]{7,22,8}, new int[]{5,10,15}, 3);
            }
        }
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, 25, 25);
        g2.translate(-xLoc, -yLoc);
    }
}
