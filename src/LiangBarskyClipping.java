import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class LiangBarskyClipping extends JPanel {

    // Definisi jendela pemotongan
    private static final int xMin = 20;
    private static final int yMin = 20;
    private static final int xMax = 80;
    private static final int yMax = 80;

    private final ArrayList<Vektor> vektors = new ArrayList<>();
    private int x1, y1, x2, y2;
    private boolean sedangMenggambar = false;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 450); // Lebih tinggi untuk menampung tombol
        LiangBarskyClipping liangBarskyClipping = new LiangBarskyClipping();
        frame.add(liangBarskyClipping);

        // Membuat tombol
        JButton btnHapusGaris = new JButton("Clippling");
        btnHapusGaris.addActionListener(e -> {
            liangBarskyClipping.hapusGarisDiluarKotak();
            liangBarskyClipping.repaint();
        });

        // Menambahkan tombol ke dalam frame
        frame.add(btnHapusGaris, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public LiangBarskyClipping() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                x1 = e.getX() / 4;
                y1 = e.getY() / 4;
                sedangMenggambar = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                x2 = e.getX() / 4;
                y2 = e.getY() / 4;
                sedangMenggambar = false;

                // Membuat nama unik untuk vektor
                String namaVektor = "Garis" + (vektors.size() + 1);

                // Membuat dan menambahkan vektor
                Vektor vektor = new Vektor(namaVektor, x1, y1, x2, y2);
                double[] koordinatTerpotong = liangBarsky(xMin, yMin, xMax, yMax, x1, y1, x2, y2);
                vektor.setKoordinatTerpotong(koordinatTerpotong);

                // Kategorikan garis
                String kategori = kategorikanGaris(x1, y1, x2, y2, koordinatTerpotong);
                vektor.setKategori(kategori);

                vektors.add(vektor);

                // Mencetak detail vektor
                cetakDetailVektor(vektor);

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                x2 = e.getX() / 4;
                y2 = e.getY() / 4;
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Menggambar jendela pemotongan
        g2d.setColor(Color.BLUE);
        g2d.drawRect(xMin * 4, yMin * 4, (xMax - xMin) * 4, (yMax - yMin) * 4);

        for (Vektor vektor : vektors) {
            // Menggambar garis asli
            g2d.setColor(Color.RED);
            g2d.draw(new Line2D.Double(vektor.x1 * 4, vektor.y1 * 4, vektor.x2 * 4, vektor.y2 * 4));

            if (vektor.koordinatTerpotong != null) {
                int x1Clip = (int) (vektor.koordinatTerpotong[0] * 4);
                int y1Clip = (int) (vektor.koordinatTerpotong[1] * 4);
                int x2Clip = (int) (vektor.koordinatTerpotong[2] * 4);
                int y2Clip = (int) (vektor.koordinatTerpotong[3] * 4);

                // Menggambar garis yang sudah dipotong
                g2d.setColor(Color.GREEN);
                g2d.draw(new Line2D.Double(x1Clip, y1Clip, x2Clip, y2Clip));

                // Menampilkan nama vektor
                g2d.drawString(vektor.nama, (x1Clip + x2Clip) / 2, (y1Clip + y2Clip) / 2);

                // Menampilkan kategori garis
                g2d.drawString(vektor.kategori, (x1Clip + x2Clip) / 2, (y1Clip + y2Clip)/ 2 + 10);
            }
        }

        if (sedangMenggambar) {
            // Menggambar garis yang sedang digambar
            g2d.setColor(Color.RED);
            g2d.draw(new Line2D.Double(x1 * 4, y1 * 4, x2 * 4, y2 * 4));
        }
    }

    private double[] liangBarsky(int xMin, int yMin, int xMax, int yMax, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x1 - xMin, xMax - x1, y1 - yMin, yMax - y1}; 
        double tEnter = 0.0;
        double tExit = 1.0;

        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {  
                if (q[i] < 0) {
                    return null;  
                }
            } else {
                double t = q[i] / p[i];
                if (p[i] < 0) {
                    if (t > tEnter) {
                        tEnter = t;
                    }
                } else {
                    if (t < tExit) {
                        tExit = t;
                    }
                }
            }
        }

        if (tEnter > tExit) {
            return null;  // Garis berada sepenuhnya di luar
        }

        double x1Clip = x1 + tEnter * dx;
        double y1Clip = y1 + tEnter * dy;
        double x2Clip = x1 + tExit * dx;
        double y2Clip = y1 + tExit * dy;

        return new double[]{x1Clip, y1Clip, x2Clip, y2Clip};
    }

    private String kategorikanGaris(int x1, int y1, int x2, int y2, double[] koordinatTerpotong) {
        if (koordinatTerpotong == null) {
            return "Invisible";
        } else if (koordinatTerpotong[0] == x1 && koordinatTerpotong[1] == y1 && koordinatTerpotong[2] == x2 && koordinatTerpotong[3] == y2) {
            return "Visible";
        } else if ((koordinatTerpotong[0] == x1 && koordinatTerpotong[1] == y1) || (koordinatTerpotong[2] == x2 && koordinatTerpotong[3] == y2)) {
            return "Half Partial";
        } else {
            return "Full Partial";
        }
    }

    private void cetakDetailVektor(Vektor vektor) {
        System.out.println(vektor.nama + ":");
        System.out.println("Koordinat Asli: (" + vektor.x1 + ", " + vektor.y1 + ") ke (" + vektor.x2 + ", " + vektor.y2 + ")");
        if (vektor.koordinatTerpotong!= null) {
            System.out.println("Koordinat Terpotong: (" + vektor.koordinatTerpotong[0] + ", " + vektor.koordinatTerpotong[1] + ") ke (" +
                    vektor.koordinatTerpotong[2] + ", " + vektor.koordinatTerpotong[3] + ")");
        } else {
            System.out.println("Garis berada di luar jendela pemotongan");
        }
        System.out.println("Kategori: " + vektor.kategori);
        System.out.println();
    }

    private void hapusGarisDiluarKotak() {
        // Buat salinan list agar tidak terjadi masalah pada saat penghapusan
        ArrayList<Vektor> vektorsCopy = new ArrayList<>(vektors);
    
        // Iterasi melalui list dan hapus vektor yang berada di luar kotak pemotongan dan berwarna merah
        for (int i = vektorsCopy.size() - 1; i >= 0; i--) {
            Vektor vektor = vektorsCopy.get(i);
            if (vektor.kategori.equals("Invisible") || vektor.koordinatTerpotong == null) {
                vektors.remove(vektor);
            } else {
                vektor.x1 = (int) vektor.koordinatTerpotong[0];
                vektor.y1 = (int) vektor.koordinatTerpotong[1];
                vektor.x2 = (int) vektor.koordinatTerpotong[2];
                vektor.y2 = (int) vektor.koordinatTerpotong[3];
            }
        }
        repaint();
    }


    // Kelas untuk menyimpan informasi vektor
    private static class Vektor {
        String nama;    
        int x1, y1, x2, y2;
        double[] koordinatTerpotong;
        String kategori;

        Vektor(String nama, int x1, int y1, int x2, int y2) {
            this.nama = nama;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        void setKoordinatTerpotong(double[] koordinatTerpotong) {
            this.koordinatTerpotong= koordinatTerpotong;
        }

        void setKategori(String kategori) {
            this.kategori = kategori;
        }
    }
}
