package p14;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
	private JButton button = null;
	private JPanel panel = null;
	private JLabel label = null;
	private JComboBox comboBox = null;
	private int index = 0;

	private MainFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Testing Ground");
		setLayout(new BorderLayout());
		//setSize(400, 300);

		button = new JButton("Start");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				label.setText("" + ++index);
				if(index == 4){
					comboBox.setSelectedIndex(2);
				}
			}
		});

		comboBox = new JComboBox(new String[]{"first", "second item in combo", "third"});
		comboBox.setName("My combo");
		
		label = new JLabel("0");
		label.setHorizontalAlignment(JLabel.CENTER);
		panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));

		panel.add(label);
		panel.add(comboBox);

		add(panel, BorderLayout.CENTER);
		add(button, BorderLayout.SOUTH);
	}

	public static void main(String[] args) {
		MainFrame f = new MainFrame();
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
