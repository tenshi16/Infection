package Infection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Game extends javax.swing.JFrame implements Runnable{
    //THREADS
    Thread LoadGame;    //Thread de carga del juego
    Thread PlayGame;    //Thread principal

    //CONSTANTES DEL JUEGO
    static final int DELAY     = 50;        // Milisegundos entre recargas de la pantalla.
    static final int MAX_LIFE = 3000;          // Cantidad de vidas al iniciar el juego.
    static final int BOSS_LIFE = 100;       // Cantidad de vidas del Linfocito.
    
    static final int MAX_SHOTS =  6;        // Cantidad máxima de sprites (Toxinas).
    static final int MAX_ANTIBODIES = 8;    // Cantidad máxima de Anticuerpos.
    static final int MAX_RBC   =  8;        // Cantidad máxima de glóbulos rojos.

    static final int SCRAP_COUNT  = 30;     // Valor incial del contador de trozos.
    static final int VIRUS_PASSES =  3;     // Máximo de veces que aparecerán los virus enemigos.

    static final int MIN_RBC_SPEED =  2;     // Velocidad mínima de un glóbulo rojo.
    static final int MAX_RBC_SPEED = 12;     // Velocidad máxima de un glóbulo rojo.

    static final int MIN_RBC_SIZE  = 20;     // Tamaño mínimo de un glóbulo rojo.
    static final int MAX_RBC_SIZE  = 40;     // Tamaño máximo de un glóbulo rojo.

    static final int RBC_1    =  25;         // Puntos ganados al disparar a un glóbulo rojo por primera vez.
    static final int RBC_2  =  50;           // Puntos ganados al disparar a un glóbulo rojo por segunda vez.
    static final int VIRUS_POINTS  = 250;    // Puntos ganados al disparar a virus enemigos.
    static final int DNA_POINTS = 500;       // Puntos ganados al esquivar o acabar con el DNA de virus enemigos.
    static final int BOSS_POINTS = 1000;     // Puntos ganados al disparar al Linfocito.
    static final int ANTIBODY_POINTS = 700;  // Puntos ganados al disparar a un Anticuerpo.
    static final int HIT_POINTS = 100;       // Puntos restados al ser golpeado o perder una vida.
    
    //DATOS DEL JUEGO
    int score;                               // Puntaje actual del jugador
    int level;                               // Nivel actual del juego
    String name,tempname;                    // Nombre del Jugador
    //int[] levelpts={25,50,100}; 
    int[] levelpts={1500,3000,5000};         // Puntaje para pasar por nivel
    List<String[]> players;                  //Tabla de Puntajes

    //Estatus del Menú
    boolean first=true;                      // Responde a: ¿Es la primera pantalla del juego?
    boolean paused;                          // Responde a: ¿El juego está pausado?
    boolean playing;                         // Responde a: ¿El juego está activo?
    boolean[] help=new boolean[3];           // (Des)activa las instrucciones.
    boolean table;                           // (Des)activa la Tabla de Puntajes
    boolean restart,exit;                    // (Des)activa la confirmación para reiniciar y salir del juego respectivamente.
    boolean showmenu=true;                   // (Des)activa el menú de la Pausa.
    boolean enablegetnames=true;             // (Des)bloquea el acceso al método getnames para evitar gastos de memoria.
    boolean debugmode;                       // Activa el modo de Depuración del Juego
    
    // Dirección activa del jugador
    boolean left  = false;
    boolean right = false;
    boolean up    = false;
    boolean down  = false;

    // Sprites
    Sprite player;                                      // Jugador
    Sprite virus;                                       // Virus
    Sprite dna;                                         // ADN del Virus
    Sprite lymphocyte;                                  // Linfocito
    Sprite[] antibody = new Sprite[MAX_ANTIBODIES];     // Anticuerpos del Linfocito
    Sprite[] toxins = new Sprite[MAX_SHOTS];            // Toxinas del Jugador.
    Sprite[] RBC    = new Sprite[MAX_RBC];              // Glóbulos Rojos.

    // Datos del Jugador
    int lifeLeft;                               // Contador de vidas, incluyendo la actual.

    // Datos de Toxinas.
    int[] toxinCounter = new int[MAX_SHOTS];    // Contador para Tiempo de Vida de una toxina.
    int   toxinIndex;                           // Siguiente Sprite de toxina disponible.
    
    // Datos de Virus.
    int VirusPassesLeft;                        // Número de pasadas de un virus.
    int VirusCounter;                           // Contador de tiempo para cada pasada.
    int VirusAnimation=0;                       // Auxiliar para la animación del Virus
    
    // Datos del Linfocito y sus Anticuerpos
    int antibodyCounter = MAX_ANTIBODIES;       // Auxiliar para la animación del ataque del Linfocito
    int lymphocyteLife = BOSS_LIFE;             // Vida del Linfocito
    int lymphocyteShield = 5;                   // Escudo del Linfocito
    
    // Datos de DNA, lanzado por Virus.
    int dnaCounter;                              // Contador para Tiempo de Vida del DNA.

    // Datos de los Glóbulos Rojos
    boolean[] attackedRBC = new boolean[MAX_RBC]; // Responde a: ¿Este glóbulo rojo ya fue atacado una vez?
    int       RBCCounter;                         // Contador de desaparición de glóbulo rojo
    int       RBCSpeed;                           // Velocidad del glóbulo rojo
    int       RBCLeft;                            // Número de glóbulos rojos activos.

    // Valores de la imagen Offscreen.
    Dimension offDimension;
    Image offImage;
    Graphics2D offGraphics;

    // Datos de la Fuente
    Font font = new Font("Helvetica", Font.BOLD, 12);
    FontMetrics fm;
    int fontWidth,fontHeight;

    //Variables de Imágenes
    static BufferedImage GlobuloRojo1,GlobuloRojo2,BaseVida,Vida,playerImage,playerBullet;
    static BufferedImage Fondo1,Fondo2,Fondo3,PuntosInf,VirusVida,aux,Barra,pause1,pause2;
    static BufferedImage[] Lymphocyte=new BufferedImage[6];
    static BufferedImage[] Virus=new BufferedImage[8];
    static BufferedImage[] Puntos = new BufferedImage[10];
    
    public Game() {
        initComponents();
        this.setTitle("I N F E C T I O N");
        this.setIconImage(new ImageIcon(Game.class.getResource("/img/icon.png")).getImage());
        name = tempname = "";
        Properties();           //Inicializa Propiedades del Juego
        start();                //Inicializa las propiedades del Runnable
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        if(!first&&name.trim().isEmpty()){              //Usado para guardar el nombre de usuario
            if(evt.getKeyCode()==Event.BACK_SPACE){     //Si se presionó la tecla Borrar
                if(tempname.length()>0)                 //Para validar que realmente haya algo que borrar y así controlar excepciones.
                    tempname=tempname.substring(0,tempname.length()-1);
            }
            else if(evt.getKeyCode()==Event.ENTER){     //Nombre definitivo, el juego puede iniciar.
                name=tempname;
                keyDown(Event.ENTER);
            }
            else //Si es cualquier otra letra.
                if((Character.isLetter(evt.getKeyChar()) ||     //Máximo de 20 caracteres, sólo letras y números.
                        Character.isDigit(evt.getKeyChar())) &&
                        tempname.length()<20)
                    tempname+=evt.getKeyChar();
        }
        else keyDown(evt.getKeyCode());                 //Ya hay nombre de usuario guardado, uso normal del listener en el juego.
    }//GEN-LAST:event_formKeyPressed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        keyUp(evt.getKeyCode());
    }//GEN-LAST:event_formKeyReleased

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Game.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Game().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    private void Properties() {
        //Guardado en búfer de imágenes.
        try{
            GlobuloRojo1 = ImageIO.read(Game.class.getResource("/img/Globulo1.png"));
            GlobuloRojo2 = ImageIO.read(Game.class.getResource("/img/Globulo2.png"));
            BufferedImage aux=ImageIO.read(Game.class.getResource("/img/Virus.png"));
            for(int i=0;i<aux.getWidth();i+=49)
                Virus[i/49]=aux.getSubimage(i, 0, 49, 67);
            aux = ImageIO.read(Game.class.getResource("/img/Linfocito.png"));
            for(int i=0;i<aux.getWidth();i+=140)
                Lymphocyte[i/140]=aux.getSubimage(i, 0, 140, 140);
            Fondo1 = ImageIO.read(Game.class.getResource("/img/Fondo1.png"));
            Fondo2 = ImageIO.read(Game.class.getResource("/img/Fondo2.png"));
            Fondo3 = ImageIO.read(Game.class.getResource("/img/Fondo3.png"));
            BaseVida = ImageIO.read(Game.class.getResource("/img/Base_vidas.png"));
            aux = ImageIO.read(Game.class.getResource("/img/Puntos.png"));
            for(int i=0;i<aux.getWidth();i+=10)
                Puntos[i/10]=aux.getSubimage(0, 0, i+10, 11);
            Barra = ImageIO.read(Game.class.getResource("/img/Barra.png"));
            PuntosInf = ImageIO.read(Game.class.getResource("/img/PuntosInf.png"));
            VirusVida = ImageIO.read(Game.class.getResource("/img/VirusVidas.png"));  
            playerImage = ImageIO.read(Game.class.getResource("/img/player.png"));
            playerBullet = ImageIO.read(Game.class.getResource("/img/playerBullet.png"));
            pause1 = ImageIO.read(Game.class.getResource("/img/FondoPausa.png"));
            pause2 = ImageIO.read(Game.class.getResource("/img/FondoPausa2.png"));
        } catch (Exception e) {
            int i=JOptionPane.showConfirmDialog(this,
                        "Ha ocurrido un error al cargar INFECTION. Por favor, inténtelo nuevamente.",
                        "¡Error!",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.ERROR_MESSAGE);
            if(i==JOptionPane.OK_OPTION||i==JOptionPane.CANCEL_OPTION) System.exit(-1);
        }

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        Graphics g=getGraphics();

        // Crea la forma del Sprite del Jugador.
        player = new Sprite();
        player.shape.addPoint(-8, 28);
        player.shape.addPoint(8, 28);
        player.shape.addPoint(8, -28);
        player.shape.addPoint(-8, -28);

        // Crea la forma de las toxinas del jugador.
        for (int i = 0; i < MAX_SHOTS; i++) {
          toxins[i] = new Sprite();
          toxins[i].shape.addPoint(-10, 33);
          toxins[i].shape.addPoint(10, 33);
          toxins[i].shape.addPoint(10, -33);
          toxins[i].shape.addPoint(-10, -33);
        }

        // Crea la forma de los virus enemigos.
        virus = new Sprite();
        virus.shape.addPoint(1,0);
        virus.shape.addPoint(8,6);
        virus.shape.addPoint(8,20);
        virus.shape.addPoint(1,25);
        virus.shape.addPoint(5,27);
        virus.shape.addPoint(9,35);
        virus.shape.addPoint(5,28);
        virus.shape.addPoint(4,48);
        virus.shape.addPoint(9,41);
        virus.shape.addPoint(14,56);
        virus.shape.addPoint(9,43);
        virus.shape.addPoint(5,48);
        virus.shape.addPoint(5,53);
        virus.shape.addPoint(14,44);
        virus.shape.addPoint(22,55);
        virus.shape.addPoint(14,45);
        virus.shape.addPoint(6,53);
        virus.shape.addPoint(6,58);
        virus.shape.addPoint(6,59);
        virus.shape.addPoint(0,59);
        virus.shape.addPoint(-0,59);
        virus.shape.addPoint(-6,59);
        virus.shape.addPoint(-6,58);
        virus.shape.addPoint(-6,53);
        virus.shape.addPoint(-14,45);
        virus.shape.addPoint(-22,55);
        virus.shape.addPoint(-14,44);
        virus.shape.addPoint(-5,53);
        virus.shape.addPoint(-5,48);
        virus.shape.addPoint(-9,43);
        virus.shape.addPoint(-14,56);
        virus.shape.addPoint(-9,41);
        virus.shape.addPoint(-4,48);
        virus.shape.addPoint(-5,28);
        virus.shape.addPoint(-9,35);
        virus.shape.addPoint(-5,27);
        virus.shape.addPoint(-1,25);
        virus.shape.addPoint(-8,20);
        virus.shape.addPoint(-8,6);
        virus.shape.addPoint(-1,0);

        // Crea la forma del DNA de los virus enemigos.
        dna = new Sprite();
        dna.shape.addPoint(-2,-4);
        dna.shape.addPoint(-1,4);
        dna.shape.addPoint(0,-4);
        dna.shape.addPoint(1,4);
        dna.shape.addPoint(2,-4);

        // Crea la forma de los Sprites de Glóbulos Rojos
        for (int i = 0; i < MAX_RBC; i++)
            RBC[i] = new Sprite();
        
        // Crea la forma del Linfocito
        lymphocyte = new Sprite();
        lymphocyte.shape.addPoint(45,12);
        lymphocyte.shape.addPoint(56,9);
        lymphocyte.shape.addPoint(60,16);
        lymphocyte.shape.addPoint(65,14);
        lymphocyte.shape.addPoint(68,12);
        lymphocyte.shape.addPoint(73,15);
        lymphocyte.shape.addPoint(82,15);
        lymphocyte.shape.addPoint(87,16);
        lymphocyte.shape.addPoint(91,18);
        lymphocyte.shape.addPoint(93,21);
        lymphocyte.shape.addPoint(98,23);
        lymphocyte.shape.addPoint(104,18);
        lymphocyte.shape.addPoint(110,22);
        lymphocyte.shape.addPoint(110,27);
        lymphocyte.shape.addPoint(108,31);
        lymphocyte.shape.addPoint(115,37);
        lymphocyte.shape.addPoint(115,40);
        lymphocyte.shape.addPoint(119,39);
        lymphocyte.shape.addPoint(123,45);
        lymphocyte.shape.addPoint(120,48);
        lymphocyte.shape.addPoint(121,50);
        lymphocyte.shape.addPoint(123,51);
        lymphocyte.shape.addPoint(124,55);
        lymphocyte.shape.addPoint(122,56);
        lymphocyte.shape.addPoint(123,59);
        lymphocyte.shape.addPoint(123,62);
        lymphocyte.shape.addPoint(133,62);
        lymphocyte.shape.addPoint(133,66);
        lymphocyte.shape.addPoint(130,70);
        lymphocyte.shape.addPoint(126,74);
        lymphocyte.shape.addPoint(123,73);
        lymphocyte.shape.addPoint(126,77);
        lymphocyte.shape.addPoint(127,86);
        lymphocyte.shape.addPoint(122,88);
        lymphocyte.shape.addPoint(120,90);
        lymphocyte.shape.addPoint(118,95);
        lymphocyte.shape.addPoint(115,101);
        lymphocyte.shape.addPoint(110,106);
        lymphocyte.shape.addPoint(112,108);
        lymphocyte.shape.addPoint(115,110);
        lymphocyte.shape.addPoint(117,111);
        lymphocyte.shape.addPoint(114,116);
        lymphocyte.shape.addPoint(109,118);
        lymphocyte.shape.addPoint(105,122);
        lymphocyte.shape.addPoint(98,117);
        lymphocyte.shape.addPoint(92,120);
        lymphocyte.shape.addPoint(85,122);
        lymphocyte.shape.addPoint(79,130);
        lymphocyte.shape.addPoint(77,128);
        lymphocyte.shape.addPoint(73,129);
        lymphocyte.shape.addPoint(69,125);
        lymphocyte.shape.addPoint(63,124);
        lymphocyte.shape.addPoint(58,125);
        lymphocyte.shape.addPoint(56,128);
        lymphocyte.shape.addPoint(57,132);
        lymphocyte.shape.addPoint(52,130);
        lymphocyte.shape.addPoint(53,122);
        lymphocyte.shape.addPoint(47,123);
        lymphocyte.shape.addPoint(45,119);
        lymphocyte.shape.addPoint(39,118);
        lymphocyte.shape.addPoint(37,124);
        lymphocyte.shape.addPoint(34,125);
        lymphocyte.shape.addPoint(31,120);
        lymphocyte.shape.addPoint(31,117);
        lymphocyte.shape.addPoint(35,114);
        lymphocyte.shape.addPoint(30,108);
        lymphocyte.shape.addPoint(23,102);
        lymphocyte.shape.addPoint(18,89);
        lymphocyte.shape.addPoint(13,90);
        lymphocyte.shape.addPoint(9,82);
        lymphocyte.shape.addPoint(18,76);
        lymphocyte.shape.addPoint(15,72);
        lymphocyte.shape.addPoint(14,69);
        lymphocyte.shape.addPoint(11,67);
        lymphocyte.shape.addPoint(12,57);
        lymphocyte.shape.addPoint(15,54);
        lymphocyte.shape.addPoint(19,47);
        lymphocyte.shape.addPoint(20,44);
        lymphocyte.shape.addPoint(24,43);
        lymphocyte.shape.addPoint(24,40);
        lymphocyte.shape.addPoint(21,38);
        lymphocyte.shape.addPoint(25,32);
        lymphocyte.shape.addPoint(30,32);
        lymphocyte.shape.addPoint(35,27);
        lymphocyte.shape.addPoint(35,21);
        lymphocyte.shape.addPoint(45,17);
        
        //Crea la forma de los anticuerpos. 
        for(int i = 0; i < MAX_ANTIBODIES; i++){
            antibody[i] = new Sprite();
            antibody[i].shape.addPoint(0,3);
            antibody[i].shape.addPoint(1,2);
            antibody[i].shape.addPoint(2,1);
            antibody[i].shape.addPoint(2,0);
            antibody[i].shape.addPoint(4,0);
            antibody[i].shape.addPoint(4,3);
            antibody[i].shape.addPoint(3,4);
            antibody[i].shape.addPoint(2,5);
            antibody[i].shape.addPoint(1,6);
            antibody[i].shape.addPoint(1,9);
            antibody[i].shape.addPoint(-1,9);
            antibody[i].shape.addPoint(-1,6);
            antibody[i].shape.addPoint(-2,5);
            antibody[i].shape.addPoint(-3,4);
            antibody[i].shape.addPoint(-4,3);
            antibody[i].shape.addPoint(-4,0);
            antibody[i].shape.addPoint(-2,0);
            antibody[i].shape.addPoint(-2,1);
            antibody[i].shape.addPoint(-1,2);
            antibody[i].shape.addPoint(0,3);
        }
        
        // Carga los datos de la Fuente
        g.setFont(font);
        fm = g.getFontMetrics();
        fontWidth = fm.getMaxAdvance();
        fontHeight = fm.getHeight();

        // Inicializa el juego y coloca al jugador en modo de "Fin del Juego"
        initGame();
        endGame();
    }

    private void initGame() {
        //Inicializa los datos del juego y los Sprites.
        score = 0;
        level = 0;
        lifeLeft = MAX_LIFE;
        RBCSpeed = MIN_RBC_SPEED;
        lymphocyteLife=BOSS_LIFE;
        initPlayer();
        initToxins();
        stopVirus();
        stopDNA();
        stopBoss();
        stopAntibodies();
        initRBC();
        playing = true;
        paused = table = false;
    }

    private void endGame() {
        // Detiene al jugador, los virus y Sprites.
        enablegetnames=true;
        playing = false;
        stopPlayer();
        stopVirus();
        stopDNA();
        if(lymphocyteLife==BOSS_LIFE) stopBoss();
        stopAntibodies();
    }

    private void start() {
        if (PlayGame == null) {
          PlayGame = new Thread(this);
          PlayGame.start();
        }
        if (LoadGame == null) {
          LoadGame = new Thread(this);
          LoadGame.start();
        }
    }

    private void stop() {
        if (PlayGame != null) {
          PlayGame.stop();
          PlayGame = null;
        }
        if (LoadGame != null) {
          LoadGame.stop();
          LoadGame = null;
        }
    }

    public void run() {
        long startTime;
        // Disminuye la prioridad del Thread y obtiene el tiempo actual para cálculos posteriores

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        startTime = System.currentTimeMillis();
        if (Thread.currentThread() == LoadGame) {
          LoadGame.stop();
        }

        // CICLO PRINCIPAL DEL JUEGO
        while (Thread.currentThread() == PlayGame) {
            if (!paused) {
                // Mueve y modifica los Sprites
                updatePlayer();
                updateToxins();
                updateVirus();
                updateDNA();
                updateRBC();
                updateBoss();
                updateAntibodies();
                //Modificaciones de puntaje, nivel y estado del juego.
                if(level<2)
                    if(score>=levelpts[level]){
                        lifeLeft++;
                        level++;
                    }

                if(level==1&&!virus.active){
                    VirusPassesLeft = VIRUS_PASSES;
                    initVirus();
                }
                
                if(level==2&&lymphocyteLife>0){
                    if(!lymphocyte.active){
                        stopVirus();
                        stopDNA();
                        initBoss();
                    }
                    if(antibodyCounter==0) initAntibodies();
                }

                // Si todos los glóbulos fueron destruidos, actualiza el nivel
                if (RBCLeft <= 0 && level!=2)
                    if (--RBCCounter <= 0)
                        initRBC();
            }

            //Actualiza la pantalla y el tiempo para el siguiente ciclo
            repaint();
            try {
                startTime += DELAY;
                Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
            }catch (Exception e) { break; } //En caso de error, culmina el ciclo.
        }
    }

    private void initPlayer() {
        player.active = true;
        player.angle = 0.0;
        player.deltaAngle = 0.0;
        player.currentX = 0.0;
        player.currentY = 0.0;
        player.deltaX = 0.0;
        player.deltaY = 0.0;
        player.render();
    }

    private void updatePlayer() {
        double dx, dy, limit;

        if (!playing) return;

        //Rota al jugador si se tiene presionada la tecla izquierda o derecha
        if (left) {
            player.angle += Math.PI / 16.0;
            if (player.angle > 2 * Math.PI)
                player.angle -= 2 * Math.PI;
        }
        if (right) {
            player.angle -= Math.PI / 16.0;
            if (player.angle < 0)
                player.angle += 2 * Math.PI;
        }

        //Mueve al jugador si se tiene presionada la tecla de arriba o abajo.
        //Evitando que el jugador sobrepase el límite de velocidad dado.
        dx = -Math.sin(player.angle);
        dy =  Math.cos(player.angle);
        limit = 0.8 * MIN_RBC_SIZE;
        if (up) {
            if (player.deltaX + dx > -limit && player.deltaX + dx < limit)
                player.deltaX += dx;
            if (player.deltaY + dy > -limit && player.deltaY + dy < limit)
                player.deltaY += dy;
        }
        if (down) {
            if (player.deltaX - dx > -limit && player.deltaX - dx < limit)
                player.deltaX -= dx;
            if (player.deltaY - dy > -limit && player.deltaY - dy < limit)
                player.deltaY -= dy;
        }

        // FRENA MOVIMIENTO DE JUGADOR SI NINGUNA TECLA SE PRESIONA
        if(!down && !up){
            player.deltaX=0;
            player.deltaY=0;
        }
        
        //Si el usuario se teletransporta, hace que éste avance, junto al contador de posición.
        if (player.active) {
            player.advance();
            player.render();
        }
        //Si se perdió una vida, el contador avanza y el usuario reaparece en el centro, tal y como si se hubiese teletransportado.
        //Si era la última vida disponible, el juego termina.
        else
            if (lifeLeft > 0) {
                initPlayer();
            }
            else
              endGame();
      }

    private void stopPlayer() {
        score-=HIT_POINTS;  if(score<0) score=0;
        player.active = false;
        if (lifeLeft > 0)
            lifeLeft--;
    }

    private void initToxins() {
        for (int i = 0; i < MAX_SHOTS; i++) {
            toxins[i].active = false;
            toxinCounter[i] = 0;
        }
        toxinIndex = 0;
    }

    private void updateToxins() {
        for (int i = 0; i < MAX_SHOTS; i++)
            if (toxins[i].active) {
                toxins[i].advance();
                toxins[i].render();
                if (--toxinCounter[i] < 0)
                    toxins[i].active = false;
            }
    }

    private void initVirus() {
        //Inicializa un virus a la izquierda o derecha de la pantalla, aleatoriamente.
        virus.active = true;
        virus.currentX = -800 / 2;
        virus.currentY = Math.random() * 500;
        virus.deltaX = MIN_RBC_SPEED + Math.random() * (MAX_RBC_SPEED - MIN_RBC_SPEED);
        if (Math.random() < 0.5) {
            virus.deltaX = -virus.deltaX;
            virus.currentX = 800 / 2;
        }
        virus.deltaY = MIN_RBC_SPEED + Math.random() * (MAX_RBC_SPEED - MIN_RBC_SPEED);
        if (Math.random() < 0.5)
            virus.deltaY = -virus.deltaY;
        virus.render();

        //Inicializa el contador para el paso del Virus.
        VirusCounter = (int) Math.floor(800 / Math.abs(virus.deltaX));
    }

    private void updateVirus() {
        // Mueve al Virus y verifica sus colisiones con una toxina lanzada por el jugador
        // Se detiene cuando el contador culmine.
        if (virus.active) {
          virus.advance();
          virus.render();
          if (--VirusCounter <= 0)
            if (--VirusPassesLeft > 0)
                initVirus();
            else
                stopVirus();
          else {
            for (int i = 0; i < MAX_SHOTS; i++)
                if (toxins[i].active && virus.isColliding(toxins[i])) {
                    //ACTUALIZAR VIRUS AQUI
                    stopVirus();
                    score += VIRUS_POINTS;
                }

                // En ocaciones, lanza un ADN al jugador si el virus no está muy cercano a él
                int d = (int) Math.max(Math.abs(virus.currentX - player.currentX), Math.abs(virus.currentY - player.currentY));
                if (player.active && virus.active && !dna.active &&
                    d > 4 * MAX_RBC_SIZE && Math.random() < .03)
                initDNA();
           }
        }
    }

    private void stopVirus() {
        virus.active = false;
        VirusCounter = 0;
        VirusPassesLeft = 0;
    }

    private void initDNA() {
        dna.active = true;
        dna.angle = 0.0;
        dna.deltaAngle = 0.0;
        dna.currentX = virus.currentX;
        dna.currentY = virus.currentY;
        dna.deltaX = 0.0;
        dna.deltaY = 0.0;
        dna.render();
        dnaCounter = 3 * Math.max(800, 500) / MIN_RBC_SIZE;
    }

    private void updateDNA() {
        //Mueve el ADN de un virus y verifica que éste no colosione con una toxina lanzada por el jugador
        // Se detiene cuando el contador culmine.
        if (dna.active) {
          if (--dnaCounter <= 0)
            stopDNA();
          else {
            guideDNA();
            dna.advance();
            dna.render();
            for (int i = 0; i < MAX_SHOTS; i++)
              if (toxins[i].active && dna.isColliding(toxins[i])) {
                //ACTUALIZAR DNA AQUI
                stopDNA();
                score += DNA_POINTS;
              }
            if (dna.active && player.active && player.isColliding(dna)) {
              //ACTUALIZAR JUGADOR AQUI
              stopPlayer();
              stopVirus();
              stopDNA();
            }
          }
        }
    }

    private void guideDNA() {
        if (!player.active)  return;
        double dx, dy, angle;
        // Encuentra el ángulo necesario para atacar al jugador
        dx = player.currentX - dna.currentX;
        dy = player.currentY - dna.currentY;
        if (dx == 0 && dy == 0)
            angle = 0;
        if (dx == 0) {
            if (dy < 0)
                angle = -Math.PI / 2;
            else
                angle = Math.PI / 2;
        }
        else {
            angle = Math.atan(Math.abs(dy / dx));
            if (dy > 0)
                angle = -angle;
            if (dx < 0)
                angle = Math.PI - angle;
        }

        // Ajusta el ángulo con las coordenadas de la pantalla.
        dna.angle = angle - Math.PI / 2;

        // Cambia el ángulo del ADN para que apunte al jugador.
        dna.deltaX = MIN_RBC_SIZE / 3 * -Math.sin(dna.angle);
        dna.deltaY = MIN_RBC_SIZE / 3 *  Math.cos(dna.angle);
    }

    private void stopDNA() {
        dna.active = false;
        dnaCounter = 0;
    }

    private void initBoss() {
        lymphocyte.active = true;
        lymphocyte.currentX = -56.0;
        lymphocyte.currentY = -150.0;
        lymphocyte.deltaX = lymphocyte.deltaY = 1;
        lymphocyte.angle = 0.0;
        lymphocyte.deltaAngle = (Math.random() - 0.5) / 10;
        lymphocyte.render();
    }
    
    private void updateBoss() {
        if (lymphocyte.active) {
            lymphocyte.advance();
            lymphocyte.render();
            for (int i = 0; i < MAX_SHOTS; i++){
                if (toxins[i].active && lymphocyte.isColliding(toxins[i])) {
                    toxins[i].active = false;
                    lymphocyteShield--;
                    if(lymphocyteShield<0){
                        lymphocyteLife-=10;
                        lymphocyteShield=5;
                        score += BOSS_POINTS;
                    }
                }
                if (lymphocyte.active && player.active && player.isColliding(lymphocyte)) {
                    //ACTUALIZAR JUGADOR AQUI
                    stopPlayer();
                }
            }
            if (lymphocyteLife <= 0)
                stopBoss();
            // En ocaciones, lanza un Anticuerpo al jugador si no está muy cercano a él
            int d = (int) Math.max(Math.abs(lymphocyte.currentX - player.currentX), Math.abs(lymphocyte.currentY - player.currentY));
            if (player.active && virus.active && !dna.active && d > 4 * MAX_RBC_SIZE && Math.random() < .03)
                initAntibodies();
        }
    }
    
    private void stopBoss() {
        lymphocyte.active = false;
        if(lymphocyteLife<=0) endGame();
    }
    
    private void initAntibodies(){
        for(int i=0;i<MAX_ANTIBODIES;i++){
            antibody[i].active = true;
            antibody[i].angle = 0.0;
            antibody[i].deltaAngle = 0.0;
            switch(i){
                case 0: antibody[i].currentX=lymphocyte.currentX; antibody[i].currentY=lymphocyte.currentY-20; break;
                case 1: antibody[i].currentX=lymphocyte.currentX+140; antibody[i].currentY=lymphocyte.currentY-20; break;
                case 2: antibody[i].currentX=lymphocyte.currentX+160; antibody[i].currentY=lymphocyte.currentY; break;
                case 3: antibody[i].currentX=lymphocyte.currentX+160; antibody[i].currentY=lymphocyte.currentY+140; break;
                case 4: antibody[i].currentX=lymphocyte.currentX+140; antibody[i].currentY=lymphocyte.currentY+160; break;
                case 5: antibody[i].currentX=lymphocyte.currentX; antibody[i].currentY=lymphocyte.currentY+160; break;
                case 6: antibody[i].currentX=lymphocyte.currentX-20; antibody[i].currentY=lymphocyte.currentY+140; break;
                case 7: antibody[i].currentX=lymphocyte.currentX-20; antibody[i].currentY=lymphocyte.currentY; break;
            }
            antibody[i].deltaX = (i>=3)? i+1:-i-1;
            antibody[i].deltaY = i+1;
            antibody[i].render();
            antibodyCounter = 8;
        }
    }
    
    private void updateAntibodies() {
        for(int i=0;i<MAX_ANTIBODIES;i++){
            if (antibody[i].active) {
                guideAntibodies(i);
                antibody[i].advance();
                antibody[i].render();
                for (int j = 0; j < MAX_SHOTS; j++)
                  if (toxins[j].active && antibody[i].isColliding(toxins[j])) {
                    //ACTUALIZAR DNA AQUI
                    stopAntibodies(i);
                    score += ANTIBODY_POINTS;
                  }
                if (antibody[i].active && player.active && player.isColliding(antibody[i])) {
                  //ACTUALIZAR JUGADOR AQUI
                    stopPlayer();
                    stopAntibodies(i);
                }
                
                if(antibodyCounter==0 && lymphocyteLife!=0)
                    initAntibodies();
              }
        }
    }

    private void guideAntibodies(int i) {
        if (!player.active)  return;
        double dx, dy, angle;
        // Encuentra el ángulo necesario para atacar al jugador
        dx = player.currentX - antibody[i].currentX + ((i>=3)? i+1:-i-1);
        dy = player.currentY - antibody[i].currentY + i;
        if (dx == 0 && dy == 0)
            angle = i;
        if (dx == 0) {
            if (dy < 0)
                angle = -Math.PI / 2;
            else
                angle = Math.PI / 2;
        }
        else {
            angle = Math.atan(Math.abs(dy / dx));
            if (dy > 0)
                angle = -angle;
            if (dx < 0)
                angle = Math.PI - angle;
        }

        // Ajusta el ángulo con las coordenadas de la pantalla.
        antibody[i].angle = angle - Math.PI / 2;

        // Cambia el ángulo del ADN para que apunte al jugador.
        antibody[i].deltaX = MIN_RBC_SIZE / 3 * -Math.sin(antibody[i].angle) + ((i>=3)? i+1:-i-1);
        antibody[i].deltaY = MIN_RBC_SIZE / 3 *  Math.cos(antibody[i].angle) + i;
    }

    private void stopAntibodies(int i) {
        antibody[i].active = false;
        antibodyCounter--;
        if(antibodyCounter==0) initAntibodies();
    }
    
    private void stopAntibodies() {
        for(int i=0;i<MAX_ANTIBODIES;i++){
            stopAntibodies(i);
        }
        antibodyCounter=0;
    }
    
    private void initRBC() {
        //Crea formas, posiciones y movimientos aleatorios para cada glóbulo.
        int i;
        
        for (i = 0; i < MAX_RBC; i++) {
            // Crea una forma irregular para el glóbulo y le da una rotación aleatoria.
            RBC[i].shape = new Polygon();
            RBC[i].shape.addPoint(10, 4);RBC[i].shape.addPoint(7, 8);RBC[i].shape.addPoint(5, 12);
            RBC[i].shape.addPoint(2, 17);RBC[i].shape.addPoint(2, 20);RBC[i].shape.addPoint(1, 26);
            RBC[i].shape.addPoint(1, 30);RBC[i].shape.addPoint(2, 34);RBC[i].shape.addPoint(2, 40);
            RBC[i].shape.addPoint(5, 48);RBC[i].shape.addPoint(9, 52);RBC[i].shape.addPoint(13, 60);
            RBC[i].shape.addPoint(17, 63);RBC[i].shape.addPoint(24, 68);RBC[i].shape.addPoint(29, 70);
            RBC[i].shape.addPoint(35, 73);RBC[i].shape.addPoint(40, 73);RBC[i].shape.addPoint(46, 73);
            RBC[i].shape.addPoint(51, 72);RBC[i].shape.addPoint(57,71);RBC[i].shape.addPoint(62,68);
            RBC[i].shape.addPoint(66,63);RBC[i].shape.addPoint(67,56);RBC[i].shape.addPoint(68,49);
            RBC[i].shape.addPoint(68,42);RBC[i].shape.addPoint(67,37);RBC[i].shape.addPoint(65,31);
            RBC[i].shape.addPoint(63,25);RBC[i].shape.addPoint(59,16);RBC[i].shape.addPoint(55,11);
            RBC[i].shape.addPoint(49,7);RBC[i].shape.addPoint(44,4);RBC[i].shape.addPoint(39,1);
            RBC[i].shape.addPoint(32,0);RBC[i].shape.addPoint(25,0);RBC[i].shape.addPoint(18,1);

            RBC[i].active = true;
            RBC[i].angle = 0.0;
            RBC[i].deltaAngle = (Math.random() - 0.5) / 10;
            //RBC[i].deltaAngle = 0;

            // Coloca al glóbulo en una esquina de la pantalla
            if (Math.random() < 0.5) {
                RBC[i].currentX = -800 / 2;
                if (Math.random() < 0.5)
                    RBC[i].currentX = 800 / 2;
                RBC[i].currentY = Math.random() * 500;
            }
            else {
                RBC[i].currentX = Math.random() * 800;
                RBC[i].currentY = -500 / 2;
                if (Math.random() < 0.5)
                    RBC[i].currentY = 500 / 2;
            }

            // Coloca una forma de movimiento aleatorio para el glóbulo
            RBC[i].deltaX = Math.random() * RBCSpeed;
            if (Math.random() < 0.5)
                RBC[i].deltaX = -RBC[i].deltaX;
            RBC[i].deltaY = Math.random() * RBCSpeed;
            if (Math.random() < 0.5)
                RBC[i].deltaY = -RBC[i].deltaY;

            RBC[i].render();
            attackedRBC[i] = false;
        }

        RBCLeft = MAX_RBC;
        if (RBCSpeed < MAX_RBC_SPEED)
            RBCSpeed++;
    }

    private void initRBC2(int n) {
        double tempX,tempY;

        // Actualiza la imagen del glóbulo original
        // Al impactar le envía un booleano al Runnable que dibuja para que cambie su imagen
        // Colocándolo en la misma posición pero con características aleatorias nuevas.
        tempX = RBC[n].currentX;
        tempY = RBC[n].currentY;
        int count=0,i=0;
        do {
            if (!RBC[i].active) {
                RBC[i].shape = new Polygon();
                RBC[i].shape.addPoint(13,0);RBC[i].shape.addPoint(9,2);RBC[i].shape.addPoint(6,5);RBC[i].shape.addPoint(5,9);
                RBC[i].shape.addPoint(4,11);RBC[i].shape.addPoint(3,15);RBC[i].shape.addPoint(3,19);RBC[i].shape.addPoint(1,25);
                RBC[i].shape.addPoint(1,30);RBC[i].shape.addPoint(1,35);RBC[i].shape.addPoint(1,38);RBC[i].shape.addPoint(3,42);
                RBC[i].shape.addPoint(4,47);RBC[i].shape.addPoint(6,51);RBC[i].shape.addPoint(8,55);RBC[i].shape.addPoint(11,61);
                RBC[i].shape.addPoint(16,66);RBC[i].shape.addPoint(28,70);RBC[i].shape.addPoint(36,70);RBC[i].shape.addPoint(42,69);
                RBC[i].shape.addPoint(44,65);RBC[i].shape.addPoint(41,63);RBC[i].shape.addPoint(38,60);RBC[i].shape.addPoint(34,58);
                RBC[i].shape.addPoint(30,55);RBC[i].shape.addPoint(26,53);RBC[i].shape.addPoint(22,48);RBC[i].shape.addPoint(20,44);
                RBC[i].shape.addPoint(19,38);RBC[i].shape.addPoint(17,32);RBC[i].shape.addPoint(17,27);RBC[i].shape.addPoint(18,22);
                RBC[i].shape.addPoint(18,16);RBC[i].shape.addPoint(17,10);RBC[i].shape.addPoint(16,3);RBC[i].shape.addPoint(14,1);

                RBC[i].active = true;
                RBC[i].angle = 0.0;
                RBC[i].deltaAngle = (Math.random() - 0.5) / 10;
                //RBC[i].deltaAngle = 0;
                RBC[i].currentX = tempX;
                RBC[i].currentY = tempY;
                RBC[i].deltaX = Math.random() * 2 * RBCSpeed - RBCSpeed;
                RBC[i].deltaY = Math.random() * 2 * RBCSpeed - RBCSpeed;
                RBC[i].render();
                attackedRBC[i] = true;
                count++;
                RBCLeft++;
            }
            i++;
           } while (i < MAX_RBC && count < 1);
    }

    private void updateRBC() {
        // Mueve glóbulos inactivos y confirma colisiones.

        for (int i = 0; i < MAX_RBC; i++)
            if (RBC[i].active) {
                RBC[i].advance();
                RBC[i].render();

                // Si un Glóbulo Rojo es golpeado por una toxina, se suma la puntuación y actualiza el juego.
                // Si es el primer golpe, la imagen se actualiza.
                for (int j = 0; j < MAX_SHOTS; j++)
                  if (toxins[j].active && RBC[i].active && RBC[i].isColliding(toxins[j])) {
                    RBCLeft--;
                    RBC[i].active = false;
                    toxins[j].active = false;
                    if (!attackedRBC[i]) {
                      score += RBC_1;
                      initRBC2(i);
                    }
                    else
                      score += RBC_2;
                  }

                // Si el jugador no se está teletransportando, verifica si fue golpeado.
                if (player.active && RBC[i].active && RBC[i].isColliding(player)) {
                  //ACTUALIZAR JUGADOR AQUI
                  stopPlayer();
                  stopVirus();
                  stopDNA();
                }
        }
    }

    private void keyDown(int key) {
        // Verifica si alguna tecla de dirección ha sido presionada.
        if (key == 37)      left = true;
        if (key == 39)     right = true;
        if (key == 38)        up = true;
        if (key == 40)      down = true;

        // Barra Espaciadora: Disparar Toxina y empezar su contador de vida.
        if (key == 32 && player.active) {
            toxinIndex++;
            if (toxinIndex >= MAX_SHOTS)
                toxinIndex = 0;
            toxins[toxinIndex].active = true;
            toxins[toxinIndex].currentX = player.currentX;
            toxins[toxinIndex].currentY = player.currentY;
            toxins[toxinIndex].deltaX = MIN_RBC_SIZE * -Math.sin(player.angle);
            toxins[toxinIndex].deltaY = MIN_RBC_SIZE *  Math.cos(player.angle);
            toxinCounter[toxinIndex] = Math.min(800, 500) / MIN_RBC_SIZE;
            toxins[toxinIndex].angle=player.angle;
        }

        // Tecla 'T': Teletransporta al jugador a una posición aleatoria y comienza el contador de la animación.
        if (key == 'T' && player.active && !paused) {
            player.currentX = Math.random() * 800;
            player.currentX = Math.random() * 500;
        }

        // Tecla 'P': Activa/Desactiva el Menú y pausa el juego.
        if (key == 'P' && !first && playing) {
            paused = !paused;
            showmenu=true;
            help[0]=restart=table=exit=false;
        }

        // Tecla 'Enter' o 'Barra Espaciadora': Inicia el Juego, si no está ya en progreso.
        if (key == Event.ENTER && !playing){
            first=false;
            if(!name.trim().isEmpty())  initGame();
        }

        //Menú de Pausa
        if(paused){
            switch(key){
                case java.awt.event.KeyEvent.VK_1:  showmenu=false; help[0]=true;   break;  //Instrucciones
                case java.awt.event.KeyEvent.VK_2:  showmenu=false; restart=true;   break;  //Reiniciar Sesión
                case java.awt.event.KeyEvent.VK_3:  showmenu=false; restart=true; tempname=""; break;  //Reiniciar Juego
                case java.awt.event.KeyEvent.VK_4:  showmenu=false; table=true;     break;  //Tabla de Puntaje
                case java.awt.event.KeyEvent.VK_5:  showmenu=false; exit=true;      break;  //Salir del Juego
                case 'D':                           debugmode=!debugmode;           break;// Modo de Depuración (Testeo y Evaluación)
            }
        }

        //Instrucciones
        if(left){
            if(help[2]){ help[0]=false; help[1]=true; help[2]=false;}
            else if(help[1]){ help[0]=true; help[1]=false; help[2]=false;}
        }
        if(right){
            if(help[0]){ help[0]=false; help[1]=true; help[2]=false;}
            else if(help[1]){ help[0]=false; help[1]=false; help[2]=true;}
        }
        
        //Confirmaciones del Menú
        if((help[0]||help[1]||help[2])&&key == Event.ENTER){            //Enter para salir de las Instrucciones
            help[0]=help[1]=help[2]=false;
            showmenu=true;
        }
        
        if(table&&key == Event.ENTER){                                  //Enter para salir de la Tabla de Puntajes
            table=false;
            showmenu=true;
            if(!playing) paused=false;                                  //Tabla mostrada automáticamente tras fin del juego
        }
        if(restart){                                                    //Reiniciar Juego
            if(key == 'S'){
                restart=false;
                initGame();
                if(tempname.trim().isEmpty()){                          //Reiniciar Juego completo, sino sólo sesión.
                    name="";
                    first=true;
                    endGame();
                }
            }
            else if(key == 'N'){
                restart=false;
                showmenu=true;
            }
        }
        if(exit){                                                       //Salir del Juego
            if(key == 'S')      System.exit(0);
            else if(key == 'N'){
                exit=false;
                showmenu=true;
            }
        }
      }

    private void keyUp(int key) {
        // Verifica si alguna tecla de dirección dejó de ser presionada.
        if (key == 37)      left = false;
        if (key == 39)      right = false;
        if (key == 38)      up = false;
        if (key == 40)      down = false;
    }

    public void paint(Graphics g) {
        update(g);
    }

    public void update(Graphics g) {
        try{
            Dimension d = this.getSize();

            //Crea los gráficos Offscreen si no existe uno correcto.
            if (offGraphics == null || d.width != offDimension.width || d.height != offDimension.height) {
                offDimension = d;
                offImage = createImage(d.width, d.height);
                offGraphics = (Graphics2D) offImage.getGraphics();
            }

            // Fondo del Juego
            //offGraphics.setColor(Color.black);
            switch(level){
                case 1: offGraphics.drawImage((Image)Fondo2,0,0,null); break;
                case 2: offGraphics.drawImage((Image)Fondo3,0,0,null); break;
                default: offGraphics.drawImage((Image)Fondo1,0,0,null);
            }
            //offGraphics.fillRect(0, 0, d.width, d.height);

            // Dibuja las toxinas.
            offGraphics.setColor(Color.white);
            for (int i = 0; i < MAX_SHOTS; i++)
                if (toxins[i].active){
                    AffineTransform reset2 = new AffineTransform();
                    reset2.rotate(0,0,0); 
                    offGraphics.rotate(-toxins[i].angle,toxins[i].sprite.xpoints[toxins[i].sprite.npoints-1],toxins[i].sprite.ypoints[toxins[i].sprite.npoints-1]);
                    offGraphics.drawImage((Image)playerBullet, toxins[i].sprite.xpoints[toxins[i].sprite.npoints-1]-8,toxins[i].sprite.ypoints[toxins[i].sprite.npoints-1],null);
                    offGraphics.setTransform(reset2);
                    if(debugmode){  offGraphics.setColor(Color.BLUE);
                                    offGraphics.drawPolygon(toxins[i].sprite);
                    }
            }

            // Dibuja el DNA de los Virus.
            // El contador permite la animación de desaparición cuando acaba su tiempo de vida.
            int c = Math.min(dnaCounter * 24, 255);
            offGraphics.setColor(new Color(c, c, c));
            if (dna.active) {
                offGraphics.drawPolygon(dna.sprite);
                offGraphics.drawLine(dna.sprite.xpoints[dna.sprite.npoints - 1], dna.sprite.ypoints[dna.sprite.npoints - 1],
                                   dna.sprite.xpoints[0], dna.sprite.ypoints[0]);
            }

            // Transparente
            offGraphics.setColor(new Color(1,1,1,0));
            AffineTransform at;
            // Dibuja los glóbulos rojos
            for (int i = 0; i < MAX_RBC; i++){
                if (RBC[i].active){
                    if(attackedRBC[i]){
                        at = new AffineTransform();
                        at.rotate(0,0,0);
                        offGraphics.rotate(-RBC[i].angle,RBC[i].sprite.xpoints[RBC[i].sprite.npoints-1],
                                RBC[i].sprite.ypoints[RBC[i].sprite.npoints-1]);
                        offGraphics.drawImage((Image)GlobuloRojo2,RBC[i].sprite.xpoints[RBC[i].sprite.npoints - 1]-15,
                                RBC[i].sprite.ypoints[RBC[i].sprite.npoints - 1], null);
                        offGraphics.setTransform(at);
                    }
                    else{
                        at = new AffineTransform();
                        at.rotate(0,0,0);
                        offGraphics.rotate(-RBC[i].angle,RBC[i].sprite.xpoints[RBC[i].sprite.npoints-1],
                                RBC[i].sprite.ypoints[RBC[i].sprite.npoints-1]);
                        offGraphics.drawImage((Image)GlobuloRojo1,RBC[i].sprite.xpoints[RBC[i].sprite.npoints - 1]-15,
                                RBC[i].sprite.ypoints[RBC[i].sprite.npoints - 1], null);
                        offGraphics.setTransform(at);
                    }
                    if(debugmode) offGraphics.setColor(Color.BLUE);
                    offGraphics.drawPolygon(RBC[i].sprite);
                    offGraphics.drawLine(RBC[i].sprite.xpoints[RBC[i].sprite.npoints - 1], RBC[i].sprite.ypoints[RBC[i].sprite.npoints - 1],
                                         RBC[i].sprite.xpoints[0], RBC[i].sprite.ypoints[0]);
                }
            }
            // Dibuja los virus
            if (virus.active) {
                VirusAnimation++;
                int auxv,zeros=10;
                if(VirusAnimation==8*zeros) VirusAnimation=0;
                if(VirusAnimation<zeros)    auxv=0;
                else if(VirusAnimation<2*zeros)    auxv=1;
                else if(VirusAnimation<3*zeros)    auxv=2;
                else if(VirusAnimation<4*zeros)    auxv=3;
                else if(VirusAnimation<5*zeros)    auxv=4;
                else if(VirusAnimation<6*zeros)    auxv=5;
                else if(VirusAnimation<7*zeros)    auxv=6;
                else auxv=7;
                offGraphics.drawImage(Virus[auxv],virus.sprite.xpoints[virus.sprite.npoints - 1]-22,
                                            virus.sprite.ypoints[virus.sprite.npoints - 1]-1,null);
                if(debugmode) offGraphics.setColor(Color.BLUE);
                offGraphics.drawPolygon(virus.sprite);
                offGraphics.drawLine(virus.sprite.xpoints[virus.sprite.npoints - 1], virus.sprite.ypoints[virus.sprite.npoints - 1],
                                     virus.sprite.xpoints[0], virus.sprite.ypoints[0]);
            }

            //Dibuja el Linfocito
            if(lymphocyte.active){
                at = new AffineTransform();
                at.rotate(0,0,0);
                offGraphics.rotate(-lymphocyte.angle,lymphocyte.sprite.getBounds().getCenterX(),
                                lymphocyte.sprite.getBounds().getCenterY());
                offGraphics.drawImage((Image)Lymphocyte[5-lymphocyteShield],lymphocyte.sprite.getBounds().x-8,
                        lymphocyte.sprite.getBounds().y-10, null);
                offGraphics.setTransform(at);
                if(debugmode) offGraphics.setColor(Color.BLUE);
                offGraphics.drawPolygon(lymphocyte.sprite);
                offGraphics.drawLine(lymphocyte.sprite.xpoints[lymphocyte.sprite.npoints - 1], lymphocyte.sprite.ypoints[lymphocyte.sprite.npoints - 1],
                                     lymphocyte.sprite.xpoints[0], lymphocyte.sprite.ypoints[0]);

                //Dibuja los Anticuerpos
                offGraphics.setColor(Color.WHITE);
                for(int i=0;i<MAX_ANTIBODIES;i++){
                    if (antibody[i].active) {
                        offGraphics.fillPolygon(antibody[i].sprite);
                        offGraphics.drawLine(antibody[i].sprite.xpoints[antibody[i].sprite.npoints - 1], antibody[i].sprite.ypoints[antibody[i].sprite.npoints - 1],
                                           antibody[i].sprite.xpoints[0], antibody[i].sprite.ypoints[0]);
                    }
                }
            }

            // Dibuja al Jugador
            offGraphics.setColor(new Color(1,1,1,0));
            if (player.active) {
                AffineTransform reset = new AffineTransform();
                reset.rotate(0,0,0);
                offGraphics.rotate(-player.angle, player.sprite.xpoints[player.sprite.npoints - 1],player.sprite.ypoints[player.sprite.npoints - 1]);
                offGraphics.drawImage((Image)playerImage,player.sprite.xpoints[player.sprite.npoints - 1], player.sprite.ypoints[player.sprite.npoints - 1],null);
                offGraphics.setTransform(reset);
                if(debugmode) offGraphics.setColor(Color.YELLOW);
                offGraphics.drawPolygon(player.sprite);
            }

            // Muestra Estatus y Mensajes
            offGraphics.setFont(font);
            String s;
            if(playing&&!first&!name.trim().isEmpty()){
                s=name.toUpperCase();
                offGraphics.drawImage(Barra,5,25,null);
                offGraphics.setColor(Color.red);
                offGraphics.drawString(s, 40, 60);
                offGraphics.setColor(Color.white);
                s="|  NIVEL " + (level+1)+ " ";

                offGraphics.drawImage(BaseVida, 254, 50, null);
                int auxl;
                if(level<2){
                    auxl=(score*100)/levelpts[level];
                    if(auxl<10) auxl=0;
                    else offGraphics.drawImage(Puntos[auxl/10], 265,51, null);

                }
                else offGraphics.drawImage(PuntosInf, 265,51, null);
                offGraphics.drawString(s, 200, 60);

                s="  |  PUNTAJE ";
                if(score>999999) s="+100K";
                else{
                    for(int j=String.valueOf(score).length();j<6;j++) s+="0"; //Completa 6 espacios.
                    s+=score;
                }
                offGraphics.drawString(s, 375, 60);

                s="|  VIDAS ";
                offGraphics.drawString(s, 494, 60);
                s="";
                if(lifeLeft>999999) s+="+1M";
                else{
                    for(int j=String.valueOf(lifeLeft).length();j<6;j++) s+="0"; //Completa 6 espacios.
                    s+= lifeLeft;
                }
                s+=" ♥";
                offGraphics.setColor(Color.GREEN);
                offGraphics.drawString(s, 540, 60);

                offGraphics.drawImage(VirusVida,570+fm.stringWidth(s)-fontWidth,50, null);

                offGraphics.setColor(Color.WHITE);
                s="      |   PAUSA (P)";
                offGraphics.drawString(s,d.width - fm.stringWidth(s)-34, 60);


                if(level==2&&lymphocyte.active){
                    s="SISTEMA INMUNOLÓGICO | ";
                    offGraphics.drawString(s, 40, d.height - fontHeight - 20);

                    offGraphics.setFont(new Font("Helvetica", 0, 10));
                    fm = g.getFontMetrics();
                    s="";
                    for(int j=0;j<lymphocyteLife;j++)   s+= "▪";
                    s+=" ";
                    int life=(lymphocyteLife*100)/BOSS_LIFE;
                    if(life<10) s+="0";
                    s+=life+" %";    
                    offGraphics.setColor(new Color(0f,0f,0f,0.75f));
                    offGraphics.fillRect(200, d.height - fontHeight*2 - 16,fm.stringWidth(s)-lymphocyteLife, fm.getHeight());
                    offGraphics.setColor(Color.WHITE);
                    offGraphics.drawString(s, 200, d.height - fontHeight - 20);
                    offGraphics.setFont(font); fm = g.getFontMetrics();
                }
            }

            if (!playing) {
                if(!table)offGraphics.drawImage((Image)pause1,0,0,null);
                offGraphics.setFont(new Font("Arial Unicode MS", Font.PLAIN, 80));
                int n=new Random().nextInt(256);
                offGraphics.setColor(new Color(n,n,n));
                s = "☣";
                offGraphics.drawString(s, (d.width-fm.stringWidth(s))/2-20, table?d.height / 4-20:d.height / 4+40);
                offGraphics.setColor(Color.white);
                offGraphics.setFont(new Font("Helvetica", Font.BOLD, 24));
                s = "I N F E C T I O N";
                offGraphics.drawString(s, (d.width) / 2-fm.stringWidth(s), table?d.height / 4+10:d.height / 4+70);
                offGraphics.setFont(font);  fm = g.getFontMetrics(); //Valores por defecto
                if(first){  //Primera pantalla del juego
                    s = "Computación Gráfica — N813";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 30);
                    s = "García, Miguelangel   —   20.548.639";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 80);
                    s = "Gómez, Angel            —   24.728.313";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 100);
                    s = "Sánchez, Diego         —   24.957.109";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 120);
                    s = "¡Bienvenido/a!";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + 100);
                }
                else{
                    if(name.trim().isEmpty()){//Primera vez en jugar
                        s="NOMBRE";
                        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight-20);
                        s="";
                        for(int j=0;j<10-tempname.length()/2;j++) s+="_";
                        s+=tempname;
                        for(int j=tempname.length()/2;j<10;j++) s+="_";
                        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight -10);
                    }
                    else{   //Ya se había jugado, pantalla de Fin del juego
                        s=(lymphocyteLife>0? "Fin del Juego":"¡Felicidades! ¡Has ganado!");
                        offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + 50);
                        table=true;
                    }
                }
                s = "'Enter' para "+(first||name.trim().isEmpty()? "":"re")+"iniciar el juego";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, first? d.height/4+120:table? d.height / 4+30:d.height/4 + fontHeight+70);
            }
            else if (paused) {
                if(showmenu){
                    offGraphics.drawImage((Image)pause1,0,0,null);
                    s = "Juego Pausado";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
                    s = "_____________";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + 5);
                    s = "1) Instrucciones";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 20);
                    s = "2) Reiniciar Partida";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 40);
                    s = "3) Reiniciar Juego";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 60);
                    s = "4) Tabla de Puntaje";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 80);
                    s = "5) Salir del Juego";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 100);
                    s = "P) Regresar al Juego";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 140);
                }
            }

            //Confirmación para salir o reiniciar
            if(restart||exit){
                offGraphics.drawImage((Image)pause1,0,0,null);
                s = "¿Está seguro de que desea "+
                    (restart? "reiniciar ":"salir d")+
                    (tempname.trim().isEmpty()? "el juego":"la partida")+"?";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight);
                s = "S / N";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 2 + fontHeight + 20);
            }

            //Instrucciones del Juego
            if(help[0]){
                offGraphics.drawImage((Image)pause2,0,0,null);
                s = "Instrucciones: Información Básica";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
                s = "_________________________________";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + 5);
                s="En Infection estarás en el papel de una bacteria que intenta invadir a su huésped,";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 120);
                s="te las has arreglado para entrar al torrente sanguíneo y destruir sus glóbulos rojos.";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 140);
                s="Pero, no todo es tan sencillo cuando te encuentras con enemigos que también desean";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 160);
                s="transmitir sus propias enfermedades, ¿serás capaz de vencerlos y empezar tu infección";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 180);
                s="antes de que te detenga el sistema inmunológico?";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 200);
                s="MÁS INFORMACIÓN   →";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 260);
                s="'Enter' para regresar al Menú de Pausa";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 5);
            }
            if(help[1]){
                offGraphics.drawImage((Image)pause2,0,0,null);
                s = "Instrucciones: Controles";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
                s = "________________________";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + 5);
                offGraphics.setFont(new Font("Arial Unicode MS", Font.BOLD, 30));
                s="↕"; offGraphics.drawString(s, 175, d.height/4 + fontHeight + 125);
                s="↔"; offGraphics.drawString(s, 174, d.height/4 + fontHeight + 155);
                s="⌨"; offGraphics.drawString(s, 170, d.height/4 + fontHeight + 185);
                s="ℙ"; offGraphics.drawString(s, 176, d.height/4 + fontHeight + 215);
                offGraphics.setFont(font);
                s="‣ Utiliza las teclas de dirección de Arriba y Abajo para moverte.";
                offGraphics.drawString(s, 200, d.height/4 + fontHeight + 120);
                s="‣ Utiliza las teclas de Derecha e Izquierda para girar.";
                offGraphics.drawString(s, 200, d.height/4 + fontHeight + 150);
                s="‣ Dispara toxinas con la Barra Espaciadora para atacar.";
                offGraphics.drawString(s, 200, d.height/4 + fontHeight + 180);
                s="‣ Pausa el juego en cualquier momento con la tecla 'P'.";
                offGraphics.drawString(s, 200, d.height/4 + fontHeight + 210);
                s="←   MÁS INFORMACIÓN   →";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 260);
                s="'Enter' para regresar al Menú de Pausa";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 5);
            }
            if(help[2]){
                offGraphics.drawImage((Image)pause2,0,0,null);
                s = "Instrucciones: Puntaje";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4);
                s = "______________________";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height / 4 + 5);
                offGraphics.setFont(new Font("Helvetica", Font.BOLD, 24));
                offGraphics.setColor(Color.YELLOW);
                s="+"+RBC_1+" / "+"+"+RBC_2;
                offGraphics.drawString(s, 400, d.height/4 + fontHeight + 80);
                s="+"+VIRUS_POINTS;
                offGraphics.drawString(s, 400, d.height/4 + fontHeight + 110);
                s="+"+DNA_POINTS;
                offGraphics.drawString(s, 400, d.height/4 + fontHeight + 140);
                s="+"+ANTIBODY_POINTS;
                offGraphics.drawString(s, 400, d.height/4 + fontHeight + 170);
                s="+"+BOSS_POINTS;
                offGraphics.drawString(s, 400, d.height/4 + fontHeight + 200);
                offGraphics.setColor(Color.RED);
                s="-"+HIT_POINTS;
                offGraphics.drawString(s, 400, d.height/4 + fontHeight + 230);
                offGraphics.setColor(Color.WHITE);
                offGraphics.setFont(font);
                s="¡Atención! ¡Deberás aumentar tu puntaje para poder pasar de nivel!";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 50);
                s="Glóbulos Rojos:";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 80);
                s="Virus:";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 110);
                s="ADN (Lanzado por Virus):";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 140);
                s="Anticuerpo:";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 170);
                s="Linfocito (Ganar el Juego):";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 200);
                s="Ataques y Pérdida de Vidas:";
                offGraphics.drawString(s, 220, d.height/4 + fontHeight + 230);
                s="←   MÁS INFORMACIÓN";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 260);
                s="'Enter' para regresar al Menú de Pausa";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 5);
            }

            //Tabla de Mejores Puntajes
            if(table){
                offGraphics.drawImage((Image)pause2,0,0,null);
                s = "Mejores Puntuaciones";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, playing?d.height / 4+40:d.height / 4 + 90);
                s = "____________________";
                offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, playing?d.height / 4+45:d.height / 4 + 95);

                if(enablegetnames) getnames();
                for(int j=0;j<players.size()&&j<10;j++){
                    int y=d.height/4 + fontHeight + 120 + j*20;
                    s=(j+1)+". "+players.get(j)[0].toUpperCase();                   //EJ. X. NOMBRE
                    offGraphics.drawString(s, 180, y);   s="";
                    s+=".............................................................................."; //78 ESPACIOS
                    offGraphics.drawString(s, 340, y);   s="";
                    if(players.get(j)[1].length()<6)
                        for(int k=players.get(j)[1].length();k<6;k++)   //PUNTAJE EN FORMATO 000123
                            s+="0";
                    s+=(players.get(j)[1].length()<10? players.get(j)[1]:"+1000M")+" PTS";
                    offGraphics.drawString(s, 580, y);
                    if(j==0){                                                       //RECUADRO AMARILLO RESALTANDO LA MAYOR PUNTUACIÓN
                        offGraphics.setColor(new Color(200,200,0));
                        int largo;
                        switch(players.get(j)[1].length()){                         //EXACTITUD DE PÍXELES
                            case 7: largo=485; break;
                            case 8: largo=490; break;
                            case 9: largo=495; break;
                            default: largo=480;
                        }
                        offGraphics.drawRect(175,d.height/4 + 123, largo, fontHeight);
                        offGraphics.setColor(Color.white);
                    }
                }
                if(playing){
                    s = "'Enter' para continuar";
                    offGraphics.drawString(s, (d.width - fm.stringWidth(s)) / 2, d.height/4 + fontHeight + 45);
                }
            }
            // Dibuja las imágenes Offscreen en la pantalla real del juego.
            g.drawImage(offImage, 0, 0, this);
        }catch(Exception e){}
    }

    private void scorefile(){
        String ruta=System.getenv("APPDATA")+"/Infection/";
        File dir=new File(ruta);        if(!dir.exists()) dir.mkdirs();
        dir=new File(ruta+"data.txt");
        try{    if(!dir.exists())
                    dir.createNewFile();
        }catch(Exception e){}
    }

    private void getnames(){
        //Bloquea más accesos a este método
        enablegetnames=false;
        players = new ArrayList<>();

        scorefile();
        File dir=new File(System.getenv("APPDATA")+"/Infection/data.txt");
        //Lectura de nombres y puntajes ya existentes
        try{BufferedReader reader=new BufferedReader(new FileReader(dir));
            String line=reader.readLine();
            while(line!=null){   //Añade: Nombre, puntaje
                players.add(new String[]{line.substring(0,line.indexOf("-")),line.substring(line.indexOf("-")+1)});
                line=reader.readLine();
            }
            reader.close();
        } catch (Exception x) {}

        //Se añade al jugador actual si perdió el juego
        if(!playing && !paused && !name.trim().isEmpty() && lifeLeft==0)
            players.add(new String[]{name,String.valueOf(score)});

        //Ordenación por Burbuja
        for(int i=0;i<players.size()-1;i++)
            for(int j=0;j<players.size()-1;j++)
            {	if(Integer.parseInt(players.get(j)[1])<Integer.parseInt(players.get(j+1)[1])){
                    int num=Integer.parseInt(players.get(j)[1]);
                    players.get(j)[1]=players.get(j+1)[1];
                    players.get(j+1)[1]=String.valueOf(num);
                    String n=players.get(j)[0];
                    players.get(j)[0]=players.get(j+1)[0];
                    players.get(j+1)[0]=n;
                }
            }

        //Una vez terminado, guarda los datos organizados en el archivo de Data
        try{PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir, false)));
            for(int i=0;i<players.size();i++)
                out.println(players.get(i)[0]+"-"+players.get(i)[1]);
            out.flush();    out.close();
        }catch(Exception e){}
    }
}