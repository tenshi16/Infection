package Infection;

import java.awt.Polygon;

public class Sprite {
    // Medidas del Juego
    int W=800;
    int H=500;
    
    Polygon shape;                 // Forma inicial, centrada en el origen.
    boolean active;                // Responde a: ¿Está activo?.
    double  angle;                 // Ángulo de Rotación.
    double  deltaAngle;            // Delta para cambiar el ángulo de rotación.
    double  currentX, currentY;    // Posición actual en pantalla.
    double  deltaX, deltaY;        // Delta para cambiar la posición en pantalla.
    Polygon sprite;                // Ubicación y forma final del sprite tras aplicar rotación y movimiento.
                                   // Usada para dibujar en pantalla y detectar colisiones.
    
    public Sprite() {
        this.shape = new Polygon();
        this.active = false;
        this.angle = 0.0;
        this.deltaAngle = 0.0;
        this.currentX = 0.0;
        this.currentY = 0.0;
        this.deltaX = 0.0;
        this.deltaY = 0.0;
        this.sprite = new Polygon();
    }

    public void advance() {
        // Actualiza la rotación y posición del Sprite basado en los Delta dados
        // Si el Sprite sobrepasa los límites de la pantalla, se transporta al otro lado de la misma.
        this.angle += this.deltaAngle;
        if (this.angle < 0)
            this.angle += 2 * Math.PI;
        if (this.angle > 2 * Math.PI)
            this.angle -= 2 * Math.PI;
        this.currentX += this.deltaX;
        if (this.currentX < -W / 2)
            this.currentX += W;
        if (this.currentX > W / 2)
            this.currentX -= W;
        this.currentY -= this.deltaY;
        if (this.currentY < -H / 2)
            this.currentY += H;
        if (this.currentY > H / 2)
            this.currentY -= H;
    }

    public void render() {
        // Renderiza la forma y ubicación del Sprite, rotando la forma de su base y moviéndolo a la posición correcta en pantalla.

        this.sprite = new Polygon();
        for (int i = 0; i < this.shape.npoints; i++)
            // Método sprite.addPoint(int x, int y)
            this.sprite.addPoint(
                (int) Math.round(this.shape.xpoints[i] * Math.cos(this.angle) + this.shape.ypoints[i] * Math.sin(this.angle)) + (int) Math.round(this.currentX) + W / 2,
                (int) Math.round(this.shape.ypoints[i] * Math.cos(this.angle) - this.shape.xpoints[i] * Math.sin(this.angle)) + (int) Math.round(this.currentY) + H / 2);
    }

    public boolean isColliding(Sprite s) {
        // Determina si un Sprite se superpone a otro.
        // Para ello, confirma que los vértices y otros puntos no se sobrepongan entre los Sprites.
       
        for (int i = 0; i < s.sprite.npoints; i++)
            if (this.sprite.contains(s.sprite.xpoints[i], s.sprite.ypoints[i]))
                return true;
        for (int i = 0; i < this.sprite.npoints; i++)
            if (s.sprite.contains(this.sprite.xpoints[i], this.sprite.ypoints[i]))
                return true;
        return false;
    }
}