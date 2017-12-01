package Infection;

public class Infection {
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Game();
            }
        });
    }
}
