import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class EV3Client extends JFrame implements KeyListener {

    private Socket socket;
    private DataOutputStream dos;
    private Map<Integer, KeyPressThread> keyPressThreads = new HashMap<>();
    private int currentSpeed = 1100; // Velocità predefinita
    private int speedturn = 500;

    public EV3Client() {
        // Configura la finestra
        setTitle("EV3 Control");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 3));

        // Crea i pulsanti
        JButton btnForward = new JButton("W (Avanti)");
        JButton btnLeft = new JButton("A (Sinistra)");
        JButton btnBackward = new JButton("S (Indietro)");
        JButton btnRight = new JButton("D (Destra)");
        JButton btnStop = new JButton("Stop"); // Stop button

        // Aggiungi i pulsanti alla finestra
        add(new JLabel()); // empty cell
        add(btnForward);
        add(new JLabel()); // empty cell
        add(btnLeft);
        add(btnStop); // Stop button added
        add(btnRight);
        add(new JLabel()); // empty cell
        add(btnBackward);
        add(new JLabel()); // empty cell
        add(new JLabel()); // empty cell
        add(new JLabel()); // empty cell
        add(new JLabel()); // empty cell

        // Configura i pulsanti
        btnForward.addActionListener(createButtonActionListener(KeyEvent.VK_W));
        btnLeft.addActionListener(createButtonActionListener(KeyEvent.VK_A));
        btnBackward.addActionListener(createButtonActionListener(KeyEvent.VK_S));
        btnRight.addActionListener(createButtonActionListener(KeyEvent.VK_D));
        btnStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAllMovements();
            }
        });

        // Connessione al server
        try {
            socket = new Socket("10.0.1.1", 1234); // Inserisci l'IP del tuo EV3
            dos = new DataOutputStream(socket.getOutputStream());
            playConnectionSound();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Errore di connessione: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }

        // Aggiungi il KeyListener
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Mostra la finestra
        setVisible(true);

        // Richiedi il focus per catturare i tasti
        requestFocusInWindow();
    }

    private ActionListener createButtonActionListener(final int keyCode) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startKeyPressThread(keyCode);
            }
        };
    }

    private void sendSpeedCommand(int command, int speed) {
        try {
            dos.writeInt(command);
            dos.writeInt(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playConnectionSound() {
        try {
            File soundFile = new File("C:\\Users\\Studenti\\eclipse-workspace\\ClientRamonPC\\src\\mcdonalds-jingle.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_1) {
            // Modifica la velocità a una certa quantità (ad esempio +1000)
            currentSpeed = 1100;
            speedturn = 700;
            System.out.println("Velocità a: " + currentSpeed);
        } else if (keyCode == KeyEvent.VK_2) {
            // Modifica la velocità a una certa quantità (ad esempio -1000)
            currentSpeed = 5000;
            speedturn = 1100;
            System.out.println("Velocità a: " + currentSpeed);
        } else {
            startKeyPressThread(keyCode);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode != KeyEvent.VK_1 && keyCode != KeyEvent.VK_2) {
            stopKeyPressThread(keyCode);
        }
    }

    private void startKeyPressThread(int key) {
        if (!keyPressThreads.containsKey(key)) {
            KeyPressThread thread = new KeyPressThread(key);
            keyPressThreads.put(key, thread);
            thread.start();
        }
    }

    private void stopKeyPressThread(int key) {
        KeyPressThread thread = keyPressThreads.remove(key);
        if (thread != null) {
            thread.stopRunning();
            sendSpeedCommand(0, 0); // Stop the command when key is released
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Non utilizzato
    }

    private void stopAllMovements() {
        for (KeyPressThread thread : keyPressThreads.values()) {
            thread.stopRunning();
        }
        keyPressThreads.clear();
        sendSpeedCommand(0, 0); // Stop all commands
    }

    private class KeyPressThread extends Thread {
        private final int key;
        private boolean running = true;

        KeyPressThread(int key) {
            this.key = key;
        }

        @Override
        public void run() {
            while (running) {
                switch (key) {
                    case KeyEvent.VK_W:
                        sendSpeedCommand(1, currentSpeed);
                        break;
                    case KeyEvent.VK_A:
                        sendSpeedCommand(3, speedturn);
                        break;
                    case KeyEvent.VK_S:
                        sendSpeedCommand(2, currentSpeed);
                        break;
                    case KeyEvent.VK_D:
                        sendSpeedCommand(4, speedturn);
                        break;
                }
                try {
                    Thread.sleep(10); // Maintain speed every 100ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void stopRunning() {
            running = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final EV3Client client = new EV3Client();
                client.addWindowFocusListener(new WindowAdapter() {
                    public void windowGainedFocus(WindowEvent e) {
                        client.requestFocusInWindow();
                    }
                });
            }
        });
    }
}