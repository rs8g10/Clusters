package testClusters;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class testClusters extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private final String imagePath = "testImage.jpg";
	
	public testClusters() {
		initPanel();
		readImage();
	}
	
	private void readImage() {
		new Thread(new Runnable() {
			public void run() {
				File imageFile = new File(imagePath);
				BufferedImage bf = null;
				try {
					bf = ImageIO.read(imageFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (bf != null) {
					byte[] bytes = ((DataBufferByte) bf.getRaster().getDataBuffer()).getData();
					short[] pixels = new short[bytes.length / 3];
					for (int i = 0, j = 0; i < bytes.length; i+= 3, j++) {
						short pixel = bytes[i];
						if (pixel < 0) {
							pixel = (short) (pixel + 256);
						}
						pixels[j] = pixel;
						//System.out.println(pixels[j]);
					}
					transformImage(pixels, bf.getWidth(), bf.getHeight());
				}	
			}
		}).start();
	}
	
	private void transformImage(short[] pixels, int w, int h) {
		//System.out.println(pixels.length);
		
		
		
		int[] newPixels = new int[pixels.length];
		
		float dump_value = 0.1f;
		float dump_dist = 3.5f;
		int threshold = 4000;
		int maxValue = 0;
		
		int tx = 20;
		int ty = 20;
		int tdiff = 6;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int index = j * w + i;
				int value = (int) dump_value * pixels[index];
				if (value == 0) {
					value = 1;
				}
				int totalValue = 0;
				for (int a = i - tx/2; a < i + tx/2; a+= tdiff) {
					for (int b = j - ty/2; b < j + ty/2; b+= tdiff) {
						if (a >= 0 && a < w && b >= 0 && b < h && !(a == i && b == j)) {
							int v = pixels[b * w + a];
							int dist = (int) Math.sqrt(Math.pow(a - i, 2) + Math.pow(b - j, 2));
							totalValue+= (v / dist);
						}
					}
				}
				totalValue*= dump_dist * value;
				if (totalValue > threshold) {
					totalValue = threshold;
				}
				newPixels[index] = totalValue;
				if (totalValue > maxValue) {
					maxValue = totalValue;
				}
			}
		}
		
		for (int i = 0; i < newPixels.length; i++) {
			//System.out.println("Before: " + pixels[i] + ", after: " + newPixels[i]);
			newPixels[i] = (int) (((float) newPixels[i] / maxValue) * 255);
			//System.out.println("Now : " + newPixels[i]);
		}
		
		int[] rgb = new int[newPixels.length];
		for (int i = 0; i < newPixels.length; i++) {
			if (newPixels[i] > 0) {
				rgb[i] = (newPixels[i] << 16) + (newPixels[i]);
			}
		}
		BufferedImage bf = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    int[] imgData = ((DataBufferInt) bf.getRaster().getDataBuffer()).getData();
	    System.arraycopy(rgb, 0, imgData, 0, newPixels.length);  
	    try {
			ImageIO.write(bf, "jpg", new File("outputImage.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel label = new JLabel(new ImageIcon(bf));
		label.setOpaque(false);
		add(label);
		this.repaint();
	}
	
	private void initPanel() {
		setPreferredSize(new Dimension(500, 500));
	}
	
	private static void createAndShowGUI() {
		JPanel testClusters = new testClusters();
		JFrame frame = new JFrame("Test Clusters");
		frame.setContentPane(testClusters);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] argv) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
