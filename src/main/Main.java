package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import com.fazecast.jSerialComm.SerialPort;

public class Main {
	static SerialPort chosenPort;
	static JFrame window;
	static XYSeries series;
	static XYSeriesCollection dataset;
	static int x = 0;

	public static void main(String[] args) {
		// cria a janela com os botoes e graficos
		window = new JFrame();
		window.setTitle("Sensor Grafico GUI");
		window.setSize(600, 400);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// cria a drop-dow box e botao de conecao
		final JComboBox<String> portList = new JComboBox<String>();
		final JButton connectButton = new JButton("Conectar");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		window.add(topPanel, BorderLayout.NORTH);

		// colocar dados no drop-down box
		SerialPort[] portNames = SerialPort.getCommPorts();
		for (int i = 0; i < portNames.length; i++) {
			portList.addItem(portNames[i].getSystemPortName());
		}

		// cria a linha grafica
		series = new XYSeries("Leitura Sensor Luz");
	    dataset = new XYSeriesCollection(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Leitura Sensor Luz ", "Tempo (Segundos)", "Leitura ACD",
				dataset);
		window.add(new ChartPanel(chart), BorderLayout.CENTER);

		// configuracao do botao
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (connectButton.getText().equals("Conectar")) {
					// conecta a porta
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if (chosenPort.openPort()) {
						connectButton.setText("Discconectar");
						portList.setEditable(false);
					}
					// cria a nova thread
					Thread thread = new Thread() {
						@Override
						public void run() {
							Scanner scanner = new Scanner(chosenPort.getInputStream());
							while (scanner.hasNextLine()) {
								try {
									String line = scanner.nextLine();
									int number = Integer.parseInt(line);
									series.add(x++, 1023 - number);
									window.repaint();
								} catch (Exception e) {
								}
							}
							scanner.close();
						}
					};
					thread.start();
				} else {
					// disconeta a porta
					chosenPort.closePort();
					portList.setEditable(true);
					connectButton.setText("Conectar");
					series.clear();
					x = 0;
				}

			}
		});

		// mostra a janela
		window.setVisible(true);

	}

}
