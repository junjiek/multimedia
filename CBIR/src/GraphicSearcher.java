
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


public class GraphicSearcher extends JFrame {
	int row = 5, col = 4;
	JButton chooseFileButton = new JButton("choose file");
	JButton run16BinsButton = new JButton("run (16 bins)");
	JButton run128BinsButton = new JButton("run (128 bins)");
	JLabel queryImg = new JLabel();
	String queryFilePath = null;
	ImageSearcher imgeSearcher16 = null;
	ImageSearcher imgeSearcher128 = null;
	JLabel[] resImg = new JLabel[row * col];
	String[] distType = {"L2", "HI", "Bh", "Chi"};
	ImageSearcher.PathDistPair[] resFilePath;
	String allImgFilename = "./AllImages.txt";
	JComboBox<String> distTypeChooser = new JComboBox<String>(distType);
	public GraphicSearcher() {
		this.setTitle("GraphicSearcher");
		this.setLayout(new BorderLayout());
		JPanel rightJPanel = new JPanel(new GridLayout(row, col, 10, 10));
		
        for (int i = 0; i < row * col; i++) {
        	resImg[i] = new JLabel();
        	resImg[i].setBorder(BorderFactory.createTitledBorder(""));
        	rightJPanel.add(resImg[i]);
        }
        JPanel leftJPanel = new JPanel(new BorderLayout());
        
        
        JPanel northPanel = new JPanel(new FlowLayout());
        JPanel centerPanel = new JPanel(new FlowLayout());
        JPanel southPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        northPanel.add(chooseFileButton);
        northPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        centerPanel.add(queryImg);
        centerPanel.setBorder(BorderFactory.createTitledBorder("图片"));
        distTypeChooser.setSelectedIndex(0);
        JPanel south1 = new JPanel(new FlowLayout());
        south1.add(new JLabel("dist type: "));
        south1.add(distTypeChooser);
        JPanel south2 = new JPanel(new FlowLayout());
        south2.add(run16BinsButton);
        south2.add(run128BinsButton);
        southPanel.add(south1);
        southPanel.add(south2);
        southPanel.setBorder(BorderFactory.createEmptyBorder(80, 60, 300, 60));
        leftJPanel.add(northPanel, "North");
        leftJPanel.add(centerPanel, "Center");
        leftJPanel.add(southPanel, "South");
        run16BinsButton.addActionListener(new runAction());
        run128BinsButton.addActionListener(new runAction());
        
        chooseFileButton.addActionListener(new chooseFileAction());
        this.add(leftJPanel, "West");
        this.add(rightJPanel, "Center");
        this.setSize(1300, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	private void displayResult() {
		for (int i = 0; i < row * col; i++) {
			ImageIcon icon = new ImageIcon(resFilePath[i].path);
			double ratio = (double)icon.getIconHeight() / icon.getIconWidth();
			icon.setImage(icon.getImage().getScaledInstance(200, (int)(200 *ratio), Image.SCALE_DEFAULT));
			java.text.DecimalFormat df = new java.text.DecimalFormat("#0.0000");  
			resImg[i].setBorder(BorderFactory.createTitledBorder(df.format(resFilePath[i].dist)));
			resImg[i].setIcon(icon);
		}
		
	}
	class runAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (queryFilePath == null) return;
			if ((JButton)e.getSource() == run16BinsButton) {
				if (imgeSearcher16 == null) {
					imgeSearcher16 = new ImageSearcher();
					imgeSearcher16.buildImgHistogram(allImgFilename, 16);
				}
				resFilePath = imgeSearcher16.search(queryFilePath, distType[distTypeChooser.getSelectedIndex()]);
				displayResult();
			} else if ((JButton)e.getSource() == run128BinsButton) {
				if (imgeSearcher128 == null) {
					imgeSearcher128 = new ImageSearcher();
					imgeSearcher128.buildImgHistogram(allImgFilename, 128);
				}
				resFilePath = imgeSearcher128.search(queryFilePath, distType[distTypeChooser.getSelectedIndex()]);
				displayResult();
			}
		}
	}
	class chooseFileAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if ((JButton)e.getSource() == chooseFileButton) {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter extensionFilter =
						new FileNameExtensionFilter("图片文件 (jpg, jpeg, png)", "jpg", "png", "jpeg");
				fileChooser.setFileFilter(extensionFilter);
				int returnval = fileChooser.showOpenDialog(null);
				if (returnval == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					queryFilePath = file.getPath();
					ImageIcon icon = new ImageIcon(queryFilePath);
					double ratio = (double)icon.getIconHeight() / icon.getIconWidth();
					icon.setImage(icon.getImage().getScaledInstance(350, (int)(350 *ratio), Image.SCALE_DEFAULT));
					queryImg.setIcon(icon);
				}
			}
		}
	}
	public static void main(String[] args) {
		GraphicSearcher GraphicSearcher = new GraphicSearcher();
	}
	
	
}
