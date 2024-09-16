import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZorluTopOyunu extends JPanel implements ActionListener, KeyListener {

    private int sepetX = 300;
    private int sepetY = 600;
    private int sepetGenislik = 150;
    private int sepetYukseklik = 75;
    private int pencereGenislik = 800;
    private int pencereYukseklik = 800;
    private int skor = 0;
    private int oncekiSkor = 0;
    private int toplarYakalanan = 0;
    private int topHızı = 4;
    private List<Top> toplar = new ArrayList<>();
    private Timer timer;
    private BufferedImage topImage, sepetImage, kirmiziTopImage, yesilTopImage, altinTopImage, backgroundImage;
    private boolean oyunBitti = false;
    private JButton retryButton;
    private Clip backgroundMusicClip;

    public ZorluTopOyunu() {
        this.setFocusable(true);
        this.addKeyListener(this);
        timer = new Timer(10, this);
        timer.start();

        arkaPlanMuzigiCal("src/main/java/data/background_music.wav");

        toplar.add(new Top());

        try {

            topImage = ImageIO.read(new File("src/main/java/data/ball.png"));
            kirmiziTopImage = ImageIO.read(new File("src/main/java/data/red_ball.png"));
            yesilTopImage = ImageIO.read(new File("src/main/java/data/green_ball.png"));
            altinTopImage = ImageIO.read(new File("src/main/java/data/gold_ball.png"));
            sepetImage = ImageIO.read(new File("src/main/java/data/basket.png"));
            backgroundImage = ImageIO.read(new File("src/main/java/data/image.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        retryButton = new JButton("Retry");
        retryButton.setBounds(350, 400, 100, 50);
        retryButton.setVisible(false);
        retryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                yenidenBaslat();
            }
        });
        this.setLayout(null);
        this.add(retryButton);
    }

    private void yenidenBaslat() {
        oyunBitti = false;
        skor = 0;
        oncekiSkor = 0;
        toplarYakalanan = 0;
        topHızı = 4;
        toplar.clear();
        toplar.add(new Top());
        retryButton.setVisible(false);
        timer.start();
        repaint();
    }

    private void arkaPlanMuzigiCal(String musicFilePath) {
        try {
            File musicPath = new File(musicFilePath);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                backgroundMusicClip = AudioSystem.getClip();
                backgroundMusicClip.open(audioInput);
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusicClip.start();
            } else {
                System.out.println("Dosya bulunamadı: " + musicFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void topYakalamaSesiCal(String soundFilePath) {
        try {
            File soundFile = new File(soundFilePath);
            if (soundFile.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else {
                System.out.println("Dosya bulunamadı: " + soundFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage, 0, 0, pencereGenislik, pencereYukseklik, this);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Skor: " + skor, 10, 30);

        if (oyunBitti) {

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("Game Over", 250, 350);
            retryButton.setVisible(true);
        } else {

            g.drawImage(sepetImage, sepetX, sepetY, sepetGenislik, sepetYukseklik, this);

            for (Top top : toplar) {
                if (top.type.equals("kirmizi")) {
                    g.drawImage(kirmiziTopImage, top.x, top.y, top.size, top.size, this);
                } else if (top.type.equals("yesil")) {
                    g.drawImage(yesilTopImage, top.x, top.y, top.size, top.size, this);
                } else if (top.type.equals("altin")) {
                    g.drawImage(altinTopImage, top.x, top.y, top.size, top.size, this);
                } else {
                    g.drawImage(topImage, top.x, top.y, top.size, top.size, this);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!oyunBitti) {
            List<Top> yakalananToplar = new ArrayList<>();

            for (Top top : toplar) {
                top.y += topHızı;

                if (top.y + top.size >= sepetY && top.x + top.size >= sepetX && top.x <= sepetX + sepetGenislik) {
                    yakalananToplar.add(top);
                    toplarYakalanan++;

                    if (top.type.equals("kirmizi")) {
                        skor -= 5;
                    } else if (top.type.equals("yesil")) {
                        skor += 10;
                    } else if (top.type.equals("altin")) {
                        topHızı = Math.max(topHızı - 1, 2);
                    } else {
                        skor++;
                        topYakalamaSesiCal("src/main/java/data/catch_sound.wav");
                    }
                }

                if (top.y > pencereYukseklik) {
                    if (!top.type.equals("kirmizi")) {
                        oyunBitti = true;
                        timer.stop();
                    } else {

                        yakalananToplar.add(top);
                    }
                }
            }

            toplar.removeAll(yakalananToplar);

            if (skor / 10 > oncekiSkor / 10) {
                topHızı++;
                oncekiSkor = skor;
            }

            if (toplar.isEmpty()) {
                toplar.add(new Top());
            }

            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && sepetX > 0) {
            sepetX -= 20;
        }
        if (key == KeyEvent.VK_RIGHT && sepetX < pencereGenislik - sepetGenislik) {
            sepetX += 20;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame pencere = new JFrame("Zorlu Top Oyunu");
        ZorluTopOyunu oyun = new ZorluTopOyunu();
        pencere.setSize(800, 800);
        pencere.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pencere.add(oyun);
        pencere.setVisible(true);
    }

    class Top {
        int x, y;
        int size = 50;
        String type;

        public Top() {
            this.x = (int) (Math.random() * (pencereGenislik - size));
            this.y = 0;

            double rastgele = Math.random();
            if (rastgele < 0.1) {
                this.type = "altin";
            } else if (rastgele < 0.3) {
                this.type = "yesil";
            } else if (rastgele < 0.5) {
                this.type = "kirmizi";
            } else {
                this.type = "normal";
            }
        }
    }
}